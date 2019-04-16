package com.example.android.simplealarm;

import android.app.Activity;
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
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.simplealarm.adapters.AlarmAdapter;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.viewmodels.MainViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AlarmAdapter.ListItemClickListener,
        SetTimeFragment.TimeDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TIME_PICKER_FRAGMENT_ID = "timePicker";
    private static final String CLICKED_ITEM_INDEX_KEY = "clickedItemIndex";
    private static final String CLICKED_ITEM_POSITION_KEY = "clickedItemPosition";

    private AlarmAdapter mAdaptor;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);

        // set size of RecyclerView to be fixed as changes of content will not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adaptor
        mAdaptor = new AlarmAdapter(this, this);
        recyclerView.setAdapter(mAdaptor);

        // Add a touch helper to the RecyclerView to recognise when the user swipes a ViewHolder.
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDirection) {
                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<AlarmEntry> alarmEntries = AlarmAdapter.getTasks();
                        AlarmEntry alarmEntry = alarmEntries.get(position);
                        // Cancel PendingIntent for deleted alarm if alarmIsOn
                        if (alarmEntry.getAlarmIsOn()) {
                            int alarmEntryId = alarmEntry.getId();
                            AlarmReceiver.cancelAlarm(getApplicationContext(), alarmEntryId);
                        }
                        mDb.alarmDao().deleteAlarm(alarmEntry);
                    }
                });
            }
        }).attachToRecyclerView(recyclerView);

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getAlarms().observe(this, new Observer<List<AlarmEntry>>() {
            @Override
            public void onChanged(@Nullable List<AlarmEntry> alarmEntries) {
                Log.d(TAG, "Updating the list of alarms from LiveData in ViewModel");
                mAdaptor.setTasks(alarmEntries);
            }
        });
    }

    @Override
    public void onAlarmClick(int adapterPosition, int clickedItemIndex) {
        showAndUpdateTimePickerDialog(adapterPosition, clickedItemIndex);
        Toast.makeText(this, "Position: " + clickedItemIndex, Toast.LENGTH_LONG).show();
    }

    public void showNewTimePickerDialog(View view) {
        DialogFragment newFragment = new SetTimeFragment();
        newFragment.show(getSupportFragmentManager(), TIME_PICKER_FRAGMENT_ID);
    }

    public void showAndUpdateTimePickerDialog(int adapterPosition, int clickedItemIndex) {
        DialogFragment newFragment = new SetTimeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CLICKED_ITEM_POSITION_KEY, adapterPosition);
        bundle.putInt(CLICKED_ITEM_INDEX_KEY, clickedItemIndex);
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), TIME_PICKER_FRAGMENT_ID);
    }

    @Override
    public void onFinishNewTimeDialog(String time) {
        final AlarmEntry alarmEntry = new AlarmEntry(time, false);
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.alarmDao().insertAlarm(alarmEntry);
            }
        });

    }

    @Override
    public void onFinishUpdateTimeDialog(final String time, final int clickedItemIndex) {
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                AlarmEntry alarmEntry = mDb.alarmDao().loadAlarmById(clickedItemIndex);
                int alarmEntryId = alarmEntry.getId();
                alarmEntry.setTime(time);
                // Check if alarm is already on and turn on if not
                boolean alarmIsOn = alarmEntry.getAlarmIsOn();
                if (!alarmIsOn) {
                    alarmEntry.setAlarmIsOn(true);
                } else {
                    AlarmReceiver.cancelAlarm(MainActivity.this, alarmEntryId);
                    AlarmAdapter.setAlarm(MainActivity.this, alarmEntry);
                    // Toast here causing Error: Can't toast on a thread that has not called Looper.prepare()
//                    Toast.makeText(MainActivity.this, R.string.alarm_set_message, Toast.LENGTH_LONG).show();
                }
                mDb.alarmDao().updateAlarm(alarmEntry);
            }
        });
    }
}
