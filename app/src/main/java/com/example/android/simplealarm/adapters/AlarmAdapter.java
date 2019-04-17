package com.example.android.simplealarm.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.android.simplealarm.AlarmReceiver;
import com.example.android.simplealarm.AppExecutors;
import com.example.android.simplealarm.R;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.AlarmUtils;

import java.util.List;

/**
 * This adapter creates and binds ViewHolders that hold the alarm details to a RecyclerView
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    // Member variable to handle item clicks
    final private ListItemClickListener mOnClickListener;

    // Class variables for the list that holds task data and the context
    private static List<AlarmEntry> mAlarmEntries;
    private Context mContext;

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
        void onAlarmClick(int adapterPosition, int itemId);
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
            int adapterPosition = getAdapterPosition();
            int alarmEntryId = mAlarmEntries.get(getAdapterPosition()).getId();
            mOnClickListener.onAlarmClick(adapterPosition, alarmEntryId);
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
                // Get adapterPosition of holder and update alarmEntry
                int adapterPosition = holder.getAdapterPosition();
                final AlarmEntry alarmEntry = mAlarmEntries.get(adapterPosition);
                int alarmEntryId = alarmEntry.getId();
                String time = alarmEntry.getTime();
                alarmEntry.setAlarmIsOn(isChecked);

                if (isChecked) {
                    // MyAlarm turned on, set alarm
                    new AlarmReceiver(mContext, alarmEntry);
                    String timeUntilAlarm = AlarmUtils.timeUntilAlarmFormatter(time);
                    Toast.makeText(mContext, mContext.getString(R.string.alarm_set_message) + ": " + timeUntilAlarm + " from now", Toast.LENGTH_LONG).show();
                } else {
                    // MyAlarm turned off, disable alarm
                    AlarmReceiver.cancelAlarm(mContext, alarmEntryId);
                }

                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase mDb = AppDatabase.getInstance(mContext);
                        mDb.alarmDao().updateAlarm(alarmEntry);
                    }
                });
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

    public static List<AlarmEntry> getTasks() {
        return mAlarmEntries;
    }

    public void setTasks(List<AlarmEntry> alarmEntries) {
        mAlarmEntries = alarmEntries;
        notifyDataSetChanged();
    }
}


