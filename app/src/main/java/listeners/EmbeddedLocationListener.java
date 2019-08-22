package listeners;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;

import applications.Meili;
import constants.Constants;
import constants.PreferencesAPI;
import constants.Variables;
import daos.AdministrativeDao;
import daos.LocationAccelerationDao;
import daos.LocationSimpleDao;
import id.ac.stis.meili.R;
import models.AccelerometerModel;
import models.EmbeddedLocationModel;
import models.LocationModel;
import models.ProcessedAccelerometerModel;
import preferences.PreferencesManager;
import receivers.PowerSavingReceiver;
import timber.log.Timber;
import utilities.EquidistanceTracking;
import utilities.LoggingUtils;

public class EmbeddedLocationListener {

    private static final float ALPHA = 0.8f;
    private static PowerManager pm;
    private static PowerManager.WakeLock wl;
    private static PowerManager.WakeLock serviceWakeLock;
    private static boolean serviceIsStarted = false;
    private static boolean isMoving;
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private static LocationListener secLocationListener;
    private static Location prevLocation;
    private static EmbeddedLocationModel currentEmbeddedLocationModel;
    private static LinkedList<AccelerometerModel> accelerometerValues = new LinkedList<>();
    private static long timeFrequency;
    private static float distanceFrequency;
    private static float[] gravity;
    private Context mContext;
    //LinkedList<EmbeddedLocationModel> noiseTestList;
    private PowerSavingReceiver powerAlarm;
    private LocationAccelerationDao locationDatabase;
    private LocationSimpleDao locationDatabaseSimple;
    public static long lastRecordedLocationTime = 0;

    /*
     * private static boolean isAccelerometerOn = false; private static boolean
     * isRunning = false;
     */
    private AdministrativeDao adminDb;
    private int userId;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    private Sensor mSensor;
    private boolean skipOneLocation = false;

    public EmbeddedLocationListener(final Context ctx, long timeFreq, float distFreq) {
        mContext = ctx;
        timeFrequency = timeFreq;
        distanceFrequency = distFreq;
        gravity = new float[3];

        locationDatabase = new LocationAccelerationDao(
                Constants.databaseName, Constants.locationTable, mContext);
        locationDatabaseSimple = new LocationSimpleDao(
                Constants.databaseName, Constants.simpleLocationTable, mContext);
        adminDb = new AdministrativeDao(Constants.databaseName,
                Constants.adminTable, mContext);

        locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(
                        mContext,
                        Constants.deactivateGPSWarning,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLocationChanged(Location location) {
                lastRecordedLocationTime = System.currentTimeMillis();
                LoggingUtils.i(LoggingUtils.TAG_GPS, "New location detected");
                LoggingUtils.i(mContext, LoggingUtils.TAG_GPS, ctx.getString(R.string.tag_gps_detected));
                releaseServiceWakeLock();

                stopSecListening();

                if (!skipOneLocation) {
                    // check that the user id is in its correct instance

                    if (userId == 0) {
                        userId = adminDb.getUserId();
                    }
                    /*
                     * this also resets the accelerometer values
                     */

                    if (Variables.isPowerSavingOn) {
                        //	if (powerAlarm!=null)
                        powerAlarm.cancelAlarm(mContext, true);
                    }

                    if (Variables.isAccelerometerEmbedded) {
                        stopAccelerometer();

                        /*
                         * instance of the ongoing accelerometer values
                         */

                        if (getAccelerometerValues().size() != 0) {
                            /*
                             * the location and embedded accelerometer reading
                             */

                            if (isAccurate(location)) {
                                currentEmbeddedLocationModel = new EmbeddedLocationModel(
                                        location, new ProcessedAccelerometerModel(
                                        getAccelerometerValues()),
                                        userId, "GPS");

                                filterAndInsertIntoDatabase(currentEmbeddedLocationModel);
                            } else {
                                LoggingUtils.w(LoggingUtils.TAG_GPS,
                                        "Location is Inaccurate. Ignoring, Long: "
                                                + location.getLongitude() +
                                                ", Lat: " + location.getLatitude() +
                                                ", Acc: " + location.getAccuracy());
                                LoggingUtils.i(mContext, LoggingUtils.TAG_GPS, mContext.getString(R.string.tag_gps_inaccurate),
                                        String.valueOf(location.getAccuracy()));
                            }

                            resetAccelerometerValues();
                            // feed listener

                            /*
                             * start new instance of the accelerometer
                             */

                        } else {
                            locationDatabase.insertLocationIntoDb(new EmbeddedLocationModel(location, userId, true, "GPS"));
                        }
                        startAccelerometer();
                    } else {
                        if (isAccurate(location)) {
                            locationDatabaseSimple.insertLocationIntoDb(new LocationModel(location, userId, "GPS"));
                        } else {
                            LoggingUtils.w(LoggingUtils.TAG_GPS,
                                    "Location is Inaccurate. Ignoring, Long: "
                                            + location.getLongitude() +
                                            ", Lat: " + location.getLatitude() +
                                            ", Acc: " + location.getAccuracy());
                            LoggingUtils.i(mContext, LoggingUtils.TAG_GPS, mContext.getString(R.string.tag_gps_inaccurate),
                                    String.valueOf(location.getAccuracy()));
                        }
                    }

                    if (Variables.equiDistance) {
                        try {
                            tryToAdaptSpeedUsingList(location);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Timber.e(e);
                        }
                    }

                    if (Variables.isPowerSavingOn) {
                        powerAlarm = new PowerSavingReceiver(2);
                        powerAlarm.setAlarm(mContext, true);
                    }
                } else {
                    skipOneLocation = false;
                    releaseLock();
                }
            }

            private void filterAndInsertIntoDatabase(EmbeddedLocationModel loc) {
                if (prevLocation == null) {
                    prevLocation = loc.getCurrentLocation().getAsLocation();
                    locationDatabase.insertLocationIntoDb(loc);
                } else {
                    long timeDiff = loc.getCurrentLocation().getAsLocation().getTime() - prevLocation.getTime();
                    if (timeDiff >= EquidistanceTracking.currentFrequency * 1000) {
                        locationDatabase.insertLocationIntoDb(loc);
                        prevLocation = loc.getCurrentLocation().getAsLocation();
                    } else {
                        LoggingUtils.i(LoggingUtils.TAG_GPS, "New location time diff " + timeDiff
                                + " is less than current frequency " + (EquidistanceTracking.currentFrequency*1000));
                        LoggingUtils.i(mContext, LoggingUtils.TAG_GPS, ctx.getString(R.string.tag_gps_time_diff),
                                String.valueOf(timeDiff), String.valueOf((EquidistanceTracking.currentFrequency * 1000)));
                    }
                }
                /*
                 * int difBearing = 0; if (noiseTestList == null) {
                 * noiseTestList = new LinkedList<EmbeddedLocationModel>();
                 * noiseTestList.add(loc); } else if (loc.currentLocation.time_
                 * - noiseTestList.getLast().getCurrentLocation().time_ != 0) {
                 * if (noiseTestList.size() <= 5) noiseTestList.add(loc); else {
                 * float prevBearing = 400; Location prevLocation = null; for
                 * (EmbeddedLocationModel l : noiseTestList) if (prevLocation ==
                 * null) prevLocation = l.getCurrentLocation() .getAsLocation();
                 * else { if (prevBearing == 400) prevBearing =
                 * prevLocation.bearingTo(l .getCurrentLocation()
                 * .getAsLocation()); else { if (Math.abs(prevBearing -
                 * prevLocation.bearingTo(l .getCurrentLocation()
                 * .getAsLocation())) > 120) difBearing++;
                 *
                 * prevBearing = prevLocation.bearingTo(l .getCurrentLocation()
                 * .getAsLocation()); } prevLocation = l.getCurrentLocation()
                 * .getAsLocation(); } //TODO if (difBearing < 3) { for
                 * (EmbeddedLocationModel e : noiseTestList)
                 * locationDatabase.insertLocationIntoDb(e); Timber.d(
                 * "REGULAR INSERT"); } else {
                 * locationDatabase.insertLocationIntoDb(noiseTestList
                 * .getFirst()); Timber.d("NOISE TAG", "DELETE ALL "); }
                 * noiseTestList = null; } }
                 */

            }

            private boolean isAccurate(Location location) {
                if (Variables.isAccuracyFilterEnabled) {
                    return location.getAccuracy() <= Double.parseDouble(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_ACCURACY).toString());
                }
                return true;
            }
        };


        secLocationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(
                        mContext,
                        Constants.deactivateGPSWarning,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLocationChanged(Location location) {
                lastRecordedLocationTime = System.currentTimeMillis();
                LoggingUtils.i(LoggingUtils.TAG_WIFI, "New location detected");
                LoggingUtils.i(mContext, LoggingUtils.TAG_WIFI, ctx.getString(R.string.tag_gps_detected));

                Timber.d("RECEIVED WiFi location");

                if (!skipOneLocation) {
                    // check that the user id is in its correct instance

                    if (userId == 0) {
                        userId = adminDb.getUserId();
                    }
                    /*
                     * this also resets the accelerometer values
                     */

					/*if (Variables.isPowerSavingOn)
						try {
							powerAlarm.cancelAlarm(mContext, true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Timber.e(e);
						}
					*/
                    if (Variables.isAccelerometerEmbedded) {
                        stopAccelerometer();

                        /*
                         * instance of the ongoing accelerometer values
                         */

                        if (getAccelerometerValues().size() != 0) {
                            /*
                             * the location and embedded accelerometer reading
                             */

							/*if (isAccurate(location)) {
								Toast.makeText(mContext, "Received accurate WIFI location", Toast.LENGTH_LONG).show();
							} else 	Toast.makeText(mContext, "Received inaccurate WIFI location", Toast.LENGTH_LONG).show();*/

                            currentEmbeddedLocationModel = new EmbeddedLocationModel(
                                    location, new ProcessedAccelerometerModel(
                                    getAccelerometerValues()),
                                    userId, "WiFi");
                            filterAndInsertIntoDatabase(currentEmbeddedLocationModel);

                            resetAccelerometerValues();

                            /*
                             * start new instance of the accelerometer
                             */

                        } else {
                            locationDatabase.insertLocationIntoDb(new EmbeddedLocationModel(location, userId, true, "WiFi"));
                        }
                        startAccelerometer();
                    } else {
                        if (isAccurate(location)) {
                            locationDatabaseSimple.insertLocationIntoDb(new LocationModel(location, userId, "WiFi"));
                        } else {
                            LoggingUtils.w(LoggingUtils.TAG_WIFI,
                                    "Location is Inaccurate. Ignoring, Long: "
                                            + location.getLongitude() +
                                            ", Lat: " + location.getLatitude() +
                                            ", Acc: " + location.getAccuracy());
                            LoggingUtils.i(mContext, LoggingUtils.TAG_WIFI, mContext.getString(R.string.tag_gps_inaccurate),
                                    String.valueOf(location.getAccuracy()));
                        }
                    }
                } else {
                    skipOneLocation = false;
                    resetAccelerometerValues();
                    //	releaseLock();
                }

            }

            private void filterAndInsertIntoDatabase(EmbeddedLocationModel loc) {
                if (prevLocation == null) {
                    prevLocation = loc.getCurrentLocation().getAsLocation();
                    locationDatabase.insertLocationIntoDb(loc);
                } else {
                    if (prevLocation.getTime() != loc.getCurrentLocation().getAsLocation().getTime()) {
                        locationDatabase.insertLocationIntoDb(loc);
                        prevLocation = loc.getCurrentLocation().getAsLocation();
                    } else {
                        LoggingUtils.i(LoggingUtils.TAG_WIFI, "New location time is equal to prev. location: " + prevLocation.getTime());
                        LoggingUtils.i(mContext, LoggingUtils.TAG_WIFI, ctx.getString(R.string.tag_gps_equal_to_prev),
                                String.valueOf(prevLocation.getTime()));
                    }
                }
            }

            private boolean isAccurate(Location location) {
                if (Variables.isAccuracyFilterEnabled) {
                    return location.getAccuracy() <= Double.parseDouble(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_ACCURACY).toString());
                }
                return true;
            }
        };


        mSensorListener = new SensorEventListener() {

            int counter = 0;

            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {

            }

            @Override
            public void onSensorChanged(SensorEvent event) {

                float[] values = event.values.clone();
                counter++;

                values = lowPass(values[0], values[1], values[2]);

                AccelerometerModel model = new AccelerometerModel(values, event.accuracy,
                        System.currentTimeMillis());

                if (counter > 10) {
                    //Timber.d("Accelerometer Values Added");
                    accelerometerValues.addLast(model);
                }
            }
        };
    }

    @Override
    protected void finalize() throws Throwable {
        adminDb.closeDb();
        locationDatabase.closeDb();
        locationDatabaseSimple.closeDb();
        super.finalize();
    }

    public static long getTimeFreq() {
        return timeFrequency;
    }

    public static float getDistanceFreq() {
        return distanceFrequency;
    }

    private static int getNeededDelay() {
        // not needed - gets service killed
        /*
         * if (timeFrequency == 1000) return SensorManager.SENSOR_DELAY_FASTEST;
         * else if (timeFrequency <= 10000) return
         * SensorManager.SENSOR_DELAY_GAME; else if (timeFrequency <= 20000)
         * return SensorManager.SENSOR_DELAY_UI;
         */
        // TODO Modify this
        return SensorManager.SENSOR_DELAY_NORMAL;
    }

    private static LinkedList<AccelerometerModel> getAccelerometerValues() {
        /*
         * Singleton accelerometer values
         */

        if (accelerometerValues == null) {
            accelerometerValues = new LinkedList<>();
        }
        return accelerometerValues;
    }

    /**
     * Reset the list's content
     */
    private static LinkedList<AccelerometerModel> emptyAccelerometerValuesGetNewList() {

        accelerometerValues = getAccelerometerValues();
        accelerometerValues = new LinkedList<>();
        gravity = new float[3];
        return accelerometerValues;
    }

    /**
     * Smooth the accelerometer values using a low pass filter
     */
    private static float[] lowPass(float x, float y, float z) {
        float[] filteredValues = new float[3];

        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];

        return filteredValues;
    }

    public static void getMove() {
        // TODO Auto-generated method stub
        StringBuilder b = new StringBuilder();
        if (getAccelerometerValues() != null) {
            b.append("- Accelerometer Values : Not Null ");
//            Timber.d("MOVING NULL : PASSED");
            if (getAccelerometerValues().size() != 0) {
//                Timber.d("MOVING ACC SIZE : PASSED");
                b.append("- Accelerometer Size : ").append(getAccelerometerValues().size()).append(" ");

                ProcessedAccelerometerModel aV = new ProcessedAccelerometerModel(getAccelerometerValues());
                isMoving = aV.isTotalIsMoving2();
                b.append("- Is Moving : ").append(isMoving).append(" ");
            } else {
//                Timber.d("MOVING ACC SIZE : FAILED");
                b.append("- Accelerometer Size : 0 ");
            }

        } else {
//            Timber.d("MOVING NULL : FAILED");
            b.append("- Accelerometer Values : Null ");
        }

        Timber.i("GET MOVE : %s", b.toString());

    }

    /**
     * This calls for the normal launch of the location listener
     */
    public void startListening() {

//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                timeFrequency, Long.parseLong(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_DISTANCE).toString()),
//                locationListener);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            LoggingUtils.e(LoggingUtils.TAG_SYSTEM, "Permission Denied while trying to register to location updates. Context: Primary listener");
            LoggingUtils.i(mContext, LoggingUtils.TAG_SYSTEM, mContext.getString(R.string.tag_system_denied_reg_loc), "startListening");

            return;
        }

        serviceIsStarted = true;
        requestServiceWakeLock();

        Timber.d("GPS PROVIDER ON");

        if (Variables.isPowerSavingOn) {
            powerAlarm = new PowerSavingReceiver(2);
            powerAlarm.setAlarm(mContext, true);
        }

        // isRunning = true;

        locationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);

        /*
         * mSensorManager = (SensorManager) mContext
         * .getSystemService(Context.SENSOR_SERVICE);
         */

        startAccelerometer();

        locationManager.removeUpdates(locationListener);


        lastRecordedLocationTime = System.currentTimeMillis();
        LoggingUtils.i(LoggingUtils.TAG_GPS, "Requesting GPS location");
        LoggingUtils.i(mContext, LoggingUtils.TAG_GPS, mContext.getString(R.string.tag_gps_request));

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                timeFrequency, distanceFrequency,
                locationListener);

        /*
         * for (EmbeddedLocationModel lv:
         * locationDatabase.getAllAccelerometersFromDatabase()) { Location l = new
         * Location(""); l.setTime(lv.currentLocation.time_);
         * l.setLatitude(lv.currentLocation.lat_);
         * l.setLongitude(lv.currentLocation.lon_);
         * locationListener.onLocationChanged(l); }
         */

    }

    private void resetAccelerometerValues() {
        accelerometerValues = new LinkedList<>();
    }

    public void stopListening() {
        try {
            locationManager.removeUpdates(locationListener);
            Timber.d("GPS PROVIDER DISABLED");
            //locationManager = null;
            releaseLock();
        } catch (Exception e) {
            Timber.e(e);
        }

        stopAccelerometer();

    }

    public void stopAlarm() {
        if (Variables.isPowerSavingOn) {
            try {
                powerAlarm.cancelAlarm(mContext, true);
            } catch (Exception e) {
                Timber.e(e);
            }

            try {
                powerAlarm.cancelAlarm(mContext);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void startAccelerometer() {

        // isAccelerometerOn = true;
        emptyAccelerometerValuesGetNewList();

        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(mSensorListener, mSensor, getNeededDelay());
        }

    }

    private void stopAccelerometer() {

        try {
            mSensorManager.unregisterListener(mSensorListener, mSensor);
            Timber.d("Accelerometer SENSOR DISABLED");
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    @SuppressWarnings("static-access")
    private void tryToAdaptSpeedUsingList(Location location) {

        EquidistanceTracking.getInstance().addLocationToList(location);

        final long newFrequency = Math.round(EquidistanceTracking.getInstance().checkForLocationAdjustment());
        if (newFrequency != -1) {
            if (newFrequency > 10000) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    LoggingUtils.e(LoggingUtils.TAG_SYSTEM, "Permission Denied while trying to register to location updates. Context: Equidistance adjustment");
                    LoggingUtils.i(mContext, LoggingUtils.TAG_SYSTEM, mContext.getString(R.string.tag_system_denied_reg_loc), "tryToAdaptSpeedUsingList");

                    return;
                }
                locationManager.removeUpdates(locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, newFrequency, distanceFrequency, locationListener);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, newFrequency, distanceFrequency, locationListener);
            }
            //skipOneLocation = true;
            resetAccelerometerValues();
            requestLock();
            timeFrequency = newFrequency;
        }
    }

    @SuppressLint("WakelockTimeout")
    private void requestLock() {
        // TODO Auto-generated method stub
        getWakeLockInstance().acquire();
    }

    private WakeLock getWakeLockInstance() {
        if (wl == null) {
            pm = (PowerManager) mContext
                    .getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Fix wake lock");
            }
        }
        return wl;
    }

    private WakeLock getServiceWakeLockInstance() {
        if (serviceWakeLock == null) {
            pm = (PowerManager) mContext
                    .getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                serviceWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "Service wake lock");
            }
        }
        return serviceWakeLock;
    }

    private void releaseServiceWakeLock() {
        if (serviceIsStarted) {
            getServiceWakeLockInstance().release();
            serviceIsStarted = false;
        }
    }

    @SuppressLint("WakelockTimeout")
    private void requestServiceWakeLock() {
        serviceIsStarted = true;
        getServiceWakeLockInstance().acquire();
    }

    private void releaseLock() {
        try {
            if (getWakeLockInstance().isHeld()) {
                getWakeLockInstance().release();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Timber.e(e);
        }
    }

    public void restartForGarbageCollector() {
        // TODO Auto-generated method stub
        Timber.d("I was received now : %s", System.currentTimeMillis());
        stopAlarm();
        System.exit(-1);
    }

    public void startSecListening() {
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                30000, 50,
//                secLocationListener);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            LoggingUtils.e( LoggingUtils.TAG_SYSTEM, "Permission Denied while trying to register to location updates. Context: Secondary listener");
            ArrayList<String> al = new ArrayList<>();
            al.add(Meili.getInstance().getResources().getString(R.string.tag_system_denied_reg_loc));
            al.add("Secondary listener");
            LoggingUtils.i(mContext, LoggingUtils.TAG_SYSTEM, mContext.getString(R.string.tag_system_denied_reg_loc), "startSecListening");

            return;
        }

/*		// prevent re-registration
		isMoving = false;*/

        Timber.d("ENABLED WIFI");

        if (locationManager == null) {
            locationManager = (LocationManager) mContext
                    .getSystemService(Context.LOCATION_SERVICE);
        }

        startAccelerometer();

        locationManager.removeUpdates(locationListener);
        locationManager.removeUpdates(secLocationListener);

        lastRecordedLocationTime = System.currentTimeMillis();
        LoggingUtils.i(LoggingUtils.TAG_WIFI, "Requesting Network location");
        LoggingUtils.i(mContext, LoggingUtils.TAG_WIFI, mContext.getString(R.string.tag_gps_request));
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                timeFrequency, distanceFrequency, secLocationListener);

    }

    private void stopSecListening() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(secLocationListener);
            }
            releaseLock();
            Timber.d("WiFi PROVIDER DISABLED");
        } catch (Exception e) {
            Timber.e(e);
        }

        stopAccelerometer();

    }

}
