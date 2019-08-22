package tasks;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import activities.ServiceActivity;
import applications.Meili;
import constants.Constants;
import constants.PreferencesAPI;
import constants.Variables;
import daos.AdministrativeDao;
import preferences.PreferencesManager;
import preferences.SessionManager;
import timber.log.Timber;

import static android.content.Context.ACTIVITY_SERVICE;

public class LoginTask extends AsyncTask<Void, Void, String> {

    private Context mContext;
    private String userName;
    private String passWord;
    private Activity referencingActivity;

    public LoginTask(Activity ref, String username, String password,
                     Context ctx) {
        this.referencingActivity = ref;
        mContext = ctx;
        userName = username;
        passWord = password;
    }

    protected static void sendDeviceInfo(Context mContext, String username){
        //firebase key limitation
        username = username.replace(".", ",");
        username = username.replace("$", "");
        username = username.replace("\\[", "");
        username = username.replace("\\]", "");
        username = username.replace("#", "");
        username = username.replace("/", "");

        Log.d("username_",username);

        FirebaseApp.initializeApp(mContext);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("meili");

        ActivityManager actManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }
        String totalMemory = "NA";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            totalMemory = Variables.formatSize(memInfo.totalMem);
        }

        SensorManager oSM = (SensorManager) mContext.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> accelerometer = null;
        if (oSM != null) {
            accelerometer = oSM.getSensorList(Sensor.TYPE_ACCELEROMETER);
        }

//        Log.d("device_info", Build.HARDWARE+"\n"//mt6592
//                + Build.HOST+"\n"//rlk-buildsrv49
//                + Build.ID+"\n"//LMY471
//                + Build.MANUFACTURER+"\n"//INFINIX
//                + Build.MODEL+"\n"//INFINIX X-551
//                + Build.PRODUCT+"\n"//INFINIX X-551
//                + Build.VERSION.RELEASE+"\n"//5.1
//                + Build.VERSION.SDK_INT+"\n"//22
//                + totalMemory+"\n"
//                + Variables.getAvailableInternalMemorySize()+"/"+Variables.getTotalInternalMemorySize()+"\n"
//                + Variables.getAvailableExternalMemorySize()+"/"+Variables.getTotalExternalMemorySize()+"\n"
//                + accelerometer.toString()+"\n"
//        );
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("HARDWARE")
                    .setValue(Build.HARDWARE);
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("MANUFACTURER")
                    .setValue(Build.MANUFACTURER);
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("MODEL")
                    .setValue(Build.MODEL);
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("PRODUCT")
                    .setValue(Build.PRODUCT);
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("MEMORY")
                    .setValue(totalMemory);
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("INTERNAL_STORAGE")
                    .setValue(Variables.getTotalInternalMemorySize());
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("EXTERNAL_STORAGE")
                    .setValue(Variables.getTotalExternalMemorySize());
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            String baseOs = "NA";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                baseOs = Build.VERSION.BASE_OS;
            }
            mDatabase.child(username)
                    .child("Info")
                    .child("ANDROID")
                    .child("BASE_OS")
                    .setValue(baseOs);
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("ANDROID")
                    .child("RELEASE")
                    .setValue(Build.VERSION.RELEASE);
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("ANDROID")
                    .child("SDK")
                    .setValue(Build.VERSION.SDK_INT);
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("ACCELEROMETER")
                    .child("NAME")
                    .setValue(accelerometer.get(0).getName());
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("ACCELEROMETER")
                    .child("VENDOR")
                    .setValue(accelerometer.get(0).getVendor());
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("ACCELEROMETER")
                    .child("VERSION")
                    .setValue(accelerometer.get(0).getVersion());
        } catch (Exception e) {
            Timber.d(e);
        }
        try {
            mDatabase.child(username)
                    .child("Info")
                    .child("ACCELEROMETER")
                    .child("TYPE")
                    .setValue(accelerometer.get(0).getType());
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    protected String doInBackground(Void... params) {
        URL url;
        try {
            String urlString = PreferencesManager.getInstance().get(PreferencesAPI.KEY_SERVER_URL) +
                    Variables.userUrl + Variables.userLoginEndpoint;

            url = new URL(urlString);

            Timber.d("Connecting to %s", url.toString());

            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setUseCaches(false);
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConn.setRequestProperty("Charset", "utf-8");
            urlConn.connect();

            DataOutputStream dop = new DataOutputStream(urlConn.getOutputStream());
            dop.writeBytes("method=" + URLEncoder.encode("login", "utf-8"));
            dop.writeBytes("&username=" + URLEncoder.encode(userName, "utf-8"));
            dop.writeBytes("&password=" + URLEncoder.encode(passWord, "utf-8"));
            dop.flush();
            dop.close();

            BufferedReader dis = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String locPassage = dis.readLine();
            String read = locPassage;
            while (read != null) {
                Timber.d("Login Message : %s", read);
                read = dis.readLine();
            }
            dis.close();
            urlConn.disconnect();
            return locPassage;
        } catch (MalformedURLException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        }
        return "Failed";
    }

    protected void onPostExecute(String result) {
        Timber.d("Login Result : %s", result);
        String uid, token = "";
        try {
            JSONArray jArray = new JSONArray(result);
            uid = jArray.getJSONObject(0).getInt("id") + "";
            token = jArray.getJSONObject(0).getString("device_session");
        } catch (Exception e) {
            uid =  "Failed";
        }

        if (uid.equals("Failed")) {
            Toast.makeText(mContext, "Gagal Login. Informasi akun salah",
                    Toast.LENGTH_LONG).show();
        } else {
            AdministrativeDao adminDb = new AdministrativeDao(
                    Constants.databaseName, Constants.adminTable, mContext);

            adminDb.updateUserId(Integer.valueOf(uid.replace(" ", "")));
            adminDb.closeDb();

            try {
                Date date = new Date(System.currentTimeMillis());
                String formattedDate = SimpleDateFormat.getInstance().format(date);

                new SessionManager(referencingActivity).setUserSession(userName, passWord, token, formattedDate);

                Toast.makeText(mContext, "Login Berhasil", Toast.LENGTH_LONG)
                        .show();

                sendDeviceInfo(Meili.getInstance().getApplicationContext(), userName);
                syncPreferences(Meili.getInstance().getApplicationContext(), userName);

                this.referencingActivity.startActivity(new Intent(
                        this.referencingActivity.getApplicationContext(),
//                        ServiceHandlingActivity.class));
                        ServiceActivity.class));
                this.referencingActivity.finish();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Timber.e(e);
            }

        }
        super.onPostExecute(result);
    }

    protected static void syncPreferences(final Context mContext, String username){
        //firebase key limitation
        username = username.replace(".", ",");
        username = username.replace("$", "");
        username = username.replace("\\[", "");
        username = username.replace("\\]", "");
        username = username.replace("#", "");
        username = username.replace("/", "");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PreferencesAPI.KEY_USERNAME,username);
        editor.commit();


        FirebaseApp.initializeApp(mContext);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference meiliDB = ref
                .child("meili")
                .child(username)
                .child("preference");
        meiliDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = settings.edit();
                if(dataSnapshot.child(PreferencesAPI.KEY_MIN_ACCURACY).exists()){
                    editor.putString(PreferencesAPI.KEY_MIN_ACCURACY,dataSnapshot.child(PreferencesAPI.KEY_MIN_ACCURACY).getValue().toString());
                } else {
                    meiliDB.child(PreferencesAPI.KEY_MIN_ACCURACY)
                            .setValue(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_ACCURACY));
                }
                if(dataSnapshot.child(PreferencesAPI.KEY_MIN_DISTANCE).exists()){
                    editor.putString(PreferencesAPI.KEY_MIN_DISTANCE,dataSnapshot.child(PreferencesAPI.KEY_MIN_DISTANCE).getValue().toString());
                } else {
                    meiliDB.child(PreferencesAPI.KEY_MIN_DISTANCE)
                            .setValue(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_DISTANCE));
                }
                if(dataSnapshot.child(PreferencesAPI.KEY_MIN_TIME).exists()){
                    editor.putString(PreferencesAPI.KEY_MIN_TIME,dataSnapshot.child(PreferencesAPI.KEY_MIN_TIME).getValue().toString());
                } else {
                    meiliDB.child(PreferencesAPI.KEY_MIN_TIME)
                            .setValue(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_TIME));
                }
                if(dataSnapshot.child(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD).exists()){
                    editor.putString(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD,dataSnapshot.child(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD).getValue().toString());
                } else {
                    meiliDB.child(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD)
                            .setValue(PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD));
                }
                if(dataSnapshot.child(PreferencesAPI.KEY_ACCELEROMETER_PERIOD).exists()){
                    editor.putString(PreferencesAPI.KEY_ACCELEROMETER_PERIOD,dataSnapshot.child(PreferencesAPI.KEY_ACCELEROMETER_PERIOD).getValue().toString());
                } else {
                    meiliDB.child(PreferencesAPI.KEY_ACCELEROMETER_PERIOD)
                            .setValue(PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_PERIOD));
                }
                if(dataSnapshot.child(PreferencesAPI.KEY_ACCELEROMETER_SLEEP).exists()){
                    editor.putString(PreferencesAPI.KEY_ACCELEROMETER_SLEEP,dataSnapshot.child(PreferencesAPI.KEY_ACCELEROMETER_SLEEP).getValue().toString());
                } else {
                    meiliDB.child(PreferencesAPI.KEY_ACCELEROMETER_SLEEP)
                            .setValue(PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_SLEEP));
                }
                if(dataSnapshot.child(PreferencesAPI.KEY_RECORDING).exists()){
                    editor.putBoolean(PreferencesAPI.KEY_RECORDING,(Boolean)dataSnapshot.child(PreferencesAPI.KEY_RECORDING).getValue());
                } else {
                    meiliDB.child(PreferencesAPI.KEY_RECORDING)
                            .setValue(PreferencesManager.getInstance().get(PreferencesAPI.KEY_RECORDING));
                }
                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference globalDB = ref.child("global");
        globalDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = settings.edit();
                if(dataSnapshot.child(PreferencesAPI.KEY_SERVER_URL).exists()){
                    editor.putString(PreferencesAPI.KEY_SERVER_URL,dataSnapshot.child(PreferencesAPI.KEY_SERVER_URL).getValue().toString());
                }
                editor.commit();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
