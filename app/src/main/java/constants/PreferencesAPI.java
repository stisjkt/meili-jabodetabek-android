package constants;

import java.util.HashMap;

import applications.Meili;
import id.ac.stis.meili.R;
import utilities.LoggingUtils;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */

public class PreferencesAPI {

    public static final String KEY_MIN_ACCURACY = "pref_min_acc";
    public static final String KEY_MIN_DISTANCE = "pref_min_dist";
    public static final String KEY_MIN_TIME = "pref_min_time";

    public static final String KEY_ACCELEROMETER_THRESHOLD = "pref_acc_threshold";
    public static final String KEY_ACCELEROMETER_PERIOD = "pref_acc_period";
    public static final String KEY_ACCELEROMETER_SLEEP = "pref_acc_saving";

    public static final String KEY_SERVER_URL = "pref_server_url";
    public static final String KEY_AUTO_UPLOAD = "pref_auto_upload";
    public static final String KEY_UPLOAD_CHUNK = "pref_upload_chunk";

    public static final String KEY_SHOW_LOGS = "pref_show_logs";
    public static final String KEY_LOGS_MAX_LINES = "pref_logs_max_lines";
    public static final String KEY_RECORD_ACCELEROMETER = "pref_record_accelerometer";


    public static final String KEY_USERNAME = "pref_username";

    public static final String KEY_RECORDING = "recording";

    public static final String VALUE_UPLOAD_CHUNK_INFINITE = "-1";
    public static final String VALUE_UPLOAD_CHUNK_50 = "50";
    public static final String VALUE_UPLOAD_CHUNK_100 = "100";
    public static final String VALUE_UPLOAD_CHUNK_150 = "150";
    public static final String VALUE_UPLOAD_CHUNK_200 = "200";

    public static final HashMap<String, Object> DEFAULT_VALUE = getDefaultHashMap();

    private static HashMap<String, Object> getDefaultHashMap() {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();

        hashMap.put(KEY_MIN_ACCURACY, "250"); // meters
        hashMap.put(KEY_MIN_DISTANCE, "50"); // meters
        hashMap.put(KEY_MIN_TIME, "30000"); // milliseconds

        hashMap.put(KEY_ACCELEROMETER_THRESHOLD, "0.6"); // default: 0.6
        hashMap.put(KEY_ACCELEROMETER_PERIOD, "30000"); // milliseconds
        hashMap.put(KEY_ACCELEROMETER_SLEEP, "90000"); // milliseconds

        hashMap.put(KEY_SERVER_URL, Meili.getInstance().getString(R.string.default_server_url));
        hashMap.put(KEY_AUTO_UPLOAD, "30"); // minutes
        hashMap.put(KEY_UPLOAD_CHUNK, VALUE_UPLOAD_CHUNK_INFINITE); // items

        hashMap.put(KEY_SHOW_LOGS, true);
        hashMap.put(KEY_LOGS_MAX_LINES, "50");
        hashMap.put(KEY_RECORD_ACCELEROMETER, false);

        hashMap.put(KEY_USERNAME,"");

        hashMap.put(KEY_RECORDING, true);

        return hashMap;
    }

    public static HashMap<String, Integer> prefId (){
        HashMap<String, Integer> pref = new HashMap<>();

        pref.put(KEY_MIN_ACCURACY,1);
        pref.put(KEY_MIN_DISTANCE,2);
        pref.put(KEY_MIN_TIME,3);

        pref.put(KEY_ACCELEROMETER_THRESHOLD,4);
        pref.put(KEY_ACCELEROMETER_PERIOD,5);
        pref.put(KEY_ACCELEROMETER_SLEEP,6);

        pref.put(KEY_SERVER_URL,7);
        pref.put(KEY_AUTO_UPLOAD,8);
        pref.put(KEY_UPLOAD_CHUNK,9);

        pref.put(KEY_SHOW_LOGS,10);
        pref.put(KEY_LOGS_MAX_LINES,11);
        pref.put(KEY_RECORD_ACCELEROMETER,12);

        pref.put(KEY_RECORDING,13);

        return pref;
    }
}
