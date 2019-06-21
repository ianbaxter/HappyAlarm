package com.example.android.simplealarm.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.simplealarm.AlarmInstance;
import com.example.android.simplealarm.AppExecutors;
import com.example.android.simplealarm.R;
import com.example.android.simplealarm.database.AlarmEntry;
import com.example.android.simplealarm.database.AppDatabase;
import com.example.android.simplealarm.utilities.NotificationUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private static final String CURRENT_ALARM_TIME_IN_MILLIS_KEY = "current_alarm_time_in_millis";
    private static final String TAG = AlarmAdapter.class.getSimpleName();

    private static List<AlarmEntry> mAlarmEntries;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private AlarmItemExpandClickListener alarmItemExpandClickListener;
    private AlarmTimeClickListener alarmTimeClickListener;
    private RingtoneItemClickListener ringtoneItemClickListener;
    private int expandedPosition;
    private int previousExpandedPosition = -1;

    public AlarmAdapter(Context context, RecyclerView recyclerView, int expandedAlarmItemPosition){
        mContext = context;
        mRecyclerView = recyclerView;
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
        private Switch alarmOnOffSwitch;
        private CheckBox repeatAlarmCheckbox;
        private TextView repeatSummaryTextView;
        private Button dismissAlarmSnoozeButton;
        private Button alarmRingtoneButton;
        private Button deleteAlarmButton;
        private ImageView alarmDownArrow;
        private ImageView alarmUpArrow;
        private CheckBox repeatAlarmMonday;
        private CheckBox repeatAlarmTuesday;
        private CheckBox repeatAlarmWednesday;
        private CheckBox repeatAlarmThursday;
        private CheckBox repeatAlarmFriday;
        private CheckBox repeatAlarmSaturday;
        private CheckBox repeatAlarmSunday;

        private AlarmViewHolder(View alarmView) {
            super(alarmView);
            alarmTimeButton = alarmView.findViewById(R.id.button_alarm_time);
            alarmOnOffSwitch = alarmView.findViewById(R.id.switch_alarm);
            repeatAlarmCheckbox = alarmView.findViewById(R.id.checkbox_repeat);
            repeatSummaryTextView = alarmView.findViewById(R.id.text_view_repeat_summary);
            dismissAlarmSnoozeButton = alarmView.findViewById(R.id.button_dismiss_snooze);
            alarmRingtoneButton = alarmView.findViewById(R.id.button_pick_ringtone);
            deleteAlarmButton = alarmView.findViewById(R.id.button_delete_alarm);
            alarmDownArrow = alarmView.findViewById(R.id.image_arrow_down);
            alarmUpArrow = alarmView.findViewById(R.id.image_arrow_up);
            repeatAlarmMonday = alarmView.findViewById(R.id.checkbox_repeat_monday);
            repeatAlarmTuesday = alarmView.findViewById(R.id.checkbox_repeat_tuesday);
            repeatAlarmWednesday = alarmView.findViewById(R.id.checkbox_repeat_wednesday);
            repeatAlarmThursday = alarmView.findViewById(R.id.checkbox_repeat_thursday);
            repeatAlarmFriday = alarmView.findViewById(R.id.checkbox_repeat_friday);
            repeatAlarmSaturday = alarmView.findViewById(R.id.checkbox_repeat_saturday);
            repeatAlarmSunday = alarmView.findViewById(R.id.checkbox_repeat_sunday);

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
        boolean isAlarmOn = alarmEntry.isAlarmOn();
        boolean isAlarmRepeating = alarmEntry.isAlarmRepeating();
        boolean[] daysRepeating = alarmEntry.getDaysRepeating();

        // Set alarm sound text
        Uri ringtonePathUri = Uri.parse(alarmEntry.getRingtonePath());
        Ringtone ringtone = RingtoneManager.getRingtone(mContext, ringtonePathUri);
        String ringtoneTitle = ringtone.getTitle(mContext);
        viewHolder.alarmRingtoneButton.setText(ringtoneTitle);

        // Set alarm repeat summary text
        String daysRepeatingSummaryText;
        int numberOfDaysRepeatingForSummary = 0;
        for (boolean dayRepeating : daysRepeating) {
            if (dayRepeating) {
                numberOfDaysRepeatingForSummary++;
            }
        }

        if (numberOfDaysRepeatingForSummary == 7 && isAlarmRepeating) {
            daysRepeatingSummaryText = mContext.getString(R.string.repeating_summary_every_day);

        } else if (!isAlarmRepeating) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            long alarmTimeInMillis = sharedPreferences.getLong(CURRENT_ALARM_TIME_IN_MILLIS_KEY, 0);
            Calendar calendar = Calendar.getInstance(Locale.UK);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            long midnightTonightInMillis = calendar.getTimeInMillis();

            if (alarmTimeInMillis <= midnightTonightInMillis) {
                daysRepeatingSummaryText = mContext.getString(R.string.repeating_summary_today);
            } else {
                daysRepeatingSummaryText = mContext.getString(R.string.repeating_summary_no_repeat);
            }

        } else {
            SparseBooleanArray daysRepeatingSparse = new SparseBooleanArray(daysRepeating.length);
            for (int i = 0; i < daysRepeating.length; i++) {
                daysRepeatingSparse.append(i, daysRepeating[i]);
            }
            ArrayList<String> daysRepeatingSummary = new ArrayList<>();
            for (int i = 0; i < daysRepeatingSparse.size(); i++) {
                if (daysRepeatingSparse.get(i)) {
                    switch (i) {
                        case 0:
                            daysRepeatingSummary.add("Mon");
                            break;
                        case 1:
                            daysRepeatingSummary.add("Tues");
                            break;
                        case 2:
                            daysRepeatingSummary.add("Weds");
                            break;
                        case 3:
                            daysRepeatingSummary.add("Thurs");
                            break;
                        case 4:
                            daysRepeatingSummary.add("Fri");
                            break;
                        case 5:
                            daysRepeatingSummary.add("Sat");
                            break;
                        case 6:
                            daysRepeatingSummary.add("Sun");
                            break;
                    }
                }
            }
            daysRepeatingSummaryText = TextUtils.join(", ", daysRepeatingSummary);
            daysRepeatingSummaryText = daysRepeatingSummaryText.replace("[", "");
            daysRepeatingSummaryText = daysRepeatingSummaryText.replace("]", "");
        }
        viewHolder.repeatSummaryTextView.setText(daysRepeatingSummaryText);

        // Set alarm On/Off colour
        if (isAlarmOn) {
            viewHolder.alarmTimeButton.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            viewHolder.repeatSummaryTextView.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            viewHolder.alarmTimeButton.setTextColor(mContext.getResources().getColor(R.color.light_grey));
            viewHolder.repeatSummaryTextView.setTextColor(mContext.getResources().getColor(R.color.light_grey));
        }

        // Set alarm time text
        viewHolder.alarmTimeButton.setText(alarmEntry.getTime());

        // Set boolean values for On/Off switch, repeat checkbox and repeatDay checkboxes
        viewHolder.alarmOnOffSwitch.setChecked(isAlarmOn);
        viewHolder.repeatAlarmCheckbox.setChecked(isAlarmRepeating);
        if (isAlarmRepeating) {
            viewHolder.repeatAlarmMonday.setChecked(daysRepeating[0]);
            viewHolder.repeatAlarmTuesday.setChecked(daysRepeating[1]);
            viewHolder.repeatAlarmWednesday.setChecked(daysRepeating[2]);
            viewHolder.repeatAlarmThursday.setChecked(daysRepeating[3]);
            viewHolder.repeatAlarmFriday.setChecked(daysRepeating[4]);
            viewHolder.repeatAlarmSaturday.setChecked(daysRepeating[5]);
            viewHolder.repeatAlarmSunday.setChecked(daysRepeating[6]);
        }
        viewHolder.alarmOnOffSwitch.setOnCheckedChangeListener(getOnOffListener(viewHolder));
        viewHolder.repeatAlarmCheckbox.setOnCheckedChangeListener(getRepeatListener(viewHolder));
        viewHolder.repeatAlarmMonday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.repeatAlarmTuesday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.repeatAlarmWednesday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.repeatAlarmThursday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.repeatAlarmFriday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.repeatAlarmSaturday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));
        viewHolder.repeatAlarmSunday.setOnCheckedChangeListener(getRepeatDayListener(viewHolder));

        // Set click listeners on sound picker, snooze dismiss and delete alarm buttons
        viewHolder.alarmRingtoneButton.setOnClickListener(v -> ringtoneItemClickListener.onRingtoneClick(position, alarmEntry));

        viewHolder.dismissAlarmSnoozeButton.setOnClickListener(v -> {
            int alarmEntryId = alarmEntry.getId();
            Intent dismissAlarmIntent = NotificationUtils.getDismissSnoozeIntent(mContext, alarmEntryId);
            mContext.sendBroadcast(dismissAlarmIntent);
            alarmEntry.setAlarmSnoozed(false);
            AppExecutors.getsInstance().diskIO().execute(() -> updateAlarmEntry(alarmEntry));
        });

        viewHolder.deleteAlarmButton.setOnClickListener(v -> {
            int position1 = viewHolder.getAdapterPosition();
            onDeleteAlarm(position1);
        });

        // Set visibilities dependant on expand/collapse state of alarm item
        final boolean isExpanded = position==expandedPosition;
        viewHolder.repeatAlarmCheckbox.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.repeatAlarmMonday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.repeatAlarmTuesday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.repeatAlarmWednesday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.repeatAlarmThursday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.repeatAlarmFriday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.repeatAlarmSaturday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.repeatAlarmSunday.setVisibility((isExpanded && isAlarmRepeating)?View.VISIBLE:View.GONE);
        viewHolder.alarmRingtoneButton.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.deleteAlarmButton.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.alarmUpArrow.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        viewHolder.repeatSummaryTextView.setVisibility(isExpanded?View.GONE:View.VISIBLE);
        viewHolder.alarmDownArrow.setVisibility(isExpanded?View.GONE:View.VISIBLE);
        viewHolder.itemView.setActivated(isExpanded);

        if (isExpanded) {
            previousExpandedPosition = position;
        }

        viewHolder.itemView.setOnClickListener(view -> {
            expandedPosition = isExpanded ? -1:position;
            notifyItemChanged(previousExpandedPosition);
            notifyItemChanged(position);
            if (!isExpanded) {
                alarmItemExpandClickListener.onAlarmItemExpandClick(position);
            }
        });


        if (alarmEntry.isAlarmSnoozed()) {
            // Set dismiss snooze button text and visibility
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String alarmSnoozeTime = sharedPreferences.getString(mContext.getString(R.string.pref_snooze_time_key), "5");
            if (alarmSnoozeTime != null && alarmSnoozeTime.equals("1")) {
                viewHolder.dismissAlarmSnoozeButton.setText(mContext.getString(R.string.snooze_dismiss_minute, alarmSnoozeTime));
            } else {
                viewHolder.dismissAlarmSnoozeButton.setText(mContext.getString(R.string.snooze_dismiss_minutes, alarmSnoozeTime));
            }
            viewHolder.dismissAlarmSnoozeButton.setVisibility(View.VISIBLE);
        } else {
            viewHolder.dismissAlarmSnoozeButton.setVisibility(View.GONE);
        }
    }

    @NonNull
    private CompoundButton.OnCheckedChangeListener getOnOffListener(@NonNull final AlarmViewHolder holder) {
        return (buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                AlarmEntry alarmEntry = getAlarmEntryFromHolder(holder);
                alarmEntry.setAlarmOn(isChecked);
                if (isChecked) {
                    new AlarmInstance(mContext, alarmEntry);
                } else {
                    AlarmInstance.cancelAlarm(mContext, alarmEntry.getId());
                    // Reset saved alarm time in sharedPreferences to 0
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    sharedPreferences.edit().putLong(CURRENT_ALARM_TIME_IN_MILLIS_KEY, 0).apply();
                }

                AppExecutors.getsInstance().diskIO().execute(() -> updateAlarmEntry(alarmEntry));
            }
        };
    }

    @NonNull
    private CompoundButton.OnCheckedChangeListener getRepeatListener(@NonNull final AlarmViewHolder holder) {
        return (buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                AlarmEntry alarmEntry = getAlarmEntryFromHolder(holder);
                alarmEntry.setAlarmRepeating(isChecked);
                if (alarmEntry.isAlarmOn()) {
                    new AlarmInstance(mContext, alarmEntry);
                }

                AppExecutors.getsInstance().diskIO().execute(() -> updateAlarmEntry(alarmEntry));
            }
        };
    }

    @NonNull
    private CompoundButton.OnCheckedChangeListener getRepeatDayListener(@NonNull final AlarmViewHolder holder) {
        return (buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                AlarmEntry alarmEntry = getAlarmEntryFromHolder(holder);
                boolean[] daysRepeating = alarmEntry.getDaysRepeating();

                // Check if only one day is set to repeat when pressed
                boolean oneDayRepeating = false;
                if (!isChecked) {
                    int numberOfDaysRepeating = 0;
                    for (boolean dayRepeating : daysRepeating) {
                        if (dayRepeating) {
                            numberOfDaysRepeating ++;
                        }
                    }
                    if (numberOfDaysRepeating == 1) {
                        // Save last repeating day in daysRepeating
                        oneDayRepeating = true;
                        alarmEntry.setAlarmRepeating(false);
                    }
                }

                if (!oneDayRepeating) {
                    switch (buttonView.getId()) {
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
                }

                AppExecutors.getsInstance().diskIO().execute(() -> updateAlarmEntry(alarmEntry));

                if (alarmEntry.isAlarmOn()) {
                    new AlarmInstance(mContext, alarmEntry);
                }
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
        AppDatabase appDatabase = AppDatabase.getInstance(mContext);
        appDatabase.alarmDao().updateAlarm(alarmEntry);
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

    private void onDeleteAlarm(int position) {
        // Reset expanded position
        int currentlyExpandedPosition = expandedPosition;
        if (currentlyExpandedPosition > position) {
            expandedPosition = currentlyExpandedPosition - 1;
        } else if (currentlyExpandedPosition == position) {
            expandedPosition = -1;
        }

        AppDatabase appDatabase = AppDatabase.getInstance(mContext);
        AlarmEntry alarmEntry = mAlarmEntries.get(position);
        Snackbar snackbar = Snackbar.make(mRecyclerView, R.string.snackbar_delete_alarm_text, Snackbar.LENGTH_LONG);

        snackbar.setAction(R.string.snackbar_delete_alarm_action, v -> {
            // retrieve expanded position
            if (currentlyExpandedPosition > position) {
                expandedPosition = currentlyExpandedPosition;
            } else {
                expandedPosition = currentlyExpandedPosition;
            }

            AppExecutors.getsInstance().diskIO().execute(() -> appDatabase.alarmDao().insertAlarm(alarmEntry));
            new AlarmInstance(mContext, alarmEntry);
        });

        AppExecutors.getsInstance().diskIO().execute(() -> appDatabase.alarmDao().deleteAlarm(alarmEntry));

        snackbar.show();

        if (alarmEntry.isAlarmOn()) {
            int alarmEntryId = alarmEntry.getId();
            AlarmInstance.cancelAlarm(mContext, alarmEntryId);
            // Reset saved alarm time in sharedPreferences to 0
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            sharedPreferences.edit().putLong(CURRENT_ALARM_TIME_IN_MILLIS_KEY, 0).apply();
        }
    }
}


