package preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import constants.SessionManagerAPI;

/**
 * Created by Rahadi on 01/03/2018.
 */

public class SessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(SessionManagerAPI.Keys.SESSION_KEY, Context.MODE_PRIVATE);
    }

    public HashMap<String, String> getUserSession() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(SessionManagerAPI.Keys.KEY_USERNAME, pref.getString(SessionManagerAPI.Keys.KEY_USERNAME, null));
        map.put(SessionManagerAPI.Keys.KEY_PASSWORD, pref.getString(SessionManagerAPI.Keys.KEY_PASSWORD, null));
        map.put(SessionManagerAPI.Keys.KEY_TOKEN, pref.getString(SessionManagerAPI.Keys.KEY_TOKEN, null));
        map.put(SessionManagerAPI.Keys.KEY_DATE, pref.getString(SessionManagerAPI.Keys.KEY_DATE, null));

        return map;
    }

    public void setUserSession(String username, String password, String token, String formattedDate) {
        editor = pref.edit();
        editor.putString(SessionManagerAPI.Keys.KEY_USERNAME, username);
        editor.putString(SessionManagerAPI.Keys.KEY_PASSWORD, password);
        editor.putString(SessionManagerAPI.Keys.KEY_TOKEN, token);
        editor.putString(SessionManagerAPI.Keys.KEY_DATE, formattedDate);
        editor.clear();
        editor.apply();
    }

    public void clearUserSession() {
        editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    public void setDate(String formattedDate) {
        editor = pref.edit();
        editor.putString(SessionManagerAPI.Keys.KEY_DATE, formattedDate);
        editor.apply();
    }

    public void updateDate() {
        Date date = new Date(System.currentTimeMillis());
        String formattedDate = SimpleDateFormat.getInstance().format(date);
        setDate(formattedDate);
    }
}
