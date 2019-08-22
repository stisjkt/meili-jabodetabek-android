package activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import fragments.MiscPreferenceFragment;
import fragments.ParameterPreferenceFragment;
import fragments.ServerPreferenceFragment;
import preferences.PreferencesManager;
import id.ac.stis.meili.R;
import timber.log.Timber;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */

public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int REQUEST_SETTINGS = 1;
    public static final int RESULT_SETTINGS_CHANGED = 101;
    public static final int RESULT_SETTINGS_UNCHANGED = 102;

    public static final String RESULT_SETTINGS_LIST = "settings_list";
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;
    private PreferencesManager preferencesManager;
    private HashMap<String, Object> changedPreferences;
    private boolean isPreferenceChanged;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesManager = PreferencesManager.getInstance();
        preferencesManager.getPreferences().registerOnSharedPreferenceChangeListener(this);

        changedPreferences = new HashMap<String, Object>();
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return (MiscPreferenceFragment.class.getName().equals(fragmentName) ||
                ParameterPreferenceFragment.class.getName().equals(fragmentName) ||
                ServerPreferenceFragment.class.getName().equals(fragmentName)
        );
    }

    @Override
    public void onBackPressed() {
        if (isPreferenceChanged) {
            Intent i = new Intent();
            i.putExtra(RESULT_SETTINGS_LIST, changedPreferences);
            setResult(RESULT_SETTINGS_CHANGED, i);
        } else {
            setResult(RESULT_SETTINGS_UNCHANGED);
        }

        super.onBackPressed();
    }

    //
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        changedPreferences.put(s, preferencesManager.get(s));
        isPreferenceChanged = true;
    }

    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     *
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (mHasHeaders != null && mLoadHeaders != null) {
            try {
                return (Boolean) mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
                Timber.e(e);
            } catch (IllegalAccessException e) {
                Timber.e(e);
            } catch (InvocationTargetException e) {
                Timber.e(e);
            }
        }
        return false;
    }

//    @Override
//    public void onCreate(Bundle aSavedState) {
//        //onBuildHeaders() will be called during super.onCreate()
//        try {
//            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
//            mHasHeaders = getClass().getMethod("hasHeaders");
//        } catch (NoSuchMethodException e) {
//            Timber.e(e);
//        }
//
//        preferencesManager = PreferencesManager.getInstance();
//        preferencesManager.getPreferences().registerOnSharedPreferenceChangeListener(this);
//
//        changedPreferences = new HashMap<String, Object>();
//
//        super.onCreate(aSavedState);
//        if (!isNewV11Prefs()) {
//            addPreferencesFromResource(R.xml.preference_parameters);
//            addPreferencesFromResource(R.xml.preference_server);
//        }
//    }
//
//    @Override
//    public void onBuildHeaders(List<Header> aTarget) {
//        try {
//            mLoadHeaders.invoke(this, R.xml.preference_headers, aTarget);
//        } catch (IllegalArgumentException e) {
//            Timber.e(e);
//        } catch (IllegalAccessException e) {
//            Timber.e(e);
//        } catch (InvocationTargetException e) {
//            Timber.e(e);
//        }
//    }
}
