package com.birdbathapps.HappyAlarmClock;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;

import com.birdbathapps.HappyAlarmClock.adapters.AlarmAdapter;
import com.birdbathapps.HappyAlarmClock.adapters.EmptyRecyclerView;
import com.birdbathapps.HappyAlarmClock.database.AlarmEntry;
import com.birdbathapps.HappyAlarmClock.database.AppDatabase;
import com.birdbathapps.HappyAlarmClock.viewmodels.MainViewModel;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.AlarmTimeClickListener,
        AlarmAdapter.RingtoneItemClickListener, AlarmAdapter.AlarmItemExpandClickListener, SetTimeFragment.TimeDialogListener {

    private static final String TIME_PICKER_FRAGMENT_ID = "time_picker";
    private static final String CLICKED_ALARM_ID_KEY = "clicked_alarm_id";
    private static final String CLICKED_ALARM_POSITION_KEY = "clicked_alarm_position";
    private static final String SAVED_EXPANDED_POSITION_KEY = "saved_expanded_position";
    private static final int RINGTONE_PICKER = 0;

    private AlarmAdapter alarmAdaptor;
    private AppDatabase appDatabase;

    private int clickedAlarmRingtonePosition;
    private int savedExpandedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView emptyView = findViewById(R.id.tv_empty_view_main);
        EmptyRecyclerView recyclerView = findViewById(R.id.recycler_view_main);

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_EXPANDED_POSITION_KEY)) {
            savedExpandedPosition = savedInstanceState.getInt(SAVED_EXPANDED_POSITION_KEY, -1);
        }

        createView(emptyView, recyclerView);
        appDatabase = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SAVED_EXPANDED_POSITION_KEY, savedExpandedPosition);
    }

    private void createView(TextView emptyView, EmptyRecyclerView recyclerView) {
        alarmAdaptor = new AlarmAdapter(this, recyclerView, savedExpandedPosition);
        alarmAdaptor.setHasStableIds(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(alarmAdaptor);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getAlarms().observe(this, alarmEntries -> alarmAdaptor.setAlarmEntries(alarmEntries));

    }

    @Override
    public void onAlarmTimeClick(int adapterPosition, int alarmEntryId) {
        updateTimePickerDialog(adapterPosition, alarmEntryId);
    }

    @Override
    public void onAlarmItemExpandClick(int expandedPosition) {
        savedExpandedPosition = expandedPosition;
    }

    @Override
    public void onRingtoneClick(int position, AlarmEntry alarmEntry) {
        Uri currentRingtone = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtone_picker_title));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        clickedAlarmRingtonePosition = position;
        startActivityForResult(intent, RINGTONE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_PICKER && resultCode == RESULT_OK) {
            Uri ringtoneUri = null;
            if (data != null) {
                ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            }
            if (ringtoneUri != null) {
                String ringtonePath = ringtoneUri.toString();
                AlarmEntry alarmEntry = AlarmAdapter.getAlarmEntryFromAdapterPosition(clickedAlarmRingtonePosition);
                alarmEntry.setRingtonePath(ringtonePath);
                AppExecutors.getsInstance().diskIO().execute(() -> appDatabase.alarmDao().updateAlarm(alarmEntry));
            } else {
                Timber.e("Error getting data from intent");
            }
        }
    }

    public void updateTimePickerDialog(int adapterPosition, int alarmEntryId) {
        DialogFragment setTimeFragment = new SetTimeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CLICKED_ALARM_POSITION_KEY, adapterPosition);
        bundle.putInt(CLICKED_ALARM_ID_KEY, alarmEntryId);
        setTimeFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(setTimeFragment, TIME_PICKER_FRAGMENT_ID);

        DialogFragment fragment = (SetTimeFragment) getSupportFragmentManager().findFragmentByTag(TIME_PICKER_FRAGMENT_ID);
        if (fragment == null ) {
            fragmentTransaction.commit();
        }
    }

    public void newTimePickerDialog(View view) {
        DialogFragment setTimeFragment = new SetTimeFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(setTimeFragment, TIME_PICKER_FRAGMENT_ID);

        DialogFragment fragment = (SetTimeFragment) getSupportFragmentManager().findFragmentByTag(TIME_PICKER_FRAGMENT_ID);
        if (fragment == null ) {
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onFinishNewAlarm(String time) {
        String defaultTone = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM).toString();
        // daysRepeating represents {monday, tuesday, wednesday, thursday, friday, saturday, sunday}
        boolean[] daysRepeating = {true,true,true,true,true,true,true};
        final AlarmEntry alarmEntry = new AlarmEntry(time, defaultTone, false, false, false, daysRepeating, 0);
        AppExecutors.getsInstance().diskIO().execute(() -> appDatabase.alarmDao().insertAlarm(alarmEntry));
    }

    @Override
    public void onFinishUpdateAlarm(final String time, final int alarmEntryId) {
        AppExecutors.getsInstance().diskIO().execute(() -> {
            AlarmEntry alarmEntry = appDatabase.alarmDao().loadAlarmById(alarmEntryId);
            alarmEntry.setTime(time);

            if (alarmEntry.isAlarmOn()) {
                AlarmInstance.cancelAlarm(MainActivity.this, alarmEntryId);
            } else {
                alarmEntry.setAlarmOn(true);
            }

            new AlarmInstance(MainActivity.this, alarmEntry);
            appDatabase.alarmDao().updateAlarm(alarmEntry);
        });
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
