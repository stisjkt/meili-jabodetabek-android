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
import models.LocationModel;
import models.LocationSimpleModel;
import models.ParameterPrefModel;
import preferences.PreferencesManager;
import timber.log.Timber;
import utilities.LoggingUtils;

public class LocationSimpleDao {
    private SQLiteDatabase myDatabase;
    private String locationTableName;
    private Context mContext;

    public LocationSimpleDao(String databaseName,
                             String locationTableName_, Context ctx) {
        locationTableName = locationTableName_;
        this.mContext = ctx;
        myDatabase = mContext.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE,
                null);
        LinkedList<LinkedHashMap<String, String>> listOfMaps = new LinkedList<>();
        listOfMaps.add(LocationModel.getAllElements());
        listOfMaps.add(ParameterPrefModel.getAllElements());
        myDatabase.execSQL(utilities.GetInfo.generateSqlStatement(
                locationTableName, listOfMaps));

    }

    public LinkedList<LocationSimpleModel> getAllLocationsFromDatabase() {

        int numberOfElements = LocationModel.class.getDeclaredFields().length +
                ParameterPrefModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements];
        int i = 0;
        for (Field f : LocationModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            arrayOfFields[i] = f.getName();
            i++;
        }

        for (Field f : LocationModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            arrayOfFields[i] = f.getName();
            i++;
        }

        Cursor cursor = myDatabase.query(locationTableName, arrayOfFields,
                null, null, null, null, null);

        LinkedList<LocationSimpleModel> locationList = new LinkedList<>();

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
                    double altitude_ = cursor.getDouble(cursor
                            .getColumnIndex("altitude_"));
                    double bearing_ = cursor.getDouble(cursor
                            .getColumnIndex("bearing_"));
                    double accuracy_ = cursor.getDouble(cursor
                            .getColumnIndex("accuracy_"));
                    int satellites_ = cursor.getInt(cursor
                            .getColumnIndex("satellites_"));
                    String provider = cursor.getString(cursor.getColumnIndex("provider"));
                    LocationModel lV = new LocationModel(user_id, lat_, lon_, speed_,
                            altitude_, bearing_, accuracy_, satellites_, time_, provider);

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

                    locationList.add(new LocationSimpleModel(lV, pM));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return locationList;
    }

    public LinkedList<LocationSimpleModel> getLocationsForUploadFromDatabase(int chunk) {

        int numberOfElements = LocationModel.class.getDeclaredFields().length + ParameterPrefModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements];
        int i = 0;
        for (Field f : LocationModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            arrayOfFields[i] = f.getName();
            i++;
        }

        for (Field f : ParameterPrefModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            arrayOfFields[i] = f.getName();
            i++;
        }

        Cursor cursor = myDatabase.query(locationTableName, arrayOfFields,
                "NOT upload LIMIT " + chunk, null, null,
                null, null);
        LinkedList<LocationSimpleModel> locationList = new LinkedList<>();

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
                    double altitude_ = cursor.getDouble(cursor
                            .getColumnIndex("altitude_"));
                    double bearing_ = cursor.getDouble(cursor
                            .getColumnIndex("bearing_"));
                    double accuracy_ = cursor.getDouble(cursor
                            .getColumnIndex("accuracy_"));
                    int satellites_ = cursor.getInt(cursor
                            .getColumnIndex("satellites_"));
                    String provider = cursor.getString(cursor.getColumnIndex("provider"));
                    LocationModel lV = new LocationModel(user_id, lat_, lon_, speed_,
                            altitude_, bearing_, accuracy_, satellites_, time_, provider);

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

                    LocationSimpleModel locationSimpleModel = new LocationSimpleModel(lV, pM);
                    locationSimpleModel.setId(cursor.getInt(cursor.getColumnIndex("id")));

                    locationList.add(locationSimpleModel);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return locationList;
    }

    public boolean insertLocationIntoDb(LocationModel eL) {

        LoggingUtils.i(eL.provider.equals("GPS") ? LoggingUtils.TAG_GPS : LoggingUtils.TAG_WIFI,
                "Inserting Simple Location, Long: " + eL.lon_ +
                        ", Lat: " + eL.lat_ +
                        ", Acc: " + eL.accuracy_);

        ContentValues newValues = new ContentValues();

        for (Field f : LocationModel.class.getDeclaredFields()) {
            f.setAccessible(true);

            String name = f.getName();
            String value;
            try {
                value = f.get(eL).toString();
                newValues.put(name, value);
            } catch (IllegalArgumentException e) {
                Timber.e(e);
                return false;
            } catch (IllegalAccessException e) {
                Timber.e(e);
                return false;
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

        myDatabase.insert(locationTableName, null, newValues);
        LoggingUtils.i(mContext, eL.provider.equals("GPS") ? LoggingUtils.TAG_GPS : LoggingUtils.TAG_WIFI,
                mContext.getString(R.string.tag_gps_insert), newValues.getAsString("time_"));
        newValues.clear();

        return false;
    }

    public String getJSONFromLocationsForUploadFromDatabase(int chunk) {
        Gson json = new Gson();
        return json.toJson(getLocationsForUploadFromDatabase(chunk));
    }

    public void setUploadToTrue(int lastId) {
        ContentValues values = new ContentValues();
        values.put("upload", "TRUE");
        myDatabase.update(locationTableName, values, "id <= ?",
                new String[]{String.valueOf(lastId)});
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
