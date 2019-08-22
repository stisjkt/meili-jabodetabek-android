package daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import constants.PreferencesAPI;
import id.ac.stis.meili.R;
import models.EmbeddedLocationModel;
import models.LocationModel;
import models.ParameterPrefModel;
import models.ProcessedAccelerometerModel;
import preferences.PreferencesManager;
import timber.log.Timber;
import utilities.LoggingUtils;

public class LocationAccelerationDao {
    private SQLiteDatabase myDatabase;
    private String locationTableName;
    private Context mContext;

    public LocationAccelerationDao(String databaseName,
                                   String locationTableName_, Context ctx) {
        locationTableName = locationTableName_;
        this.mContext = ctx;
        myDatabase = mContext.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        LinkedList<LinkedHashMap<String, String>> listOfMaps = new LinkedList<>();
        listOfMaps.add(LocationModel.getAllElements());
        listOfMaps.add(ProcessedAccelerometerModel.getAllElements());
        listOfMaps.add(ParameterPrefModel.getAllElements());
        myDatabase.execSQL(utilities.GetInfo.generateSqlStatement(locationTableName, listOfMaps));
    }

    public boolean insertLocationIntoDb(EmbeddedLocationModel eL) {

        LoggingUtils.i(eL.getCurrentLocation().provider.equals("GPS") ? LoggingUtils.TAG_GPS : LoggingUtils.TAG_WIFI,
                "Inserting Location, Long: " + eL.getCurrentLocation().lon_ +
                        ", Lat: " + eL.getCurrentLocation().lat_ +
                        ", Acc: " + eL.getCurrentLocation().accuracy_);

        ContentValues newValues = new ContentValues();

        for (Field f : LocationModel.class.getDeclaredFields()) {
            if (!f.getName().contains("$") && !f.getName().equalsIgnoreCase("serialVersionUID")) {
                f.setAccessible(true);

                String name = f.getName();
                String value;
                try {
                    if (eL.getCurrentLocation() != null) {
                        value = f.get(eL.getCurrentLocation()).toString();
                        newValues.put(name, value);
                    }
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                    return false;
                } catch (IllegalAccessException e) {
                    Timber.e(e);
                    return false;
                }

            }
        }

        for (Field f : ProcessedAccelerometerModel.class.getDeclaredFields()) {
            if (!f.getName().contains("$") && !f.getName().equalsIgnoreCase("serialVersionUID")) {
                f.setAccessible(true);
                String name = f.getName();
                String value;
                try {
                    if (eL.getCurrentAcc() != null) {
                        value = f.get(eL.getCurrentAcc()).toString();
                        newValues.put(name, value);
                    }
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                    return false;
                } catch (IllegalAccessException e) {
                    Timber.e(e);
                    return false;
                }
            }
        }

        for (Field f : ParameterPrefModel.class.getDeclaredFields()) {
            if (!f.getName().contains("$") && !f.getName().equalsIgnoreCase("serialVersionUID")) {
                f.setAccessible(true);
                String name = f.getName();
                try {
                    newValues.put(name, PreferencesManager.getInstance().get(name).toString());
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                    return false;
                }
            }
        }

        newValues.put("upload", "0");

        myDatabase.insert(locationTableName, null, newValues);
        LoggingUtils.i(mContext, eL.getCurrentLocation().provider.equals("GPS") ? LoggingUtils.TAG_GPS : LoggingUtils.TAG_WIFI,
                mContext.getString(R.string.tag_gps_insert),
                newValues.getAsString("time_"));
        newValues.clear();
        return false;
    }

    public LinkedList<EmbeddedLocationModel> getAllLocationsFromDatabase() {

        int numberOfElements = ProcessedAccelerometerModel.class.getDeclaredFields().length
                + LocationModel.class.getDeclaredFields().length + ParameterPrefModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements];
        int i = 0;
        for (Field f : LocationModel.class.getDeclaredFields()) {
            if (!f.getName().contains("$") && !f.getName().equalsIgnoreCase("serialVersionUID")) {
                f.setAccessible(true);
                arrayOfFields[i] = f.getName();
                i++;
            }
        }

        for (Field f : ProcessedAccelerometerModel.class.getDeclaredFields()) {
            if (!f.getName().contains("$") && !f.getName().equalsIgnoreCase("serialVersionUID")) {
                f.setAccessible(true);
                arrayOfFields[i] = f.getName();
                i++;
            }
        }

        for (Field f : ParameterPrefModel.class.getDeclaredFields()) {
            if (!f.getName().contains("$") && !f.getName().equalsIgnoreCase("serialVersionUID")) {
                f.setAccessible(true);
                arrayOfFields[i] = f.getName();
                i++;
            }
        }

        Cursor cursor = myDatabase.query(locationTableName, arrayOfFields,
                null, null, null, null, "time_");
        LinkedList<EmbeddedLocationModel> eL = new LinkedList<>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    /*
                     * LocationModel specific part
                     */
                    long time_ = cursor.getLong(cursor.getColumnIndex("time_"));
                    int user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
                    double lat_ = cursor.getDouble(cursor.getColumnIndex("lat_"));
                    double lon_ = cursor.getDouble(cursor.getColumnIndex("lon_"));
                    double speed_ = cursor.getDouble(cursor.getColumnIndex("speed_"));
                    double altitude_ = cursor.getDouble(cursor.getColumnIndex("altitude_"));
                    double bearing_ = cursor.getDouble(cursor.getColumnIndex("bearing_"));
                    double accuracy_ = cursor.getDouble(cursor.getColumnIndex("accuracy_"));
                    int satellites_ = cursor.getInt(cursor.getColumnIndex("satellites_"));
                    String provider = cursor.getString(cursor.getColumnIndex("provider"));

                    LocationModel lV = new LocationModel(user_id, lat_, lon_, speed_,
                            altitude_, bearing_, accuracy_, satellites_, time_, provider);

                    /*
                     * Accelerometer Specific Parts
                     */
                    float xMean, yMean, zMean, totalMean;
                    float xStdDev, yStdDev, zStdDev, totalStdDev;
                    float xMin, xMax, yMin, yMax, zMin, zMax, totalMin, totalMax;
                    int xNumberOfPeaks, yNumberOfPeaks, zNumberOfPeaks, totalNumberOfPeaks;
                    int totalNumberOfSteps;
                    boolean xIsMoving, yIsMoving, zIsMoving, totalIsMoving;
                    int size;

                    xMean = cursor.getFloat(cursor.getColumnIndex("xMean"));
                    yMean = cursor.getFloat(cursor.getColumnIndex("yMean"));
                    zMean = cursor.getFloat(cursor.getColumnIndex("zMean"));
                    totalMean = cursor.getFloat(cursor.getColumnIndex("totalMean"));

                    xStdDev = cursor.getFloat(cursor.getColumnIndex("xStdDev"));
                    yStdDev = cursor.getFloat(cursor.getColumnIndex("yStdDev"));
                    zStdDev = cursor.getFloat(cursor.getColumnIndex("zStdDev"));
                    totalStdDev = cursor.getFloat(cursor.getColumnIndex("totalStdDev"));

                    xMin = cursor.getFloat(cursor.getColumnIndex("xMinimum"));
                    yMin = cursor.getFloat(cursor.getColumnIndex("yMin"));
                    zMin = cursor.getFloat(cursor.getColumnIndex("zMin"));
                    totalMin = cursor.getFloat(cursor.getColumnIndex("totalMin"));

                    xMax = cursor.getFloat(cursor.getColumnIndex("xMaximum"));
                    yMax = cursor.getFloat(cursor.getColumnIndex("yMax"));
                    zMax = cursor.getFloat(cursor.getColumnIndex("zMax"));
                    totalMax = cursor.getFloat(cursor.getColumnIndex("totalMax"));

                    xNumberOfPeaks = cursor.getInt(cursor.getColumnIndex("xNumberOfPeaks"));
                    yNumberOfPeaks = cursor.getInt(cursor.getColumnIndex("yNumberOfPeaks"));
                    zNumberOfPeaks = cursor.getInt(cursor.getColumnIndex("zNumberOfPeaks"));
                    totalNumberOfPeaks = cursor.getInt(cursor.getColumnIndex("totalNumberOfPeaks"));

                    totalNumberOfSteps = cursor.getInt(cursor.getColumnIndex("totalNumberOfSteps"));

                    xIsMoving = cursor.getString(cursor.getColumnIndex("xIsMoving")).equalsIgnoreCase("true");
                    yIsMoving = cursor.getString(cursor.getColumnIndex("yIsMoving")).equalsIgnoreCase("true");
                    zIsMoving = cursor.getString(cursor.getColumnIndex("zIsMoving")).equalsIgnoreCase("true");
                    totalIsMoving = cursor.getString(cursor.getColumnIndex("totalIsMoving")).equalsIgnoreCase("true");

                    size = cursor.getInt(cursor.getColumnIndex("size"));

                    ProcessedAccelerometerModel aV = new ProcessedAccelerometerModel(xMean, yMean,
                            zMean, totalMean, xStdDev, yStdDev, zStdDev, totalStdDev,
                            xMin, xMax, yMin, yMax, zMin, zMax, totalMin, totalMax,
                            xNumberOfPeaks, yNumberOfPeaks, zNumberOfPeaks,
                            totalNumberOfPeaks, totalNumberOfSteps, xIsMoving,
                            yIsMoving, zIsMoving, totalIsMoving, size);

                    /*
                     * Preferences specific parts
                     */
                    int pref_min_acc = cursor.getInt(cursor.getColumnIndex(PreferencesAPI.KEY_MIN_ACCURACY));
                    int pref_min_dist = cursor.getInt(cursor.getColumnIndex(PreferencesAPI.KEY_MIN_DISTANCE));
                    int pref_min_time = cursor.getInt(cursor.getColumnIndex(PreferencesAPI.KEY_MIN_TIME));
                    float pref_acc_threshold = cursor.getFloat(cursor.getColumnIndex(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD));
                    float pref_acc_period = cursor.getFloat(cursor.getColumnIndex(PreferencesAPI.KEY_ACCELEROMETER_PERIOD));
                    float pref_acc_saving = cursor.getFloat(cursor.getColumnIndex(PreferencesAPI.KEY_ACCELEROMETER_SLEEP));

                    ParameterPrefModel pM = new ParameterPrefModel(pref_min_acc, pref_min_dist, pref_min_time,
                            pref_acc_threshold, pref_acc_period, pref_acc_saving);

                    EmbeddedLocationModel embeddedLocationModel = new EmbeddedLocationModel(lV, aV, pM);
                    //if (user_id==1)
                    eL.add(embeddedLocationModel);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return eL;
    }

    public LinkedList<EmbeddedLocationModel> getLocationsForUploadFromDatabase(int chunk) {

        int numberOfElements = ProcessedAccelerometerModel.class.getDeclaredFields().length
                + LocationModel.class.getDeclaredFields().length +
                ParameterPrefModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements + 1];
        int i = 0;
        for (Field f : LocationModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            if (!f.getName().equalsIgnoreCase("serialVersionUID")) {
                arrayOfFields[i] = f.getName();
                i++;
            }
        }

        for (Field f : ProcessedAccelerometerModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            if (!f.getName().equalsIgnoreCase("serialVersionUID")) {
                arrayOfFields[i] = f.getName();
                i++;
            }
        }

        for (Field f : ParameterPrefModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            if (!f.getName().equalsIgnoreCase("serialVersionUID")) {
                arrayOfFields[i] = f.getName();
                i++;
            }
        }

        arrayOfFields[i] = "id";


        //	Cursor cursor = myDatabase.query(tableName, arrayOfFields,
        //			"upload=?", new String[] { "FALSE" }, null, null, null);

        String selection = "NOT upload LIMIT " + chunk;
        if (chunk == Integer.valueOf(PreferencesAPI.VALUE_UPLOAD_CHUNK_INFINITE)) {
            selection = "NOT upload";
        }

        Cursor cursor = myDatabase.query(locationTableName, arrayOfFields,
                selection, null, null,
                null, null);

        LinkedList<EmbeddedLocationModel> eL = new LinkedList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    /*
                     * LocationModel specific part
                     */
                    long time_ = cursor.getLong(cursor.getColumnIndex("time_"));
                    int user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
                    double lat_ = cursor.getDouble(cursor.getColumnIndex("lat_"));
                    double lon_ = cursor.getDouble(cursor.getColumnIndex("lon_"));
                    double speed_ = cursor.getDouble(cursor.getColumnIndex("speed_"));
                    double altitude_ = cursor.getDouble(cursor.getColumnIndex("altitude_"));
                    double bearing_ = cursor.getDouble(cursor.getColumnIndex("bearing_"));
                    double accuracy_ = cursor.getDouble(cursor.getColumnIndex("accuracy_"));
                    int satellites_ = cursor.getInt(cursor.getColumnIndex("satellites_"));
                    String provider = cursor.getString(cursor.getColumnIndex("provider"));
                    LocationModel lV = new LocationModel(user_id, lat_, lon_, speed_,
                            altitude_, bearing_, accuracy_, satellites_, time_, provider);

                    /*
                     * Accelerometer Specific Parts
                     */
                    float xMean, yMean, zMean, totalMean;
                    float xStdDev, yStdDev, zStdDev, totalStdDev;
                    float xMin, xMax, yMin, yMax, zMin, zMax, totalMin, totalMax;
                    int xNumberOfPeaks, yNumberOfPeaks, zNumberOfPeaks, totalNumberOfPeaks;
                    int totalNumberOfSteps;
                    boolean xIsMoving, yIsMoving, zIsMoving, totalIsMoving;
                    int size;

                    xMean = cursor.getFloat(cursor.getColumnIndex("xMean"));
                    yMean = cursor.getFloat(cursor.getColumnIndex("yMean"));
                    zMean = cursor.getFloat(cursor.getColumnIndex("zMean"));
                    totalMean = cursor.getFloat(cursor.getColumnIndex("totalMean"));

                    xStdDev = cursor.getFloat(cursor.getColumnIndex("xStdDev"));
                    yStdDev = cursor.getFloat(cursor.getColumnIndex("yStdDev"));
                    zStdDev = cursor.getFloat(cursor.getColumnIndex("zStdDev"));
                    totalStdDev = cursor.getFloat(cursor.getColumnIndex("totalStdDev"));

                    xMin = cursor.getFloat(cursor.getColumnIndex("xMinimum"));
                    yMin = cursor.getFloat(cursor.getColumnIndex("yMin"));
                    zMin = cursor.getFloat(cursor.getColumnIndex("zMin"));
                    totalMin = cursor.getFloat(cursor.getColumnIndex("totalMin"));

                    xMax = cursor.getFloat(cursor.getColumnIndex("xMaximum"));
                    yMax = cursor.getFloat(cursor.getColumnIndex("yMax"));
                    zMax = cursor.getFloat(cursor.getColumnIndex("zMax"));
                    totalMax = cursor.getFloat(cursor.getColumnIndex("totalMax"));

                    xNumberOfPeaks = cursor.getInt(cursor.getColumnIndex("xNumberOfPeaks"));
                    yNumberOfPeaks = cursor.getInt(cursor.getColumnIndex("yNumberOfPeaks"));
                    zNumberOfPeaks = cursor.getInt(cursor.getColumnIndex("zNumberOfPeaks"));
                    totalNumberOfPeaks = cursor.getInt(cursor.getColumnIndex("totalNumberOfPeaks"));

                    totalNumberOfSteps = cursor.getInt(cursor.getColumnIndex("totalNumberOfSteps"));

                    xIsMoving = cursor.getString(cursor.getColumnIndex("xIsMoving")).equalsIgnoreCase("true");
                    yIsMoving = cursor.getString(cursor.getColumnIndex("yIsMoving")).equalsIgnoreCase("true");
                    zIsMoving = cursor.getString(cursor.getColumnIndex("zIsMoving")).equalsIgnoreCase("true");
                    totalIsMoving = cursor.getString(cursor.getColumnIndex("totalIsMoving")).equalsIgnoreCase("true");

                    size = cursor.getInt(cursor.getColumnIndex("size"));

                    ProcessedAccelerometerModel aV = new ProcessedAccelerometerModel(xMean, yMean,
                            zMean, totalMean, xStdDev, yStdDev, zStdDev, totalStdDev,
                            xMin, xMax, yMin, yMax, zMin, zMax, totalMin, totalMax,
                            xNumberOfPeaks, yNumberOfPeaks, zNumberOfPeaks,
                            totalNumberOfPeaks, totalNumberOfSteps, xIsMoving,
                            yIsMoving, zIsMoving, totalIsMoving, size);

                    /*
                     * Preferences specific parts
                     */
                    int pref_min_acc = cursor.getInt(cursor.getColumnIndex(PreferencesAPI.KEY_MIN_ACCURACY));
                    int pref_min_dist = cursor.getInt(cursor.getColumnIndex(PreferencesAPI.KEY_MIN_DISTANCE));
                    int pref_min_time = cursor.getInt(cursor.getColumnIndex(PreferencesAPI.KEY_MIN_TIME));
                    float pref_acc_threshold = cursor.getFloat(cursor.getColumnIndex(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD));
                    float pref_acc_period = cursor.getFloat(cursor.getColumnIndex(PreferencesAPI.KEY_ACCELEROMETER_PERIOD));
                    float pref_acc_saving = cursor.getFloat(cursor.getColumnIndex(PreferencesAPI.KEY_ACCELEROMETER_SLEEP));

                    ParameterPrefModel pM = new ParameterPrefModel(pref_min_acc, pref_min_dist, pref_min_time,
                            pref_acc_threshold, pref_acc_period, pref_acc_saving);

                    EmbeddedLocationModel embeddedLocationModel = new EmbeddedLocationModel(lV, aV, pM);
                    embeddedLocationModel.setId(cursor.getInt(cursor.getColumnIndex("id")));

                    eL.add(embeddedLocationModel);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return eL;
    }

    public String getJSONFromLocationsForUploadFromDatabase(int chunk) {
        Gson json = new Gson();
        return json.toJson(getLocationsForUploadFromDatabase(chunk));
    }

    public void setUploadToTrue(int lastId) {
        ContentValues values = new ContentValues();
        values.put("upload", true);
        myDatabase.update(locationTableName, values, "id <= ?",
                new String[]{String.valueOf(lastId)});
    }

    public LocationModel getLastLocation() {
        int numberOfElements = ProcessedAccelerometerModel.class.getDeclaredFields().length
                + LocationModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements];
        int i = 0;
        for (Field f : LocationModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            arrayOfFields[i] = f.getName();
            i++;
        }

        Cursor cursor = myDatabase.query(locationTableName, arrayOfFields,
                null, null, null, null, "id", "1");
        LocationModel lastLocation = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {

                /*
                 * LocationModel specific part
                 */
                long time_ = cursor.getLong(cursor.getColumnIndex("time_"));
                int user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
                double lat_ = cursor.getDouble(cursor.getColumnIndex("lat_"));
                double lon_ = cursor.getDouble(cursor.getColumnIndex("lon_"));
                double speed_ = cursor.getDouble(cursor.getColumnIndex("speed_"));
                double altitude_ = cursor.getDouble(cursor.getColumnIndex("altitude_"));
                double bearing_ = cursor.getDouble(cursor.getColumnIndex("bearing_"));
                double accuracy_ = cursor.getDouble(cursor.getColumnIndex("accuracy_"));
                int satellites_ = cursor.getInt(cursor.getColumnIndex("satellites_"));
                String provider = cursor.getString(cursor.getColumnIndex("provider"));
                lastLocation = new LocationModel(user_id, lat_, lon_, speed_,
                        altitude_, bearing_, accuracy_, satellites_, time_, provider);

            }
            cursor.close();
        }
        return lastLocation;
    }

    @Override
    protected void finalize() throws Throwable {
        closeDb();
        super.finalize();
    }

    public void closeDb() {
        if (myDatabase != null) {
            if (myDatabase.isOpen()) {
                myDatabase.close();
            }
        }
    }
}
