package com.example.android.simplealarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;

import java.util.List;

/**
 * This adapter creates and binds ViewHolders that hold the alarm details to a RecyclerView
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    // Member variable to handle item clicks
    final private ListItemClickListener mOnClickListener;

    // Class variables for the list that holds task data and the context
    private List<AlarmEntry> mAlarmEntries;
    private Context mContext;

    // Member variable for the database
    private AppDatabase mDb;
    private AlarmReceiver mAlarmReceiver;

    private static final String TAG = AlarmAdapter.class.getSimpleName();

    /**
     * Constructor for AlarmAdapter
     *
     * @param context the ccurrent context
     * @param listener the ItemClickListener
     */
    public AlarmAdapter(Context context, ListItemClickListener listener){
        mContext = context;
        mOnClickListener = listener;
    }

    /**
     * Add interface for ClickListener
     */
    public interface ListItemClickListener {
        void onAlarmClick(int itemId);
    }

    // Inner class for creating ViewHolders
    class AlarmViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public Button alarmTimeButton;
        public Switch alarmSwitch;

        /**
         * Constructor for the AlarmViewHolders
         *
         * @param alarmView The view inflated in onCreateViewHolder
         */
        public AlarmViewHolder(View alarmView) {
            super(alarmView);
            alarmTimeButton = alarmView.findViewById(R.id.button_alarm_time);
            alarmSwitch = alarmView.findViewById(R.id.switch_alarm);

            alarmTimeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mAlarmEntries.get(getAdapterPosition()).getId();
            mOnClickListener.onAlarmClick(elementId);
        }
    }

    /**
     * Called when ViewHolders are created to fill the RecyclerView
     *
     * @param parent The parent view to this child view
     * @param viewType
     *
     * @return A new AlarmViewHolder that holds the view for each alarm
     */
    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflate the alarm_list_item to a view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_item, parent, false);

        return new AlarmViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position
     *
     * @param holder The ViewHolder to bind Cursor data to
     * @param position The position of the data in the cursor
     */
    @Override
    public void onBindViewHolder(@NonNull final AlarmViewHolder holder, int position) {
        // Determine the values of the wanted data
        AlarmEntry alarmEntry = mAlarmEntries.get(position);
        String time = alarmEntry.getTime();
        boolean alarmState = alarmEntry.getAlarmIsOn();

        // Set values
        holder.alarmTimeButton.setText(time);
        holder.alarmSwitch.setChecked(alarmState);

        // Set OnCheckedChangeListener to alarmSwitch
        holder.alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Get position of holder and update alarmEntry
                int position = holder.getAdapterPosition();
                final AlarmEntry alarmEntry = mAlarmEntries.get(position);
                alarmEntry.setAlarmIsOn(isChecked);
                mDb = AppDatabase.getInstance(mContext.getApplicationContext());

                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.alarmDao().updateAlarm(alarmEntry);
                    }
                });

                if (isChecked) {
                    // Alarm turned on, set alarm
                    setAlarm(position);
                } else {
                    // Alarm turned off, disable alarm
                    if (mAlarmReceiver != null) {
                        mAlarmReceiver.cancelAlarm(buttonView.getContext(), position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mAlarmEntries == null) {
            return 0;
        }
        return mAlarmEntries.size();
    }

    public List<AlarmEntry> getTasks() {
        return mAlarmEntries;
    }

    public void setTasks(List<AlarmEntry> alarmEntries) {
        mAlarmEntries = alarmEntries;
        notifyDataSetChanged();
    }

    private void setAlarm(int position) {
        // Get position of holder and alarmTime String
        AlarmEntry alarmEntry = mAlarmEntries.get(position);
        String alarmTime = alarmEntry.getTime();

        mAlarmReceiver = new AlarmReceiver(mContext, alarmTime, position);

        Log.i(TAG, "Alarm set for: " + alarmTime);
        Toast.makeText(mContext, R.string.alarm_set_message, Toast.LENGTH_LONG).show();
    }
}
