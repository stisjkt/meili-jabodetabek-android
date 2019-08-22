package fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import constants.PreferencesAPI;
import preferences.PreferencesManager;
import id.ac.stis.meili.R;
import timber.log.Timber;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */

public class MiscPreferenceFragment extends PreferenceFragment {

    private PreferencesManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = PreferencesManager.getInstance();

        addPreferencesFromResource(R.xml.preference_misc);
        initPreferences();
    }

    private void initPreferences() {
        final EditTextPreference maxLines = (EditTextPreference) findPreference(PreferencesAPI.KEY_LOGS_MAX_LINES);
        if (maxLines != null) {
            maxLines.setSummary(getString(R.string.logs_max_lines_summary, manager.get(PreferencesAPI.KEY_LOGS_MAX_LINES)));
            maxLines.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        int value = Integer.parseInt(o.toString());
                        preference.setSummary(getString(R.string.logs_max_lines_summary, o.toString()));

                        return value > 0;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }

        final CheckBoxPreference showLogs = (CheckBoxPreference) findPreference(PreferencesAPI.KEY_SHOW_LOGS);
        if (showLogs != null) {
            boolean state = (Boolean) manager.get(PreferencesAPI.KEY_SHOW_LOGS);
            if (state) {
                showLogs.setSummary(getString(R.string.show_logs_summary_show));
            } else {
                showLogs.setSummary(getString(R.string.show_logs_summary_hide));
            }

            showLogs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean state = (Boolean) o;
                    if (state) {
                        preference.setSummary(getString(R.string.show_logs_summary_show));
                        if (maxLines != null) {
                            maxLines.setEnabled(true);
                        }
                    } else {
                        preference.setSummary(getString(R.string.show_logs_summary_hide));
                        if (maxLines != null) {
                            maxLines.setEnabled(false);
                        }
                    }

                    return true;
                }
            });
        }

        final CheckBoxPreference recordAccelerometer = (CheckBoxPreference) findPreference(PreferencesAPI.KEY_RECORD_ACCELEROMETER);
        if (recordAccelerometer != null) {
            boolean state = (Boolean) manager.get(PreferencesAPI.KEY_RECORD_ACCELEROMETER);
            if (state) {
                recordAccelerometer.setSummary(getString(R.string.record_accelerometer_summary_record));
            } else {
                recordAccelerometer.setSummary(getString(R.string.record_accelerometer_summary_not));
            }

            recordAccelerometer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean state = (Boolean) o;
                    if (state) {
                        preference.setSummary(getString(R.string.record_accelerometer_summary_record));
                    } else {
                        preference.setSummary(getString(R.string.record_accelerometer_summary_not));
                    }

                    return true;
                }
            });
        }
    }
}
