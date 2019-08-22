package fragments;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import constants.PreferencesAPI;
import preferences.PreferencesManager;
import id.ac.stis.meili.R;
import timber.log.Timber;
import utilities.LoggingUtils;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */

public class ParameterPreferenceFragment extends PreferenceFragment {

    private PreferencesManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = PreferencesManager.getInstance();

        addPreferencesFromResource(R.xml.preference_parameters);
        initPreferences();
    }

    private void initPreferences() {
        EditTextPreference minAccuracy = (EditTextPreference) findPreference(PreferencesAPI.KEY_MIN_ACCURACY);
        if (minAccuracy != null) {
            minAccuracy.setSummary(getString(R.string.min_accuracy_summary, manager.get(PreferencesAPI.KEY_MIN_ACCURACY)));
            minAccuracy.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        int value = Integer.parseInt(o.toString());
                        preference.setSummary(getString(R.string.min_accuracy_summary, o.toString()));

                        return value > 0;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }

        EditTextPreference minDistance = (EditTextPreference) findPreference(PreferencesAPI.KEY_MIN_DISTANCE);
        if (minDistance != null) {
            minDistance.setSummary(getString(R.string.min_distance_summary, manager.get(PreferencesAPI.KEY_MIN_DISTANCE)));
            minDistance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        double value = Double.parseDouble(o.toString());
                        preference.setSummary(getString(R.string.min_distance_summary, o.toString()));

                        return value > 0;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }

        final EditTextPreference minTime = (EditTextPreference) findPreference(PreferencesAPI.KEY_MIN_TIME);
        if (minTime != null) {
            minTime.setSummary(getString(R.string.min_time_summary, manager.get(PreferencesAPI.KEY_MIN_TIME)));
            minTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        int value = Integer.parseInt(o.toString());
                        preference.setSummary(getString(R.string.min_time_summary, o.toString()));

                        return value > 0;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }

        EditTextPreference accelerometerThreshold = (EditTextPreference) findPreference(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD);
        if (accelerometerThreshold != null) {
            accelerometerThreshold.setSummary(getString(R.string.accelerometer_threshold_summary, manager.get(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD)));
            accelerometerThreshold.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        double value = Double.parseDouble(o.toString());
                        preference.setSummary(getString(R.string.accelerometer_threshold_summary, o.toString()));

                        return value > 0;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }

        final EditTextPreference accelerometerPeriod = (EditTextPreference) findPreference(PreferencesAPI.KEY_ACCELEROMETER_PERIOD);
        if (accelerometerPeriod != null) {
            accelerometerPeriod.setSummary(getString(R.string.accelerometer_period_summary, manager.get(PreferencesAPI.KEY_ACCELEROMETER_PERIOD)));

            accelerometerPeriod.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        int value = Integer.parseInt(o.toString());
                        preference.setSummary(getString(R.string.accelerometer_period_summary, o.toString()));

                        return value > 0;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }

        final EditTextPreference accelerometerSleep = (EditTextPreference) findPreference(PreferencesAPI.KEY_ACCELEROMETER_SLEEP);
        if (accelerometerSleep != null) {
            accelerometerSleep.setSummary(getString(R.string.power_saving_period_summary, manager.get(PreferencesAPI.KEY_ACCELEROMETER_SLEEP)));
            accelerometerSleep.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        int value = Integer.parseInt(o.toString());
                        preference.setSummary(getString(R.string.power_saving_period_summary, o.toString()));

                        return value > 0;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }
    }
}
