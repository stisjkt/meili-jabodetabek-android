package activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import applications.Meili;
import constants.Constants;
import constants.PreferencesAPI;
import constants.SessionManagerAPI;
import constants.Variables;
import daos.AccelerometerDao;
import daos.LocationAccelerationDao;
import id.ac.stis.meili.R;
import listeners.OnLogAppendedListener;
import models.LogModel;
import preferences.PreferencesManager;
import preferences.SessionManager;
import receivers.LoggingReceiver;
import services.CollectingService;
import tasks.UploadTask;
import timber.log.Timber;
import utilities.LoggingUtils;
import utilities.ServiceUtils;

/**
 * Created by Rahadi on 15/03/2018.
 */

public class ServiceActivity extends Activity implements OnLogAppendedListener, UploadTask.OnUploadCompleted {

    private Class serviceClass;
    private LoggingReceiver loggingReceiver;
    private HashMap<String, String> userSession;
    private Intent collectingServiceIntent;
    private TextView username, date, db, annotate, preferences,
            upload, status, acc, accTitle, accColon, version, guidance;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsClient mCustomTabsClient;
    private CustomTabsServiceConnection mConnection;
    private static final String TAG = "CustomTabsClientExample";
    private static final String TOOLBAR_COLOR = "#ef6c00";
    public static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";  // Change when in stable
//    private ListView logList;
//    private LogAdapter logAdapter;
//    private List<LogModel> logModels;
//    private View logHolder;
private Activity activity;

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (String aChildren : children) {
                    deletedAll = deleteFile(new File(file, aChildren)) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (loggingReceiver == null) {
            loggingReceiver = new LoggingReceiver();
        }

        loggingReceiver.setOnLogAppendedListener(this);

//        loggingServiceIntent = new Intent(this, LoggingService.class);

        db.setText(getDbStatus());

        if (Boolean.valueOf(PreferencesManager.getInstance().get(PreferencesAPI.KEY_RECORD_ACCELEROMETER).toString())) {
            accTitle.setVisibility(View.VISIBLE);
            accColon.setVisibility(View.VISIBLE);
            acc.setVisibility(View.VISIBLE);

            acc.setText(getAccStatus());
        } else {
            accTitle.setVisibility(View.GONE);
            accColon.setVisibility(View.GONE);
            acc.setVisibility(View.GONE);
        }

        toggleCollectingService(true);
        setStatusText();

//        Timer t = new Timer();
//        t.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                LoggingUtils.v(LoggingUtils.TAG_SERVICE, "Yuhuu..");
//            }
//        }, 0, 1000);

        //loggingReceiverIntent = registerReceiver(loggingReceiver, new IntentFilter(Variables.logFilter));
//        startService(loggingServiceIntent);

//        if ((Boolean) PreferencesManager.getInstance().get(PreferencesAPI.KEY_SHOW_LOGS)) {
//
//            List<LogModel> list = LoggingUtils.readLogToModelList();
//
//            logModels.clear();
//            logModels.addAll(list);
//            logAdapter.notifyDataSetChanged();
//
//            logHolder.setVisibility(View.VISIBLE);
//        } else {
//            logHolder.setVisibility(View.GONE);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (loggingReceiverIntent != null) {
//            unregisterReceiver(loggingReceiver);
//        }
//        if ((Boolean) PreferencesManager.getInstance().get(PreferencesAPI.KEY_SHOW_LOGS)) {
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PreferencesActivity.REQUEST_SETTINGS) {
            if (resultCode == PreferencesActivity.RESULT_SETTINGS_CHANGED) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("The following Preferences are changed: \n\n");

                HashMap<String, Object> changedPreferences = (HashMap<String, Object>)
                        data.getSerializableExtra(PreferencesActivity.RESULT_SETTINGS_LIST);

                Set<String> keys = changedPreferences.keySet();
                for (String key : keys) {
                    stringBuilder.append(key);
                    stringBuilder.append(" -> ");
                    stringBuilder.append(PreferencesManager.getInstance().get(key).toString());
                    stringBuilder.append("\n");
                }

                stringBuilder.append("\nThe service will be restarted.");

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false)
                        .setTitle("Preferences Changed")
                        .setMessage(stringBuilder)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                restartService();
                            }
                        });

                builder.create().show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        serviceClass = CollectingService.class;
        collectingServiceIntent = new Intent(this, serviceClass);
        ServiceUtils.startCollectService(this, collectingServiceIntent);


        setContentView(R.layout.activity_service);

        username = findViewById(R.id.text_username);
        date = findViewById(R.id.text_date);
        db = findViewById(R.id.text_db);
//        dbreload = (TextView) findViewById(R.id.text_refresh);
        upload = findViewById(R.id.text_upload);
        annotate = findViewById(R.id.text_annotate);
        preferences = findViewById(R.id.text_preferences);
        guidance = findViewById(R.id.text_guidance);
        status = findViewById(R.id.text_status);
        acc = findViewById(R.id.text_acc);
        accTitle = findViewById(R.id.accelerometer_title);
        accColon = findViewById(R.id.accelerometer_colon);
//        logHolder = findViewById(R.id.logs_holder);
//        logList = (ListView) findViewById(R.id.list_logs);
//
//        logModels = new LinkedList<LogModel>();
//        logAdapter = new LogAdapter(this, logModels);
//        logList.setAdapter(logAdapter);
        version = findViewById(R.id.version);

        preferences.setVisibility(View.GONE);

        userSession = new SessionManager(ServiceActivity.this).getUserSession();

        username.setText(userSession.get(SessionManagerAPI.Keys.KEY_USERNAME));
        date.setText(getString(R.string.status_date_format, userSession.get(SessionManagerAPI.Keys.KEY_DATE)));

        syncFirebase(getApplicationContext());

//        dbreload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                db.setText(getDbStatus());
//                Toast.makeText(ServiceActivity.this, R.string.reload_completed, Toast.LENGTH_LONG).show();
//            }
//        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Meili.getInstance().isUploading()) {
                    upload.setEnabled(false);
                    upload.setText(R.string.uploading);
                    UploadTask uploadTask = new UploadTask(ServiceActivity.this);
                    uploadTask.setListener(ServiceActivity.this);

                    uploadTask.execute();
                }
            }
        });

        annotate.setOnClickListener(onClickAnnotate());

        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ServiceActivity.this, PreferencesActivity.class);
                startActivityForResult(i, PreferencesActivity.REQUEST_SETTINGS);
            }
        });

        guidance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = PreferencesAPI.KEY_SERVER_URL + getString(R.string.guidance_url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        try {
            version.setText("version " + getCurrentApplicationVersionName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setStatusText() {
        date.setText(getString(R.string.status_date_format, new SessionManager(this)
                .getUserSession().get(SessionManagerAPI.Keys.KEY_DATE)));

        if (ServiceUtils.isMyServiceRunning(this, serviceClass)) {
            status.setText(R.string.status_running);
            status.setTextColor(getResources().getColor(R.color.green));
        } else {
            status.setText(R.string.status_stopped);
            status.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private boolean toggleCollectingService(boolean toggleOn) {
        if (toggleOn) {
            if (!ServiceUtils.isMyServiceRunning(this, serviceClass)) {
                try {
                    if (passGPSFilter()) {
                        ServiceUtils.startCollectService(this,collectingServiceIntent);
                        new SessionManager(this).updateDate();
                        LoggingUtils.i(getApplicationContext(), LoggingUtils.TAG_SERVICE,
                                getString(R.string.tag_service_off), getString(R.string.tag_service_success));
                    } else {
                        buildGpsAlarm();
                    }

                    return true;
                } catch (Exception e) {
                    new SessionManager(this).updateDate();
                    LoggingUtils.i(getApplicationContext(), LoggingUtils.TAG_SERVICE,
                            getString(R.string.tag_service_off), getString(R.string.tag_service_failed_on), e.getMessage());
                    return false;
                }
            }
        } else {
            try {
                if (ServiceUtils.isMyServiceRunning(this, serviceClass)) {
                    LoggingUtils.i(LoggingUtils.TAG_SERVICE, "Service is On. Turning Off..");
                    stopService(collectingServiceIntent);
                    new SessionManager(this).updateDate();
                    LoggingUtils.i(getApplicationContext(), LoggingUtils.TAG_SERVICE, getString(R.string.tag_service_success), getString(R.string.tag_service_off));
                }
                return true;
            } catch (Exception e) {
                Timber.e(e);
                new SessionManager(this).updateDate();
                LoggingUtils.e(LoggingUtils.TAG_SERVICE, "Failed to Turn Off the Service.");
                LoggingUtils.i(getApplicationContext(), LoggingUtils.TAG_SERVICE, getString(R.string.tag_service_on), getString(R.string.tag_service_failed_off));
                return false;
            }
        }

        return false;
    }

    private String getDbStatus() {
        LocationAccelerationDao locationDb = new LocationAccelerationDao(
                Constants.databaseName, Constants.locationTable,
                this);

        int toUpload = locationDb.getLocationsForUploadFromDatabase(-1).size();
        int total = locationDb.getAllLocationsFromDatabase().size();
        locationDb.closeDb();
        StringBuilder builder = new StringBuilder();
        builder.append(total).append(" ").append(getString(R.string.recorded));
        if (toUpload > 0) {
            builder.append(", ").append(toUpload).append(" ").append(getString(R.string.to_upload));
        }

        return builder.toString();
    }

    private String getAccStatus() {
        AccelerometerDao accelerometerDao = new AccelerometerDao(
                Constants.databaseName, Constants.accelerometerTable, this);

        int toUpload = accelerometerDao.getAccelerometersForUploadFromDatabase(-1).size();
        int total = accelerometerDao.getAllAccelerometersFromDatabase().size();
        accelerometerDao.closeDb();

        StringBuilder builder = new StringBuilder();
        builder.append(total).append(" ").append(getString(R.string.recorded));
        if (toUpload > 0) {
            builder.append(", ").append(toUpload).append(" ").append(getString(R.string.to_upload));
        }

        return builder.toString();
    }

    private boolean passGPSFilter() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            return locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }

    private void restartService() {
        status.setText(R.string.status_restarting);
        status.setTextColor(getResources().getColor(R.color.yellow));

        boolean stopped = toggleCollectingService(false);
        if (stopped) {
            toggleCollectingService(true);
        }

        setStatusText();
    }

    @Override
    public void onLogAppended(List<LogModel> list) {
//        if ((Boolean) PreferencesManager.getInstance().get(PreferencesAPI.KEY_SHOW_LOGS)) {
//            logModels.clear();
//            logModels.addAll(list);
//            logAdapter.notifyDataSetChanged();
//        }

        db.setText(getDbStatus());

        if (Boolean.valueOf(PreferencesManager.getInstance().get(PreferencesAPI.KEY_RECORD_ACCELEROMETER).toString())) {
            accTitle.setVisibility(View.VISIBLE);
            accColon.setVisibility(View.VISIBLE);
            acc.setVisibility(View.VISIBLE);

            acc.setText(getAccStatus());
        } else {
            accTitle.setVisibility(View.GONE);
            accColon.setVisibility(View.GONE);
            acc.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUploadCompleted(int status) {
        upload.setEnabled(true);
        upload.setText(R.string.manual_upload);

        if (!(Boolean) PreferencesManager.getInstance().get(PreferencesAPI.KEY_SHOW_LOGS)) {
            if (status == UploadTask.STATUS_SUCCESS) {
                Toast.makeText(ServiceActivity.this, Constants.confirmUpload, Toast.LENGTH_LONG).show();
            } else if (status == UploadTask.STATUS_NO_DATA) {
                Toast.makeText(ServiceActivity.this, Constants.confirmUpload, Toast.LENGTH_LONG).show();
            } else if (status == UploadTask.STATUS_ERROR) {
                Toast.makeText(ServiceActivity.this, Constants.infirmUpload, Toast.LENGTH_LONG).show();
            }
        }
        db.setText(getDbStatus());
    }

    public String getCurrentApplicationVersionName() throws PackageManager.NameNotFoundException {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName;
    }

    public boolean isPackageExisted(String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
    int getWebviewVersionInfo() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo("com.google.android.webview", 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private void buildGpsAlarm() {
        final AlertDialog.Builder alert2 = new AlertDialog.Builder(
                ServiceActivity.this);
        alert2.setMessage(Constants.enableGPSBody);
        alert2.setTitle(Constants.enableGPSTitle);

        alert2.setPositiveButton(Constants.yesText,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Intent callGPSSettingIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });

        alert2.setNegativeButton(Constants.noText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
        AlertDialog dialog2 = alert2.create();
        dialog2.show();
    }

    public void clearApplicationData(Context mContext) {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }

        stopService(new Intent(mContext, CollectingService.class));
    }

    private void syncFirebase(final Context mContext){
        String username = PreferencesManager.getInstance().get(PreferencesAPI.KEY_USERNAME).toString();

        FirebaseApp.initializeApp(mContext);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference meiliDB = ref
                .child("meili")
                .child(username)
                .child("preference");
        meiliDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(PreferencesAPI.KEY_RECORDING).exists()) {
                    if (!(Boolean) dataSnapshot.child(PreferencesAPI.KEY_RECORDING).getValue()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            activity.finishAndRemoveTask();
                            clearApplicationData(getApplicationContext());
                            System.exit(0);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            activity.finishAffinity();
                            clearApplicationData(getApplicationContext());
                            System.exit(0);
                        } else {
                            activity.finish();
                            clearApplicationData(getApplicationContext());
                            System.exit(0);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        meiliDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = settings.edit();
                if (dataSnapshot.getValue() == null || dataSnapshot.getValue() instanceof String) {
                    editor.putString(dataSnapshot.getKey(),String.valueOf(dataSnapshot.getValue()));
                } else if (dataSnapshot.getValue() instanceof Boolean) {
                    editor.putBoolean(dataSnapshot.getKey(),(Boolean) dataSnapshot.getValue());
                } else if (dataSnapshot.getValue() instanceof Long) {
                    editor.putLong(dataSnapshot.getKey(),(Long) dataSnapshot.getValue());
                } else if (dataSnapshot.getValue() instanceof Integer) {
                    editor.putInt(dataSnapshot.getKey(),(Integer) dataSnapshot.getValue());
                } else if (dataSnapshot.getValue() instanceof Float) {
                    editor.putFloat(dataSnapshot.getKey(),(Float) dataSnapshot.getValue());
                }
                editor.commit();

                if(dataSnapshot.getValue() instanceof Boolean && dataSnapshot.getKey().equals(PreferencesAPI.KEY_RECORDING)){
                    if(!(Boolean)dataSnapshot.getValue()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            activity.finishAndRemoveTask();
                            clearApplicationData(getApplicationContext());
                            System.exit(0);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            activity.finishAffinity();
                            clearApplicationData(getApplicationContext());
                            System.exit(0);
                        } else {
                            activity.finish();
                            clearApplicationData(getApplicationContext());
                            System.exit(0);
                        }
                    }
                }else if(dataSnapshot.getKey().equals(PreferencesAPI.KEY_SERVER_URL)){
                    startActivity(new Intent(getApplicationContext(),ServiceActivity.class));
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference globalDB = ref.child("global");
        globalDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = settings.edit();
                if (dataSnapshot.getValue() == null || dataSnapshot.getValue() instanceof String) {
                    editor.putString(dataSnapshot.getKey(),String.valueOf(dataSnapshot.getValue()));
                } else if (dataSnapshot.getValue() instanceof Boolean) {
                    editor.putBoolean(dataSnapshot.getKey(),(Boolean) dataSnapshot.getValue());
                } else if (dataSnapshot.getValue() instanceof Long) {
                    editor.putLong(dataSnapshot.getKey(),(Long) dataSnapshot.getValue());
                } else if (dataSnapshot.getValue() instanceof Integer) {
                    editor.putInt(dataSnapshot.getKey(),(Integer) dataSnapshot.getValue());
                } else if (dataSnapshot.getValue() instanceof Float) {
                    editor.putFloat(dataSnapshot.getKey(),(Float) dataSnapshot.getValue());
                }
                editor.commit();

                Toast.makeText(mContext,dataSnapshot.getKey()+" changed to "+String.valueOf(PreferencesManager.getInstance().get(dataSnapshot.getKey())),Toast.LENGTH_LONG).show();
                //Log to Storage
                LoggingUtils.i(LoggingUtils.TAG_PREFERENCES_CHANGED,dataSnapshot.getKey()+" changed to "+PreferencesManager.getInstance().get(dataSnapshot.getKey()));


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private View.OnClickListener onClickAnnotate(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!Meili.getInstance().isUploading()) {
                    upload.setEnabled(false);
                    upload.setText(R.string.uploading);
                    UploadTask uploadTask = new UploadTask(ServiceActivity.this);
                    uploadTask.setListener(ServiceActivity.this);

                    uploadTask.execute();
                }
                if (isPackageExisted(CUSTOM_TAB_PACKAGE_NAME)) {
                      /*CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                      CustomTabsIntent customTabsIntent = builder.build();
                      customTabsIntent.launchUrl(ServiceActivity.this, Uri.parse(url));*/


                    CustomTabsServiceConnection mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
                        @Override
                        public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                            mCustomTabsClient = customTabsClient;
                            mCustomTabsClient.warmup(0L);
                            mCustomTabsSession = mCustomTabsClient.newSession(null);
                            //ServiceActivity.this.getApplicationContext().unbindService(this);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            mCustomTabsClient = null;
                        }
                    };

//                    String postData;
//                    Bundle extras = new Bundle();
//                    try {
//                        SessionManager sm = new SessionManager(getApplicationContext());
//                        String username1 = sm.getUserSession().get(SessionManagerAPI.Keys.KEY_USERNAME);
//                        String password = sm.getUserSession().get(SessionManagerAPI.Keys.KEY_PASSWORD);
////                        postData = "username=" + URLEncoder.encode(username1, "UTF-8")
////                                + "&password=" + URLEncoder.encode(password, "UTF-8");
//                        extras.putString("username", URLEncoder.encode(username1, "UTF-8"));
//                        extras.putString("password", URLEncoder.encode(password, "UTF-8"));
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }

                    CustomTabsClient.bindCustomTabsService(ServiceActivity.this, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);

                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(mCustomTabsSession);
                    CustomTabsIntent mCustomTabsIntent = builder
                            .setShowTitle(false)
                            .build();

                    mCustomTabsIntent.intent.setPackage(CUSTOM_TAB_PACKAGE_NAME);
                    mCustomTabsIntent.intent.putExtra("org.chromium.chrome.browser.customtabs.EXTRA_DISABLE_STAR_BUTTON",true);
                    mCustomTabsIntent.intent.putExtra("org.chromium.chrome.browser.customtabs.EXTRA_DISABLE_DOWNLOAD_BUTTON",true);
                    SessionManager sm = new SessionManager(getApplicationContext());

                    mCustomTabsIntent.launchUrl(ServiceActivity.this, Uri.parse(
                            PreferencesManager.getInstance().get(PreferencesAPI.KEY_SERVER_URL).toString()
                                    + Variables.loginChrome + "?token=" + sm.getUserSession().get(SessionManagerAPI.Keys.KEY_TOKEN)));
                } else if (getWebviewVersionInfo() > 320201) {
                    startActivity(new Intent(ServiceActivity.this, AnnotationActivity.class));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ServiceActivity.this);
                    builder.setCancelable(true);
                    builder.setTitle("Peringatan!!!");
                    builder.setMessage("Silahkan install Google Chrome untuk melanjutkan annotasi. Klik lanjut untuk menginstall.");
                    builder.setPositiveButton("Lanjut",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + CUSTOM_TAB_PACKAGE_NAME)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + CUSTOM_TAB_PACKAGE_NAME)));
                                    }
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        };
    }

}
