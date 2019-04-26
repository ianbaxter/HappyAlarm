package com.example.android.simplealarm;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.example.android.simplealarm.adapters.AlarmAdapter;
import com.example.android.simplealarm.adapters.EmptyRecyclerView;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.AlarmUtils;
import com.example.android.simplealarm.viewmodels.MainViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.ListItemClickListener,
        SetTimeFragment.TimeDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TIME_PICKER_FRAGMENT_ID = "timePicker";
    private static final String CLICKED_ALARM_ID_KEY = "clickedAlarmId";
    private static final String CLICKED_ALARM_POSITION_KEY = "clickedAlarmPosition";

    private AlarmAdapter mAdaptor;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdaptor = new AlarmAdapter(this, this);
        TextView emptyView = findViewById(R.id.tv_empty_view);
        EmptyRecyclerView recyclerView = findViewById(R.id.my_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdaptor);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);

        newItemTouchHelper().attachToRecyclerView(recyclerView);

        mDb = AppDatabase.getInstance(getApplicationContext());

        setupViewModel();
    }

    private ItemTouchHelper newItemTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDirection) {
                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        AlarmEntry alarmEntry = getAlarmEntryFromViewHolder();
                        boolean isAlarmOn = alarmEntry.isAlarmOn();

                        if (isAlarmOn) {
                            int alarmEntryId = alarmEntry.getId();
                            AlarmInstance.cancelAlarm(MainActivity.this, alarmEntryId);
                        }
                        mDb.alarmDao().deleteAlarm(alarmEntry);
                    }

                    private AlarmEntry getAlarmEntryFromViewHolder() {
                        int position = viewHolder.getAdapterPosition();
                        List<AlarmEntry> alarmEntries = AlarmAdapter.getAlarmEntries();
                        return alarmEntries.get(position);
                    }
                });
            }
        });
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getAlarms().observe(this, new Observer<List<AlarmEntry>>() {
            @Override
            public void onChanged(@Nullable List<AlarmEntry> alarmEntries) {
                Log.d(TAG, "Updating the list of alarms from LiveData in ViewModel");
                mAdaptor.setAlarmEntries(alarmEntries);
            }
        });
    }

    @Override
    public void onAlarmClick(int adapterPosition, int alarmEntryId) {
        updateTimePickerDialog(adapterPosition, alarmEntryId);
    }

    public void updateTimePickerDialog(int adapterPosition, int alarmEntryId) {
        DialogFragment newFragment = new SetTimeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CLICKED_ALARM_POSITION_KEY, adapterPosition);
        bundle.putInt(CLICKED_ALARM_ID_KEY, alarmEntryId);
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), TIME_PICKER_FRAGMENT_ID);
    }

    public void newTimePickerDialog(View view) {
        DialogFragment newFragment = new SetTimeFragment();
        newFragment.show(getSupportFragmentManager(), TIME_PICKER_FRAGMENT_ID);
    }

    @Override
    public void onFinishNewAlarm(String time) {
        final AlarmEntry alarmEntry = new AlarmEntry(time, false, false);
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.alarmDao().insertAlarm(alarmEntry);
            }
        });
    }

    @Override
    public void onFinishUpdateAlarm(final String time, final int alarmEntryId) {
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(alarmEntryId);
                alarmEntry.setTime(time);

                boolean isAlarmOn = alarmEntry.isAlarmOn();
                if (isAlarmOn) {
                    AlarmInstance.cancelAlarm(MainActivity.this, alarmEntryId);
                } else {
                    alarmEntry.setAlarmOn(true);
                }

                mDb.alarmDao().updateAlarm(alarmEntry);
                new AlarmInstance(MainActivity.this, alarmEntry);
            }
        });
        String timeUntilAlarm = AlarmUtils.timeUntilAlarmFormatter(time);
        Toast.makeText(this, this.getString(R.string.alarm_set_message) + ": " + timeUntilAlarm + " from now", Toast.LENGTH_LONG).show();
    }
}
