package utilities;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import applications.Meili;
import constants.PreferencesAPI;
import constants.SessionManagerAPI;
import constants.Variables;
import id.ac.stis.meili.R;
import models.LogModel;
import preferences.PreferencesManager;
import preferences.SessionManager;
import timber.log.Timber;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */

public class LoggingUtils {

    public static final String TAG_GPS = "L01";
    public static final String TAG_WIFI = "L02";
    public static final String TAG_ACCELEROMETER = "L03";
    public static final String TAG_UPLOAD = "L04";
    public static final String TAG_SERVICE = "L05";
    public static final String TAG_SETTINGS = "L06";
    public static final String TAG_SYSTEM = "L07";
    public static final String TAG_ALARM_RECEIVER = "L08";
    public static final String TAG_ALARM_MANAGER = "L09";
    public static final String TAG_PREFERENCES_CHANGED = "L10";

    private static final String PREF_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final String DATE_FORMAT = "dd-MM-yyyy kk:mm:ss";
    private static final String LOG_FILE_FORMAT = "yyyy-MM-dd";


    public static void v(String tag, String message) {
        logToFile(tag, Log.VERBOSE, message);
    }

    public static void d(String tag, String message) {
        logToFile(tag, Log.DEBUG, message);
    }

    public static void i(String tag, String message) {
        logToFile(tag, Log.INFO, message);
    }

    public static void w(String tag, String message) {
        logToFile(tag, Log.WARN, message);
    }

    public static void e(String tag, String message) {
        logToFile(tag, Log.ERROR, message);
    }

    public static void wtf(String tag, String message) {
        logToFile(tag, Log.ASSERT, message);
    }

    public static void v(Context mContext, String tag, String... params) {
        writeLogToFirebase(mContext, tag, Log.VERBOSE, params);
    }

    public static void d(Context mContext, String tag, String... params) {
        writeLogToFirebase(mContext, tag, Log.DEBUG, params);
    }

    public static void i(Context mContext, String tag, String... params) {
        writeLogToFirebase(mContext, tag, Log.INFO, params);
    }

    public static void w(Context mContext, String tag, String... params) {
        writeLogToFirebase(mContext, tag, Log.WARN, params);
    }

    public static void e(Context mContext, String tag, String... params) {
        writeLogToFirebase(mContext, tag, Log.ERROR, params);
    }

    public static void wtf(Context mContext, String tag, String... params) {
        writeLogToFirebase(mContext, tag, Log.ASSERT, params);
    }
    public static void p(Context mContext) {
        String message_min_accuracy = PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_ACCURACY).toString();
        String message_min_distance = PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_DISTANCE).toString();
        String message_min_time = PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_TIME).toString();

        String message_pref_accel_threshold = PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD).toString();
        String message_pref_accel_period = PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_PERIOD).toString();
        String message_pref_pow_sav = PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_SLEEP).toString();

        writeLogToFirebase(mContext, PreferencesAPI.KEY_MIN_ACCURACY, Log.INFO, message_min_accuracy,true);
        writeLogToFirebase(mContext, PreferencesAPI.KEY_MIN_DISTANCE, Log.INFO, message_min_distance,true);
        writeLogToFirebase(mContext, PreferencesAPI.KEY_MIN_TIME, Log.INFO, message_min_time,true);

        writeLogToFirebase(mContext, PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD, Log.INFO, message_pref_accel_threshold,true);
        writeLogToFirebase(mContext, PreferencesAPI.KEY_ACCELEROMETER_PERIOD, Log.INFO, message_pref_accel_period,true);
        writeLogToFirebase(mContext, PreferencesAPI.KEY_ACCELEROMETER_SLEEP, Log.INFO, message_pref_pow_sav,true);
    }

    private static void logToFile(String tag, int level, String message) {
        if (!Variables.logToFile) {
            return;
        }

        try {
            File dir = new File(Variables.logFilePath);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    Timber.d("Successfully created " + dir.getAbsolutePath() + " directory");
                } else {
                    Timber.e("Failed to create " + dir.getAbsolutePath() + " directory");
                }
            }
            SimpleDateFormat logFileFormat = new SimpleDateFormat(LOG_FILE_FORMAT);
            File file = new File(Variables.logFilePath + File.separator + logFileFormat.format(new Date()) + ".log");
            if (file.createNewFile()) {
                Timber.d("Successfully created " + file.getAbsolutePath() + " file");
            } else {
                Timber.d("No need to create " + file.getAbsolutePath() + " file");
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            String lvl = getLevel(level);

            String log = Meili.getInstance().getResources().getString(R.string.logs_format, getTag(tag),
                    dateFormat.format(new Date()), lvl, message);

            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(log);
            bw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            Timber.e(e);
        }

        Meili.getInstance().sendBroadcast(new Intent(Variables.logFilter));
    }

    @Deprecated
    public static String readLogToHtml() {
        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(Variables.logFilePath);
            br = new BufferedReader(fr);

            String line;
            StringBuilder logs = new StringBuilder();

            while ((line = br.readLine()) != null) {
                int beginTag = line.indexOf("[");
                int endTag = line.indexOf("]") + 1;
                String tag = line.substring(beginTag, endTag);

                int beginDate = endTag + 1;
                int endDate = beginDate + 19;
                String date = line.substring(beginDate, endDate);

                int beginLevel = endDate + 1;
                int endLevel = line.indexOf(":", beginLevel);
                String level = line.substring(beginLevel, endLevel);

                int beginMessage = endLevel + 2;
                int endMessage = line.length();
                String message = line.substring(beginMessage, endMessage);

                logs.append("<br><small><b><font color='#eee'>").append(tag).append("</font></b> ").append(date).append("</small><br><b>")
                        .append(level).append("</b>: <i>").append(message).append("</i><br>");
            }

            return logs.toString();
        } catch (FileNotFoundException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }
        }

        return "";
    }

    @Deprecated
    public static List<LogModel> readLogToModelList() {

        FileReader fr = null;
        BufferedReader br = null;
        LinkedList<LogModel> logs = new LinkedList<>();

        try {
            fr = new FileReader(Variables.logFilePath);
            br = new BufferedReader(fr);

            String line;

            while ((line = br.readLine()) != null) {
                int beginTag = line.indexOf("[");
                int endTag = line.indexOf("]") + 1;
                String tag = line.substring(beginTag, endTag);

                int beginDate = endTag + 1;
                int endDate = beginDate + 19;
                String date = line.substring(beginDate, endDate);

                int beginLevel = endDate + 1;
                int endLevel = line.indexOf(":", beginLevel);
                String level = line.substring(beginLevel, endLevel);

                int beginMessage = endLevel + 2;
                int endMessage = line.length();

                String message = line.substring(beginMessage, endMessage);
                message = message.replace(", ", "\n");

                logs.addFirst(new LogModel(date, tag, level, message));
            }

        } catch (FileNotFoundException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }
        }

        LinkedList<LogModel> res = new LinkedList<>();
        if (logs.size() > 0) {
            int maxLine = Integer.valueOf(PreferencesManager.getInstance().get(PreferencesAPI.KEY_LOGS_MAX_LINES).toString());
            for (int i = 0; i < maxLine; i++) {
                if (i < logs.size()) {
                    res.add(logs.get(i));
                } else {
                    break;
                }
            }
        }

        return res;
    }

    private static String getLevel(int level) {
        switch (level) {
            case Log.VERBOSE:
                return "Verbose";
            case Log.DEBUG:
                return "Debug";
            case Log.INFO:
                return "Info";
            case Log.WARN:
                return "Warning";
            case Log.ERROR:
                return "Error";
            case Log.ASSERT:
                return "Assert";
        }
        return "Verbose";
    }

    public static int parseLevel(String level) {
        if ("Verbose".equalsIgnoreCase(level)) {
            return Log.VERBOSE;
        }
        if ("Debug".equalsIgnoreCase(level)) {
            return Log.DEBUG;
        }
        if ("Info".equalsIgnoreCase(level)) {
            return Log.INFO;
        }
        if ("Warning".equalsIgnoreCase(level)) {
            return Log.WARN;
        }
        if ("Error".equalsIgnoreCase(level)) {
            return Log.ERROR;
        }
        if ("Assert".equalsIgnoreCase(level)) {
            return Log.ASSERT;
        }

        return -1;
    }

    // ...
    private static void writeLogToFirebase(Context mContext, String tag, int level, String... params) {
        FirebaseApp.initializeApp(mContext);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("meili");
        String username = PreferencesManager.getInstance().get(PreferencesAPI.KEY_USERNAME).toString();

        SimpleDateFormat logFileFormat = new SimpleDateFormat(LOG_FILE_FORMAT);

        // /user_id/logs/2018-08-04/log_type:log_message
        String key = mDatabase.push().getKey();
        Date now = new Date();
        mDatabase.child(username)
                .child("logs")
                .child(logFileFormat.format(now))
                .child(key)
                .setValue(Meili.getInstance().getResources().getString(R.string.logs_firebase_format,
                        tag,
                        String.valueOf(now.getTime()),
                        TextUtils.join(" ", params)));
    }
    // ...
    private static  void writeLogToFirebase(Context mContext, String tag, int level,String message, boolean isPreference){
        FirebaseApp.initializeApp(mContext);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("meili");
        String username = PreferencesManager.getInstance().get(PreferencesAPI.KEY_USERNAME).toString();

        SimpleDateFormat dateFormat = new SimpleDateFormat(PREF_TIME_FORMAT);
        SimpleDateFormat logFileFormat = new SimpleDateFormat(LOG_FILE_FORMAT);
        String lvl = getLevel(level);

        if(isPreference){
            // /user_id/preference/min-sampling-time:30000
            mDatabase.child(username)
                    .child("preference")
                    .child(tag)
                    .setValue(message);
        }else{
            // /user_id/logs/2018-08-04/log_type:log_message
            String key = mDatabase.push().getKey();
            mDatabase.child(username)
                    .child("logs")
                    .child(logFileFormat.format(new Date()))
                    .child(key)
                    .setValue(Meili.getInstance().getResources().getString(R.string.logs_format, tag,
                            dateFormat.format(new Date()), lvl, message));

                    //.setValue(tag+String.valueOf(new Date().getTime())+ message);
        }
    }


    private static String getTag(String tagId) {
        HashMap<String,String> hMap = new HashMap<>();
        hMap.put(TAG_GPS,Meili.getInstance().getResources().getString(R.string.tag_gps));
        hMap.put(TAG_WIFI,Meili.getInstance().getResources().getString(R.string.tag_wifi));
        hMap.put(TAG_ACCELEROMETER,Meili.getInstance().getResources().getString(R.string.tag_accelerometer));
        hMap.put(TAG_UPLOAD,Meili.getInstance().getResources().getString(R.string.tag_upload));
        hMap.put(TAG_SERVICE,Meili.getInstance().getResources().getString(R.string.tag_service));
        hMap.put(TAG_SETTINGS,Meili.getInstance().getResources().getString(R.string.tag_settings));
        hMap.put(TAG_SYSTEM,Meili.getInstance().getResources().getString(R.string.tag_system));
        hMap.put(TAG_ALARM_RECEIVER,Meili.getInstance().getResources().getString(R.string.tag_alarm_receiver));
        hMap.put(TAG_ALARM_MANAGER,Meili.getInstance().getResources().getString(R.string.tag_alarm_manager));
        hMap.put(TAG_PREFERENCES_CHANGED,Meili.getInstance().getResources().getString(R.string.tag_preferences_changed));

        return hMap.get(tagId);
    }
}
