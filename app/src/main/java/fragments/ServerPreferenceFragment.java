package fragments;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import constants.PreferencesAPI;
import preferences.PreferencesManager;
import id.ac.stis.meili.R;
import timber.log.Timber;

import static android.webkit.URLUtil.isValidUrl;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */

public class ServerPreferenceFragment extends PreferenceFragment {

    private PreferencesManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = PreferencesManager.getInstance();

        addPreferencesFromResource(R.xml.preference_server);
        initPreferences();
    }

    private void initPreferences() {
        final EditTextPreference serverUrl = (EditTextPreference) findPreference(PreferencesAPI.KEY_SERVER_URL);
        if (serverUrl != null) {
            serverUrl.setSummary(getString(R.string.server_url_summary, manager.get(PreferencesAPI.KEY_SERVER_URL)));
            serverUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        String value = o.toString();

                        if (value.endsWith("/")) {
                            value = value.substring(0, value.lastIndexOf("/"));
                        }

                        if (isValidUrl(value)) {
                            PreferencesManager.getInstance().save(PreferencesAPI.KEY_SERVER_URL, value);
                            preference.setSummary(getString(R.string.server_url_summary, value));
                            serverUrl.setText(value);
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                    return false;
                }
            });
        }

        EditTextPreference autoUpload = (EditTextPreference) findPreference(PreferencesAPI.KEY_AUTO_UPLOAD);
        if (autoUpload != null) {
            autoUpload.setSummary(getString(R.string.auto_upload_summary, manager.get(PreferencesAPI.KEY_AUTO_UPLOAD)));
            autoUpload.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        int value = Integer.parseInt(o.toString());
                        preference.setSummary(getString(R.string.auto_upload_summary, o.toString()));
                        return value > 0;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }

        ListPreference uploadChunk = (ListPreference) findPreference(PreferencesAPI.KEY_UPLOAD_CHUNK);
        if (uploadChunk != null) {


            uploadChunk.setSummary(getString(R.string.upload_chunk_size_summary,
                    getUploadChunkSummary(manager.get(PreferencesAPI.KEY_UPLOAD_CHUNK))));

            uploadChunk.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        preference.setSummary(getString(R.string.upload_chunk_size_summary,
                                getUploadChunkSummary(o)));
                        return true;
                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                }
            });
        }
    }

    private String getUploadChunkSummary(Object value) {
        if (value.equals(PreferencesAPI.VALUE_UPLOAD_CHUNK_INFINITE)) {
            return getString(R.string.all_at_once);
        } else if (value.equals(PreferencesAPI.VALUE_UPLOAD_CHUNK_50)) {
            return getString(R.string.items_50);
        } else if (value.equals(PreferencesAPI.VALUE_UPLOAD_CHUNK_100)) {
            return getString(R.string.items_100);
        } else if (value.equals(PreferencesAPI.VALUE_UPLOAD_CHUNK_150)) {
            return getString(R.string.items_150);
        } else if (value.equals(PreferencesAPI.VALUE_UPLOAD_CHUNK_200)) {
            return getString(R.string.items_200);
        }

        return "N/A";
    }
}
