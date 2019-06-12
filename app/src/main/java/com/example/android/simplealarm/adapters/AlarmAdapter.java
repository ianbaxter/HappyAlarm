package com.example.android.simplealarm.adapters;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.android.simplealarm.AlarmInstance;
import com.example.android.simplealarm.AppExecutors;
import com.example.android.simplealarm.R;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.AlarmUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    final private AlarmItemClickListener alarmItemClickListener;
    private static List<AlarmEntry> mAlarmEntries;
    private Context mContext;

    public AlarmAdapter(Context context, AlarmItemClickListener listener){
        mContext = context;
        alarmItemClickListener = listener;
    }

    public interface AlarmItemClickListener {
        void onAlarmClick(int adapterPosition, int alarmEntryId);
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public Button alarmTimeButton;
        public Switch alarmSwitch;
        public CheckBox alarmRepeatButton;

        public AlarmViewHolder(View alarmView) {
            super(alarmView);
            alarmTimeButton = alarmView.findViewById(R.id.button_alarm_time);
            alarmSwitch = alarmView.findViewById(R.id.switch_alarm);
            alarmRepeatButton = alarmView.findViewById(R.id.checkbox_repeat);

            alarmTimeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            AlarmEntry alarmEntry = mAlarmEntries.get(getAdapterPosition());
            int alarmEntryId = alarmEntry.getId();
            alarmItemClickListener.onAlarmClick(adapterPosition, alarmEntryId);
        }
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);

        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AlarmViewHolder viewHolder, int position) {
        AlarmEntry alarmEntry = mAlarmEntries.get(position);
        String alarmTime = alarmEntry.getTime();
        boolean isAlarmOn = alarmEntry.isAlarmOn();
        boolean isAlarmRepeating = alarmEntry.isAlarmRepeating();

        viewHolder.alarmTimeButton.setText(alarmTime);
        viewHolder.alarmSwitch.setChecked(isAlarmOn);
        viewHolder.alarmRepeatButton.setChecked(isAlarmRepeating);

        viewHolder.alarmSwitch.setOnCheckedChangeListener(getSwitchListener(viewHolder));
        viewHolder.alarmRepeatButton.setOnCheckedChangeListener(getRepeatButtonListener(viewHolder));
    }

    @NonNull
    private CompoundButton.OnCheckedChangeListener getSwitchListener(@NonNull final AlarmViewHolder holder) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isOn) {
                if (buttonView.isPressed()) {
                    final AlarmEntry alarmEntry = getAlarmEntryFromHolder(holder);
                    int alarmEntryId = alarmEntry.getId();
                    String alarmTime = alarmEntry.getTime();
                    alarmEntry.setAlarmOn(isOn);

                    if (isOn) {
                        setNewAlarm(alarmEntry, alarmTime);
                    } else {
                        AlarmInstance.cancelAlarm(mContext, alarmEntryId);
                    }

                    AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            updateAlarmEntry(alarmEntry);
                        }
                    });
                }
            }

            private void setNewAlarm(AlarmEntry alarmEntry, String alarmTime) {
                new AlarmInstance(mContext, alarmEntry);
                String timeUntilAlarm = AlarmUtils.timeUntilAlarmFormatter(alarmTime);
                AlarmUtils.showTimeUntilAlarmSnack(mContext, timeUntilAlarm);
            }
        };
    }

    @NonNull
    private CompoundButton.OnCheckedChangeListener getRepeatButtonListener(@NonNull final AlarmViewHolder holder) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isRepeating) {
                if (buttonView.isPressed()) {
                    final AlarmEntry alarmEntry = getAlarmEntryFromHolder(holder);
                    alarmEntry.setAlarmRepeating(isRepeating);

                    boolean alarmIsOn = alarmEntry.isAlarmOn();
                    if (alarmIsOn) {
                        resetCurrentAlarm(alarmEntry);
                    }

                    AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            updateAlarmEntry(alarmEntry);
                        }
                    });

                    if (isRepeating) {
                        RecyclerView recyclerView = ((Activity)mContext).findViewById(R.id.recycler_view_main);
                        Snackbar.make(recyclerView, mContext.getString(R.string.alarm_repeating_message), Snackbar.LENGTH_LONG).show();
                    }
                }
            }

            private void resetCurrentAlarm(AlarmEntry alarmEntry) {
                int alarmEntryId = alarmEntry.getId();
                AlarmInstance.cancelAlarm(mContext, alarmEntryId);
                new AlarmInstance(mContext, alarmEntry);
            }
        };
    }

    private AlarmEntry getAlarmEntryFromHolder(@NonNull AlarmViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        return getAlarmEntryFromAdapterPosition(adapterPosition);
    }

    public static AlarmEntry getAlarmEntryFromAdapterPosition(int adapterPosition) {
        List<AlarmEntry> alarmEntries = getAlarmEntries();
        return alarmEntries.get(adapterPosition);
    }

    private void updateAlarmEntry(AlarmEntry alarmEntry) {
        AppDatabase mDb = AppDatabase.getInstance(mContext);
        mDb.alarmDao().updateAlarm(alarmEntry);
    }

    public static List<AlarmEntry> getAlarmEntries() {
        return mAlarmEntries;
    }

    public void setAlarmEntries(List<AlarmEntry> alarmEntries) {
        mAlarmEntries = alarmEntries;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mAlarmEntries == null) {
            return 0;
        }
        return mAlarmEntries.size();
    }

    public void onItemRemove(Snackbar snackbar, int adapterPosition) {
        AlarmEntry alarmEntry = mAlarmEntries.get(adapterPosition);
        snackbar.setAction(R.string.snackbar_delete_alarm_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlarmEntries.add(adapterPosition, alarmEntry);
                        notifyItemInserted(adapterPosition);
                    }
                });
        snackbar.show();
        mAlarmEntries.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }
}


