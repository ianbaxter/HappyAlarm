package com.birdbathapps.HappyAlarmClock;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;

import java.util.Objects;

public class SettingsFragment extends androidx.preference.PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        AudioManager audioManager = (AudioManager) Objects.requireNonNull(getActivity()).getSystemService(Context.AUDIO_SERVICE);
        int maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int currentAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        SeekBarPreference seekBarPreference = findPreference(getActivity().getString(R.string.pref_volume_key));
        if (seekBarPreference != null) {
            seekBarPreference.setMax(maxAlarmVolume);
            seekBarPreference.setValue(currentAlarmVolume);
            seekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                int newAlarmVolume = Integer.parseInt(newValue.toString());

                if (preference != null) {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newAlarmVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    return true;
                } else {
                    return false;
                }
            });
        }

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (!(preference instanceof SeekBarPreference) && !(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference != null) {
            if (!(preference instanceof SeekBarPreference) && !(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(key, "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
