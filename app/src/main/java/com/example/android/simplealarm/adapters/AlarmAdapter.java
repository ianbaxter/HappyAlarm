package com.example.android.simplealarm.adapters;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.example.android.simplealarm.AlarmInstance;
import com.example.android.simplealarm.AppExecutors;
import com.example.android.simplealarm.R;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.AlarmUtils;
import com.example.android.simplealarm.utilities.NotificationUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private static final String TAG = AlarmAdapter.class.getSimpleName();

    private static List<AlarmEntry> mAlarmEntries;
    private Context mContext;
    private AlarmItemExpandClickListener alarmItemExpandClickListener;
    private AlarmTimeClickListener alarmTimeClickListener;
    private RingtoneItemClickListener ringtoneItemClickListener;
    private int expandedPosition;
    private int previousExpandedPosition = -1;

    public AlarmAdapter(Context context, int expandedAlarmItemPosition){
        mContext = context;
        expandedPosition = expandedAlarmItemPosition;

        try {
            alarmItemExpandClickListener = (AlarmItemExpandClickListener) context;
            alarmTimeClickListener = (AlarmTimeClickListener) context;
            ringtoneItemClickListener = (RingtoneItemClickListener) context;
        } catch (ClassCastException ex) {
            Log.e(TAG, "ClassCastException: " + ex);
        }
    }

    public interface AlarmItemExpandClickListener {
        void onAlarmItemExpandClick(int expandedPosition);
    }

    public interface AlarmTimeClickListener {
        void onAlarmTimeClick(int adapterPosition, int alarmEntryId);
    }

    public interface RingtoneItemClickListener {
        void onRingtoneClick(int adapterPosition, AlarmEntry alarmEntry);
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private Button alarmTimeButton;
        private Switch alarmSwitch;
        private CheckBox alarmRepeatAll;
        private Button alarmDismissSnoozeButton;
        private Button alarmRingtoneButton;
        private ImageView alarmDownArrow;
        private ImageView alarmUpArrow;
        private CheckBox alarmRepeatMonday;
        private CheckBox alarmRepeatTuesday;
        private CheckBox alarmRepeatWednesday;
        private CheckBox alarmRepeatThursday;
        private CheckBox alarmRepeatFriday;
        private CheckBox alarmRepeatSaturday;
        private CheckBox alarmRepeatSunday;

        private AlarmViewHolder(View alarmView) {
            super(alarmView);
            alarmTimeButton = alarmView.findViewById(R.id.button_alarm_time);
            alarmSwitch = alarmView.findViewById(R.id.switch_alarm);
            alarmRepeatAll = alarmView.findViewById(R.id.checkbox_repeat);
            alarmDismissSnoozeButton = alarmView.findViewById(R.id.button_dismiss_snooze);
            alarmRingtoneButton = alarmView.findViewById(R.id.button_pick_ringtone);
            alarmDownArrow = alarmView.findViewById(R.id.image_arrow_down);
            alarmUpArrow = alarmView.findViewById(R.id.image_arrow_up);
            alarmRepeatMonday = alarmView.findViewById(R.id.checkbox_repeat_monday);
            alarmRepeatTuesday = alarmView.findViewById(R.id.checkbox_repeat_tuesday);
            alarmRepeatWednesday = alarmView.findViewById(R.id.checkbox_repeat_wednesday);
            alarmRepeatThursday = alarmView.findViewById(R.id.checkbox_repeat_thursday);
            alarmRepeatFriday = alarmView.findViewById(R.id.checkbox_repeat_friday);
            alarmRepeatSaturday = alarmView.findViewById(R.id.checkbox_repeat_saturday);
            alarmRepeatSunday = alarmView.findViewById(R.id.checkbox_repeat_sunday);

            alarmTimeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            AlarmEntry alarmEntry = mAlarmEntries.get(getAdapterPosition());
            int alarmEntryId = alarmEntry.getId();
            alarmTimeClickListener.onAlarmTimeClick(adapterPosition, alarmEntryId);
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
        boolean isAlarmSnoozed = alarmEntry.isAlarmSnoozed();
        boolean isAlarmRepeating = alarmEntry.isAlarmRepeating();
        boolean[] daysRepeating = alarmEntry.getDaysRepeating();
        boolean isAlarmRepeatingMonday = daysRepeating[0];
        boolean isAlarmRepeatingTuesday = daysRepeating[1];
        boolean isAlarmRepeatingWednesday = daysRepeating[2];
        boolean isAlarmRepeatingThursday = daysRepeating[3];
        boolean isAlarmRepeatingFriday = daysRepeating[4];
        boolean isAlarmRepeatingSaturday = daysRepeating[5];
        boolean isAlarmRepeatingSunday = daysRepeating[6];

        viewHolder.alarmTimeButton.setText(alarmTime);
        viewHolder.alarmSwitch.setChecked(isAlarmOn);
        viewHolder.alarmRepeatAll.setChecked(isAlarmRepeating);
        viewHolder.alarmRepeatMonday.setChecked(isAlarmRepeatingMonday);
        viewHolder.alarmRepeatTuesday.setChecked(isAlarmRepeatingTuesday);
        viewHolder.alarmRepeatWednesday.setChecked(isAlarmRepeatingWednesday);
        viewHolder.alarmRepeatThursday.setChecked(isAlarmRepeatingThursday);
        viewHolder.alarmRepeatFriday.setChecked(isAlarmRepeatingFriday);
        viewHolder.alarmRepeatSaturday.setChecked(isAlarmRepeatingSaturday);
        viewHolder.alarmRepeatSunday.setChecked(isAlarmRepeatingSunday);

        String ringtonePath = alarmEntry.getRingtonePath();
        Uri ringtonePathUri = Uri.parse(ringtonePath);
        Ringtone ringtone = RingtoneManager.getRingtone(mContext, ringtonePathUri);
        String ringtoneTitle = ringtone.getTitle(mContext);
        viewHolder.alarmRingtoneButton.setText(ringtoneTitle);

        viewHolder.alarmSwitch.setOnCheckedChangeListener(getOnOffListener(viewHolder));
        viewHolder.alarmRepeatAll.setOnCheckedChangeListener(getRepeatListener(viewHolder));
        viewHolder.alarmRepeatMonday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.alarmRepeatTuesday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.alarmRepeatWednesday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.alarmRepeatThursday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.alarmRepeatFriday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.alarmRepeatSaturday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.alarmRepeatSunday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));

        viewHolder.alarmRingtoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringtoneItemClickListener.onRingtoneClick(position, alarmEntry);
            }
        });

        viewHolder.alarmDismissSnoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int alarmEntryId = alarmEntry.getId();
                Intent dismissAlarmIntent = NotificationUtils.getDismissSnoozeIntent(mContext, alarmEntryId);
                mContext.sendBroadcast(dismissAlarmIntent);
                alarmEntry.setAlarmSnoozed(false);
                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        updateAlarmEntry(alarmEntry);
                    }
                });
            }
        });

        if (isAlarmSnoozed) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String alarmSnoozeTime = sharedPreferences.getString(mContext.getString(R.string.pref_snooze_time_key), "5");
            if (alarmSnoozeTime != null && alarmSnoozeTime.equals("1")) {
                viewHolder.alarmDismissSnoozeButton.setText(mContext.getString(R.string.snooze_dismiss_minute, alarmSnoozeTime));
            } else {
                viewHolder.alarmDismissSnoozeButton.setText(mContext.getString(R.string.snooze_dismiss_minutes, alarmSnoozeTime));
            }
            viewHolder.alarmDismissSnoozeButton.setVisibility(View.VISIBLE);
        } else {
            viewHolder.alarmDismissSnoozeButton.setVisibility(View.GONE);
        }

        /**
         * Slows down code?
         */
        if (isAlarmOn) {
            viewHolder.alarmTimeButton.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        } else {
            viewHolder.alarmTimeButton.setTextColor(mContext.getResources().getColor(R.color.light_grey));
        }

        final boolean isExpanded = position==expandedPosition;
        viewHolder.alarmRepeatAll.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.alarmRepeatMonday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.alarmRepeatTuesday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.alarmRepeatWednesday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.alarmRepeatThursday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.alarmRepeatFriday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.alarmRepeatSaturday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.alarmRepeatSunday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.alarmRingtoneButton.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.alarmUpArrow.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.alarmDownArrow.setVisibility(isExpanded?View.GONE:View.VISIBLE);
        viewHolder.itemView.setActivated(isExpanded);

        if (isExpanded) {
            previousExpandedPosition = position;
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandedPosition = isExpanded ? -1:position;
                notifyItemChanged(previousExpandedPosition);
                notifyItemChanged(position);
                if (!isExpanded) {
                    alarmItemExpandClickListener.onAlarmItemExpandClick(position);
                } else {
                    alarmItemExpandClickListener.onAlarmItemExpandClick(-1);
                }
            }
        });
    }

    @NonNull
    private CompoundButton.OnCheckedChangeListener getOnOffListener(@NonNull final AlarmViewHolder holder) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isOn) {
                if (buttonView.isPressed()) {
                    AlarmEntry alarmEntry = getAlarmEntryFromHolder(holder);
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
    private CompoundButton.OnCheckedChangeListener getRepeatListener(@NonNull final AlarmViewHolder holder) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isRepeating) {
                if (buttonView.isPressed()) {
                    AlarmEntry alarmEntry = getAlarmEntryFromHolder(holder);
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

    @NonNull
    private CompoundButton.OnCheckedChangeListener getRepeatDayListener(@NonNull final AlarmViewHolder holder) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    AlarmEntry alarmEntry = getAlarmEntryFromHolder(holder);
                    boolean[] daysRepeating = alarmEntry.getDaysRepeating();
                    int buttonViewId = buttonView.getId();
                    switch (buttonViewId) {
                        case R.id.checkbox_repeat_monday:
                            daysRepeating[0] = isChecked;
                            break;
                        case R.id.checkbox_repeat_tuesday:
                            daysRepeating[1] = isChecked;
                            break;
                        case R.id.checkbox_repeat_wednesday:
                            daysRepeating[2] = isChecked;
                            break;
                        case R.id.checkbox_repeat_thursday:
                            daysRepeating[3] = isChecked;
                            break;
                        case R.id.checkbox_repeat_friday:
                            daysRepeating[4] = isChecked;
                            break;
                        case R.id.checkbox_repeat_saturday:
                            daysRepeating[5] = isChecked;
                            break;
                        case R.id.checkbox_repeat_sunday:
                            daysRepeating[6] = isChecked;
                            break;
                    }
                    alarmEntry.setDaysRepeating(daysRepeating);

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

    @Override
    public long getItemId(int position) {
        return mAlarmEntries.get(position).getId();
    }

    public void onItemRemove(Snackbar snackbar, int adapterPosition, AppDatabase appDatabase) {
        int currentlyExpandedPosition = expandedPosition;
        if (adapterPosition == currentlyExpandedPosition) {
            expandedPosition = -1;
        }

        AlarmEntry alarmEntry = mAlarmEntries.get(adapterPosition);
        snackbar.setAction(R.string.snackbar_delete_alarm_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adapterPosition == currentlyExpandedPosition) {
                            expandedPosition = adapterPosition;
                        }

                        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                appDatabase.alarmDao().insertAlarm(alarmEntry);
                            }
                        });
                        new AlarmInstance(mContext, alarmEntry);
                    }
                });

        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.alarmDao().deleteAlarm(alarmEntry);
            }
        });

        snackbar.show();

        boolean isAlarmOn = alarmEntry.isAlarmOn();
        if (isAlarmOn) {
            int alarmEntryId = alarmEntry.getId();
            AlarmInstance.cancelAlarm(mContext, alarmEntryId);
        }
    }
}


