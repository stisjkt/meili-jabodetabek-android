package services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import activities.ServiceActivity;
import constants.Constants;
import constants.PreferencesAPI;
import constants.SessionManagerAPI;
import constants.Variables;
import id.ac.stis.meili.R;
import listeners.EmbeddedLocationListener;
import preferences.PreferencesManager;
import preferences.SessionManager;
import tasks.UploadTask;
import timber.log.Timber;
import utilities.GetInfo;
import utilities.LoggingUtils;

public class CollectingService extends Service {

    public static EmbeddedLocationListener mListener;

    public static boolean secListening = false;

    public GetInfo gI;
    private Timer timer;
    private Timer garbageTimer;

    public static void startListening(Context ctx) {
        Timber.d("Start listening on null ? %s", (mListener == null));
        if (mListener == null) {
            mListener = new EmbeddedLocationListener(ctx,
                    Long.parseLong(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_TIME).toString()),
                    Long.parseLong(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_DISTANCE).toString()));
        }
        mListener.startListening();
    }

    public static void stopListening(Context ctx) {

//        Timber.d("stop listening on null ? %s" + (mListener == null));
        Timber.d("Stop Listening on Null ? %s", (mListener == null));

        if (mListener != null)
        /*mListener = new EmbeddedLocationListener(ctx,
                Variables.samplingMinTime, Variables.samplingMinDistance);
		//if (mListener!=null)*/
            mListener.stopListening();
    }

    public static void startSecondaryListening(Context ctx) {
        if (mListener != null) {
            /*mListener = new EmbeddedLocationListener(ctx,
                    Variables.samplingMinTime, Variables.samplingMinDistance);*/

            mListener.stopListening();
            mListener.startSecListening();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        changePreference(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        stopListening(this);
        mListener.stopAlarm();
        timer.cancel();
        try {
            garbageTimer.cancel();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Timber.e(e);
        }
        gI.setServiceOff();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gI = new GetInfo(this);
        gI.setServiceOn();

        mListener = new EmbeddedLocationListener(this,
                Long.parseLong(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_TIME).toString()),
                Long.parseLong(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_DISTANCE).toString()));

        startListening(this);

        scheduleAlarmForDailyRefresh(this);

        autoUploading(this);

        showNotification();

//        @SuppressWarnings("unused")
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        return START_STICKY;
    }

	/*public static void stopSecListening(Context ctx) {
        secListening=false;
			mSecListener.stopSecListening();
	}*/

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

    private void scheduleAlarmForDailyRefresh(Context ctx) {

        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.YEAR, cur_cal.get(Calendar.YEAR));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cur_cal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cur_cal.get(Calendar.MILLISECOND));
        cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
        cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));
        cal.add(Calendar.MINUTE, 120);

        garbageTimer = new Timer();
        garbageTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Timber.d("I ran at %s", System.currentTimeMillis());

                mListener.restartForGarbageCollector();
                onStartCommand(null, 0, START_STICKY);
            }
        }, cal.getTime());

        Timber.d("Set the alarm at %s", cal.toString());

    }

    public void autoUploading(final Context ctx) {
        UploadTask uploadTask = new UploadTask(ctx);
        uploadTask.execute();

        timer = new Timer();
        if (Variables.isAutoUpload == 1) {
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            if (gI.isOnline()) {
                                try {
                                    UploadTask uploadTask = new UploadTask(ctx);
                                    uploadTask.execute();
                                } catch (Exception e) {
                                    Timber.e(e);
                                }
                            }
                        }
                    }, 5 * 60 * 1000, (long) (60 * 1000 *
                            Double.parseDouble(PreferencesManager.getInstance().get(PreferencesAPI.KEY_AUTO_UPLOAD).toString())));
        } else {
            timer.cancel();
        }
    }


    private void changePreference(final Context mContext){
        final String username = PreferencesManager.getInstance().get(PreferencesAPI.KEY_USERNAME).toString();

        FirebaseApp.initializeApp(mContext);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference meiliDB = ref
                .child("meili")
                .child(username)
                .child("preference");
        meiliDB.addChildEventListener(new ChildEventListener() {
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
                if(dataSnapshot.getValue() instanceof Boolean && dataSnapshot.getKey().equals(PreferencesAPI.KEY_RECORDING)){
                    if(!(Boolean)dataSnapshot.getValue()){
                        clearApplicationData(mContext);
                        System.exit(0);
                    }
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

        stopService(new Intent(mContext,CollectingService.class));
    }

    void showNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "meili_notif_channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Default Notification Channel");
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        //		Intent interactionActivity = new Intent(this, ServiceHandlingActivity.class);
        Intent interactionActivity = new Intent(this, ServiceActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                interactionActivity, 0);

        Notification noti = new NotificationCompat.Builder(this, "default")
                .setContentTitle(Constants.notificationTitle)
                .setContentText(Constants.titleText)
                .setSmallIcon(R.drawable.ic_mobility)
                .setContentIntent(pIntent).build();

//        mNotificationManager.notify(0, noti);

        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        startForeground(1, noti);
    }

}
