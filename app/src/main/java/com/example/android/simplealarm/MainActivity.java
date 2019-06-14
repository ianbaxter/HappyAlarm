package com.example.android.simplealarm;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;

import com.example.android.simplealarm.adapters.AlarmAdapter;
import com.example.android.simplealarm.adapters.EmptyRecyclerView;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.AlarmUtils;
import com.example.android.simplealarm.viewmodels.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.AlarmItemClickListener,
        AlarmAdapter.RingtoneItemClickListener, SetTimeFragment.TimeDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TIME_PICKER_FRAGMENT_ID = "time_picker";
    private static final String CLICKED_ALARM_ID_KEY = "clicked_alarm_id";
    private static final String CLICKED_ALARM_POSITION_KEY = "clicked_alarm_position";
    private static final int RINGTONE_PICKER = 0;

    private AlarmAdapter alarmAdaptor;
    private EmptyRecyclerView recyclerView;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView emptyView = findViewById(R.id.tv_empty_view_main);
        recyclerView = findViewById(R.id.recycler_view_main);
        FloatingActionButton newAlarmFab = findViewById(R.id.fab_add_alarm);

        createView(emptyView, recyclerView, newAlarmFab);
        appDatabase = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();
    }

    private void createView(TextView emptyView, EmptyRecyclerView recyclerView, FloatingActionButton fab) {
        alarmAdaptor = new AlarmAdapter(this, recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(alarmAdaptor);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
        newItemTouchHelper().attachToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0 && !fab.isShown()) {
                    fab.show();
                } else if (dy > 0 && fab.isShown()) {
                    fab.hide();
                }
            }
        });

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
    }

    private ItemTouchHelper newItemTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDirection) {
                int adapterPosition = viewHolder.getAdapterPosition();
                List<AlarmEntry> originalAlarmEntries = AlarmAdapter.getAlarmEntries();
                int originalAlarmEntriesSize = originalAlarmEntries.size();
                AlarmEntry alarmEntry = originalAlarmEntries.get(adapterPosition);
                Snackbar snackbar = Snackbar.make(recyclerView, R.string.snackbar_delete_alarm_text, Snackbar.LENGTH_LONG);
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        List<AlarmEntry> newAlarmEntries = AlarmAdapter.getAlarmEntries();
                        int newAlarmEntriesSize = newAlarmEntries.size();
                        if (newAlarmEntriesSize < originalAlarmEntriesSize) {
                            AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    boolean isAlarmOn = alarmEntry.isAlarmOn();

                                    if (isAlarmOn) {
                                        int alarmEntryId = alarmEntry.getId();
                                        AlarmInstance.cancelAlarm(MainActivity.this, alarmEntryId);
                                    }
                                    appDatabase.alarmDao().deleteAlarm(alarmEntry);
                                }
                            });
                        }
                    }
                });
                alarmAdaptor.onItemRemove(snackbar, adapterPosition);
            }
        });
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getAlarms().observe(this, new Observer<List<AlarmEntry>>() {
            @Override
            public void onChanged(@Nullable List<AlarmEntry> alarmEntries) {
                Log.d(TAG, "Updating the list of alarms from LiveData in ViewModel");
                alarmAdaptor.setAlarmEntries(alarmEntries);
            }
        });
    }

    @Override
    public void onAlarmClick(int adapterPosition, int alarmEntryId) {
        updateTimePickerDialog(adapterPosition, alarmEntryId);
    }

    @Override
    public void onRingtoneClick(int position, AlarmEntry alarmEntry) {
        final Uri defaultRingtone = Uri.parse("android.resource://com.example.android.simplealarm/" + R.raw.alarm1);
        Uri currentRingtone = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtone_picker_title));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultRingtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(CLICKED_ALARM_POSITION_KEY, position);
        startActivityForResult(intent, RINGTONE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_PICKER && resultCode == RESULT_OK) {
            int position = -1;
            Uri ringtoneUri = null;
            if (data != null) {
                position = data.getIntExtra(CLICKED_ALARM_POSITION_KEY, 0);
                ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            }
            if (position != -1 && ringtoneUri != null) {
                String ringtonePath = ringtoneUri.toString();
                AlarmEntry alarmEntry = AlarmAdapter.getAlarmEntryFromAdapterPosition(position);
                alarmEntry.setRingtonePath(ringtonePath);
                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        appDatabase.alarmDao().updateAlarm(alarmEntry);
                    }
                });
            } else {
                Log.e(TAG, "Error getting data from intent");
            }
        }
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
        String defaultTone = "android.resource://com.example.android.simplealarm/" + R.raw.alarm1;
        final AlarmEntry alarmEntry = new AlarmEntry(time, defaultTone, false, false, false);
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.alarmDao().insertAlarm(alarmEntry);
            }
        });
    }

    @Override
    public void onFinishUpdateAlarm(final String time, final int alarmEntryId) {
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                AlarmEntry alarmEntry = appDatabase.alarmDao().loadAlarmById(alarmEntryId);
                alarmEntry.setTime(time);

                boolean isAlarmOn = alarmEntry.isAlarmOn();
                if (isAlarmOn) {
                    AlarmInstance.cancelAlarm(MainActivity.this, alarmEntryId);
                } else {
                    alarmEntry.setAlarmOn(true);
                }

                appDatabase.alarmDao().updateAlarm(alarmEntry);
                new AlarmInstance(MainActivity.this, alarmEntry);
            }
        });
        String timeUntilAlarm = AlarmUtils.timeUntilAlarmFormatter(time);
        AlarmUtils.showTimeUntilAlarmSnack(this, timeUntilAlarm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_gallery:
                startGalleryActivity();
                break;
            case R.id.action_settings:
                startSettingsActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startGalleryActivity() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }
}
