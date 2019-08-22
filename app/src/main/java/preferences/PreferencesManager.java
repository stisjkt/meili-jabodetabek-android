package preferences;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;

import applications.Meili;
import constants.PreferencesAPI;
import timber.log.Timber;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */

public class PreferencesManager {

    private static PreferencesManager instance;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private PreferencesManager() {
        preferences = PreferenceManager.getDefaultSharedPreferences(Meili.getInstance());
    }

    public static PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }

        return instance;
    }

    public Object get(String key) {
        Object defaultValue = null;
        Object value = null;

        try {
            defaultValue = PreferencesAPI.DEFAULT_VALUE.get(key);
        } catch (Exception e) {
            Timber.e(e);
        }

        if (defaultValue == null || defaultValue instanceof String) {
            value = preferences.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            value = preferences.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Long) {
            value = preferences.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Integer) {
            value = preferences.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Float) {
            value = preferences.getFloat(key, (Float) defaultValue);
        }
        return value;
    }

    public void reset(String key) {
        Object defaultValue = PreferencesAPI.DEFAULT_VALUE.get(key);
        save(key, defaultValue);
    }

    public void save(String key, Object value) {
        editor = preferences.edit();
        if (value == null || value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        } else {
            throw new RuntimeException("Unhandled preference value type: " + value);
        }
        editor.apply();
    }

    public boolean getBoolean(String key, boolean value) {
        return preferences.getBoolean(key, value);
    }

    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    public void reloadPreferences() {
        for (Map.Entry<String, Object> keyValuePair : PreferencesAPI.DEFAULT_VALUE.entrySet()) {
            save(keyValuePair.getKey(), get(keyValuePair.getKey()));
        }
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }
}
