package daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import constants.Constants;
import constants.PreferencesAPI;
import models.AccelerometerSimpleModel;
import timber.log.Timber;
import utilities.LoggingUtils;

public class AccelerometerDao {

    private SQLiteDatabase myDatabase;
    private String tableName;
    private Context context;

    public AccelerometerDao(String databaseName,
                            String tableName, Context context) {
        this.tableName = tableName;
        this.context = context;
        myDatabase = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);

        LinkedList<LinkedHashMap<String, String>> listOfMaps = new LinkedList<>();
        listOfMaps.add(AccelerometerSimpleModel.getAllElements());
        myDatabase.execSQL(utilities.GetInfo.generateSqlStatement(tableName, listOfMaps));
    }

    public boolean insertAccelerometerIntoDb(AccelerometerSimpleModel aL) {

        LoggingUtils.i(LoggingUtils.TAG_ACCELEROMETER,
                "Inserting Accelerometer, X: " + aL.getCurrent_x() + ", Y: " +
                        aL.getCurrent_y() + ", Z: " + aL.getMean_z());

        ContentValues newValues = new ContentValues();

        for (Field f : AccelerometerSimpleModel.class.getDeclaredFields()) {
            if (!f.getName().contains("$") && !f.getName().equalsIgnoreCase("serialVersionUID")) {
                f.setAccessible(true);

                String name = f.getName();
                String value;
                try {
                    if (!name.equalsIgnoreCase("id")) {
                        value = f.get(aL).toString();
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

        if (myDatabase.update(tableName, newValues, "time_=?", new String[]{"" + aL.getTime_()}) == 0) {
            newValues.put("upload", "0");
            myDatabase.insert(tableName, null, newValues);
        }
        newValues.clear();

        return false;
    }

    public LinkedList<AccelerometerSimpleModel> getAccelerometersForUploadFromDatabase(int chunk) {

        int numberOfElements = AccelerometerSimpleModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements + 1];
        int i = 0;
        for (Field f : AccelerometerSimpleModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            if (!f.getName().equalsIgnoreCase("serialVersionUID")) {
                arrayOfFields[i] = f.getName();
                i++;
            }
        }

        arrayOfFields[i] = "id";

        String selection = "NOT upload LIMIT " + chunk;
        if (chunk == Integer.valueOf(PreferencesAPI.VALUE_UPLOAD_CHUNK_INFINITE)) {
            selection = "NOT upload";
        }

        Cursor cursor = myDatabase.query(tableName, arrayOfFields,
                selection, null, null,
                null, null);

        LinkedList<AccelerometerSimpleModel> aL = new LinkedList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
                    float current_x = cursor.getFloat(cursor.getColumnIndex("current_x"));
                    float min_x = cursor.getFloat(cursor.getColumnIndex("min_x"));
                    float max_x = cursor.getFloat(cursor.getColumnIndex("max_x"));
                    float mean_x = cursor.getFloat(cursor.getColumnIndex("mean_x"));
                    float current_y = cursor.getFloat(cursor.getColumnIndex("current_y"));
                    float min_y = cursor.getFloat(cursor.getColumnIndex("min_y"));
                    float max_y = cursor.getFloat(cursor.getColumnIndex("max_y"));
                    float mean_y = cursor.getFloat(cursor.getColumnIndex("mean_y"));
                    float current_z = cursor.getFloat(cursor.getColumnIndex("current_z"));
                    float min_z = cursor.getFloat(cursor.getColumnIndex("min_z"));
                    float max_z = cursor.getFloat(cursor.getColumnIndex("max_z"));
                    float mean_z = cursor.getFloat(cursor.getColumnIndex("mean_z"));
                    long time_ = cursor.getLong(cursor.getColumnIndex("time_"));

                    AccelerometerSimpleModel model = new AccelerometerSimpleModel(user_id,
                            current_x, min_x, max_x, mean_x, current_y, min_y, max_y, mean_y,
                            current_z, min_z, max_z, mean_z, time_);
                    model.setId(cursor.getInt(cursor.getColumnIndex("id")));

                    aL.add(model);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return aL;
    }

    public LinkedList<AccelerometerSimpleModel> getAllAccelerometersFromDatabase() {

        int numberOfElements = AccelerometerSimpleModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements];
        int i = 0;
        for (Field f : AccelerometerSimpleModel.class.getDeclaredFields()) {
            if (!f.getName().contains("$") && !f.getName().equalsIgnoreCase("serialVersionUID")) {
                f.setAccessible(true);
                arrayOfFields[i] = f.getName();
                i++;
            }
        }

        Cursor cursor = myDatabase.query(tableName, arrayOfFields,
                null, null, null, null, "time_");
        LinkedList<AccelerometerSimpleModel> aL = new LinkedList<>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
                    float current_x = cursor.getFloat(cursor.getColumnIndex("current_x"));
                    float min_x = cursor.getFloat(cursor.getColumnIndex("min_x"));
                    float max_x = cursor.getFloat(cursor.getColumnIndex("max_x"));
                    float mean_x = cursor.getFloat(cursor.getColumnIndex("mean_x"));
                    float current_y = cursor.getFloat(cursor.getColumnIndex("current_y"));
                    float min_y = cursor.getFloat(cursor.getColumnIndex("min_y"));
                    float max_y = cursor.getFloat(cursor.getColumnIndex("max_y"));
                    float mean_y = cursor.getFloat(cursor.getColumnIndex("mean_y"));
                    float current_z = cursor.getFloat(cursor.getColumnIndex("current_z"));
                    float min_z = cursor.getFloat(cursor.getColumnIndex("min_z"));
                    float max_z = cursor.getFloat(cursor.getColumnIndex("max_z"));
                    float mean_z = cursor.getFloat(cursor.getColumnIndex("mean_z"));
                    long time_ = cursor.getLong(cursor.getColumnIndex("time_"));

                    AccelerometerSimpleModel model = new AccelerometerSimpleModel(user_id,
                            current_x, min_x, max_x, mean_x, current_y, min_y, max_y, mean_y,
                            current_z, min_z, max_z, mean_z, time_);
                    model.setId(cursor.getInt(cursor.getColumnIndex("id")));

                    aL.add(model);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return aL;
    }

    public String getJSONFromAccelerometersForUploadFromDatabase(int chunk) {
        Gson json = new Gson();
        return json.toJson(getAccelerometersForUploadFromDatabase(chunk));
    }

    public void setUploadToTrue(int lastId) {
        ContentValues values = new ContentValues();
        values.put("upload", true);
        myDatabase.update(Constants.accelerometerTable, values, "id <= ?",
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
