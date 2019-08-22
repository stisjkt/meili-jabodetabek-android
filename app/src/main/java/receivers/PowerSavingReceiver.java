package receivers;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import constants.Constants;
import constants.PreferencesAPI;
import daos.AccelerometerDao;
import daos.AdministrativeDao;
import id.ac.stis.meili.R;
import listeners.EmbeddedLocationListener;
import models.AccelerometerModel;
import models.AccelerometerSimpleModel;
import models.ProcessedAccelerometerModel;
import preferences.PreferencesManager;
import services.CollectingService;
import timber.log.Timber;
import utilities.LoggingUtils;
import utilities.ServiceUtils;

public class PowerSavingReceiver extends BroadcastReceiver {

    private static final float ALPHA = 0.8f;

    public static PowerManager pm;
    public static PowerManager.WakeLock wl;

    //double last_acceleration, max_acceleration;
    float[] gravity;
    long number = 0;
    LinkedList<AccelerometerModel> accelerometerValues;

    private SensorEventListener mSensorListener;
    private SensorManager mSensorManager;

    public PowerSavingReceiver() {

    }

    public PowerSavingReceiver(long number) {
        this.number = number;
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public void onReceive(final Context context, Intent intent) {

        LoggingUtils.i(LoggingUtils.TAG_ALARM_RECEIVER, "Alarm Received");
        LoggingUtils.i(context, LoggingUtils.TAG_ALARM_RECEIVER, context.getString(R.string.tag_alarm_received));

        PowerSavingReceiver alarm = new PowerSavingReceiver(0);
        alarm.setAlarm(context);

        accelerometerValues = new LinkedList<>();
		
	/*	if (!CollectingService.secListening)
		EmbeddedLocationListener.getMove();*/

        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "Maybe it is working");
            wl.acquire();
        }

        if(this.number == 0){
            int paramsTime = Integer.parseInt(PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_SLEEP).toString());
            if (System.currentTimeMillis() - EmbeddedLocationListener.lastRecordedLocationTime > paramsTime) { //timeout
                LoggingUtils.i(LoggingUtils.TAG_ALARM_RECEIVER, "Location listener timeout");
                LoggingUtils.i(context, LoggingUtils.TAG_ALARM_RECEIVER, context.getString(R.string.tag_alarm_loc_timeout));
            } else {
                LoggingUtils.i(LoggingUtils.TAG_ALARM_RECEIVER, "Location listener is still recording");
                LoggingUtils.i(context, LoggingUtils.TAG_ALARM_RECEIVER, context.getString(R.string.tag_alarm_loc_recording));
                return;
            }
        }

        CollectingService.stopListening(context);

        gravity = new float[3];

        mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);

        mSensorListener = new SensorEventListener() {

            int count = 0;

            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {

            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                count++;

                float[] values = event.values.clone();
                values = lowPassFilter(values[0], values[1], values[2]);

                AccelerometerModel model = new AccelerometerModel(values, event.accuracy,
                        System.currentTimeMillis());

                if (count > 10) {
                    accelerometerValues.addLast(model);
                }

                if (count == 150) {
                    Timber.d("R: Sensor Count : 150");

                    ProcessedAccelerometerModel aV = new ProcessedAccelerometerModel(accelerometerValues);

                    LoggingUtils.i(LoggingUtils.TAG_ALARM_RECEIVER, "Accel. total mean: " + aV.getTotalMean());
                    LoggingUtils.i(context, LoggingUtils.TAG_ALARM_RECEIVER, context.getString(R.string.tag_alarm_total_mean));
                    if (aV.isTotalIsMoving()) {
                        LoggingUtils.i(LoggingUtils.TAG_ALARM_RECEIVER, "Device is Moving");
                        LoggingUtils.i(context, LoggingUtils.TAG_ALARM_RECEIVER, context.getString(R.string.tag_alarm_moving));
//                        mSensorManager.unregisterListener(mSensorListener);
//                        if (wl != null)
//                            if (wl.isHeld()) wl.release();
//                        try {
//                            cancelAlarm(thisContext);
//                        } catch (Exception e) {
//                            // TODO Auto-generated catch block
//                            Timber.e(e);
//                        }

                        /// REPLACE THIS WITH SERVICE RESTART LISTENING

                        // WORK ON THE LISTENER RATHER THAN ON THE SERVICE z
                        try {

                            //CollectingService.stopSecListening(thisContext);

                            CollectingService.startListening(context);

                        } catch (Exception e) {
                            try {
                                Timber.e(e);
                                CollectingService.stopListening(context);
                            } catch (Exception e2) {
                                Timber.e(e2);
                            }
                            Intent collectionServiceIntent = new Intent(
                                    CollectingService.class.getName());
                            //thisContext.startService(collectionServiceIntent);
                            ServiceUtils.startCollectService(context, collectionServiceIntent);
                            Timber.e(e);
                        }
                    } else {

                        LoggingUtils.i(LoggingUtils.TAG_ALARM_RECEIVER, "Device is NOT Moving");
                        LoggingUtils.i(context, LoggingUtils.TAG_ALARM_RECEIVER, context.getString(R.string.tag_alarm_not_moving));

                        try {
                            CollectingService.stopListening(context);
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                        PowerSavingReceiver alarm = new PowerSavingReceiver(0);
                        alarm.setAlarm(context);
                    }

                    mSensorManager.unregisterListener(mSensorListener);
                    try {
                        if (wl != null) {
                            if (wl.isHeld()) {
                                wl.release();
                            }
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Timber.e(e);
                    }

                    // KENE
                    if ((Boolean) PreferencesManager.getInstance().get(PreferencesAPI.KEY_RECORD_ACCELEROMETER)) {
                        AccelerometerDao accelerometerDao = new AccelerometerDao(Constants.databaseName,
                                Constants.accelerometerTable, context);
                        AdministrativeDao administrativeDao = new AdministrativeDao(Constants.databaseName,
                                Constants.adminTable, context);

                        AccelerometerModel currAcc = accelerometerValues.getLast();

                        accelerometerDao.insertAccelerometerIntoDb(new AccelerometerSimpleModel(
                                administrativeDao.getUserId(), currAcc.values[0], aV.getxMin(), aV.getxMax(),
                                aV.getxMean(), currAcc.values[1], aV.getyMin(), aV.getyMax(),
                                aV.getyMean(), currAcc.values[2], aV.getzMin(), aV.getzMax(),
                                aV.getzMean(), currAcc.timestamp));
                        administrativeDao.closeDb();
                        accelerometerDao.closeDb();
                    }

                }
            }
        };

        EmbeddedLocationListener.getMove();

        /*boolean isMoving = EmbeddedLocationListener.isMoving;
        if (isMoving) {
            LoggingUtils.i(context, LoggingUtils.TAG_ACCELEROMETER, "Device is moving by steps");
            CollectingService.startSecondaryListening(thisContext);
        } else {
            LoggingUtils.i(context, LoggingUtils.TAG_ACCELEROMETER, "Device is NOT moving by steps");
        }*/
		/*	if (EmbeddedLocationListener.isMoving)
			{
				try {
					CollectingService.stopListening(thisContext);
					CollectingService.startSecondaryListening(thisContext);
					cancelAlarm(thisContext);
				} catch (Exception e) {
					CollectingService.stopSecListening(thisContext);
					mSensorManager.registerListener(mSensorListener,
							mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
							SensorManager.SENSOR_DELAY_NORMAL);

				}
			}
			else*/
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private float[] lowPassFilter(float x, float y, float z) {

        float[] filteredValues = new float[3];

        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];

        return filteredValues;
    }

    /**
     * The alarm gets set from within in order to be able to periodically check
     * for motion
     *
     * @param context this class context
     */
    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, PowerSavingReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        int paramsTime = Integer.parseInt(PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_PERIOD).toString());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, paramsTime / 1000);

        long alarmTime = cal.getTimeInMillis();
        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            }
        }

        LoggingUtils.i(LoggingUtils.TAG_ALARM_MANAGER, "Alarm Set at " + cal.getTime());
        LoggingUtils.i(context, LoggingUtils.TAG_ALARM_MANAGER, context.getString(R.string.tag_alarm_set),
                String.valueOf(cal.getTimeInMillis()));
    }

    /**
     * The alarm is set from outside and handled by the location listener
     *
     * @param context this context
     * @param outside called from outside class
     */
    public void setAlarm(Context context, boolean outside) {

        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, PowerSavingReceiver.class);
		
		/*//EmbeddedLocationListener.getMove();
		i.putExtra("move", EmbeddedLocationListener.isMoving);
		Timber.d("Is Moving : %s", EmbeddedLocationListener.isMoving);*/

        PendingIntent pi = PendingIntent.getBroadcast(context, 2, i, 0);

        int paramsTime = Integer.parseInt(PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_SLEEP).toString());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, paramsTime / 1000);

        long alarmTime = cal.getTimeInMillis();

        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            }
        }

        LoggingUtils.i(LoggingUtils.TAG_ALARM_MANAGER, "Alarm Set at " + cal.getTime());
        ArrayList<String> al = new ArrayList<>();
        al.add(String.valueOf(new Date(alarmTime).getTime()));
        LoggingUtils.i(context, LoggingUtils.TAG_ALARM_MANAGER, context.getString(R.string.tag_alarm_set),
                String.valueOf(cal.getTimeInMillis()));

    }

    /**
     * Cancels the alarm from within
     *
     * @param context this class context
     */
    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, PowerSavingReceiver.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(sender);
        }

		/*try {
			wl.release();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Timber.e(e);
		}*/
    }

    /**
     * Cancels the alarm by the listener
     *
     * @param context this app context
     * @param time the time
     */
    public void cancelAlarm(Context context, boolean time) {
        Intent intent = new Intent(context, PowerSavingReceiver.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 2, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(sender);
        }
		/*try {
			wl.release();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Timber.e(e);
		}*/
    }
}
