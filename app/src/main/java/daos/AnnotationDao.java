package daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import models.AnnotationModel;
import timber.log.Timber;

public class AnnotationDao {

    private SQLiteDatabase myDatabase;
    private String annotationTableName;

    public AnnotationDao(String databaseName, String annotationTableName,
                         Context context) {
        this.annotationTableName = annotationTableName;
        myDatabase = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE,
                null);
        LinkedList<LinkedHashMap<String, String>> listOfMaps = new LinkedList<>();
        listOfMaps.add(AnnotationModel.getAllElements());
        myDatabase.execSQL(utilities.GetInfo.generateSqlStatement(
                annotationTableName, listOfMaps));
    }

    public boolean insertAnnotationIntoDatabase(AnnotationModel aV) {
        ContentValues newValues = new ContentValues();

        for (Field f : AnnotationModel.class.getDeclaredFields()) {
            f.setAccessible(true);

            String name = f.getName();
            String value;
            try {
                value = f.get(aV).toString();
                newValues.put(name, value);
            } catch (IllegalArgumentException e) {
                Timber.e(e);
                return false;
            } catch (IllegalAccessException e) {
                Timber.e(e);
                return false;
            }

        }

        myDatabase.insert(annotationTableName, null, newValues);
        newValues.clear();

        return false;
    }

    public LinkedList<AnnotationModel> getAllAnnotationsFromDatabase() {

        int numberOfElements = AnnotationModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements];
        int i = 0;
        for (Field f : AnnotationModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            arrayOfFields[i] = f.getName();
            i++;
        }

        Cursor cursor = myDatabase.query(annotationTableName, arrayOfFields,
                null, null, null, null, null);

        LinkedList<AnnotationModel> annotationList = new LinkedList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {

                /*
                 * LocationModel specific part
                 */
                int userid = cursor.getInt(cursor.getColumnIndex("userid"));
                long annotationStartTime = cursor.getLong(cursor
                        .getColumnIndex("annotationStartTime"));
                long annotationStopTime = cursor.getLong(cursor
                        .getColumnIndex("annotationStopTime"));
                String annotationValues = cursor.getString(cursor
                        .getColumnIndex("annotationValues"));
                AnnotationModel aVal = new AnnotationModel(userid,
                        annotationStartTime, annotationStopTime, annotationValues);

                annotationList.add(aVal);
            }
            cursor.close();
        }
        return annotationList;
    }

    private LinkedList<AnnotationModel> getAllAnnotationsForUpload() {

        int numberOfElements = AnnotationModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements];
        int i = 0;
        for (Field f : AnnotationModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            arrayOfFields[i] = f.getName();
            i++;
        }

        Cursor cursor = myDatabase.query(annotationTableName, arrayOfFields,
                "upload=?", new String[]{"FALSE"}, null, null, null);

        LinkedList<AnnotationModel> annotationList = new LinkedList<>();

        while (cursor.moveToNext()) {

            /*
             * LocationModel specific part
             */
            int userid = cursor.getInt(cursor.getColumnIndex("userid"));
            long annotationStartTime = cursor.getLong(cursor
                    .getColumnIndex("annotationStartTime"));
            long annotationStopTime = cursor.getLong(cursor
                    .getColumnIndex("annotationStopTime"));
            String annotationValues = cursor.getString(cursor
                    .getColumnIndex("annotationValues"));
            cursor.close();
            AnnotationModel aVal = new AnnotationModel(userid,
                    annotationStartTime, annotationStopTime, annotationValues);

            annotationList.add(aVal);
        }
        return annotationList;
    }

    public String getJSONFromAllForUpload() {
        Gson json = new Gson();
        return json.toJson(getAllAnnotationsForUpload());
    }

    public void setUploadToTrue() {
        ContentValues values = new ContentValues();
        values.put("upload", "TRUE");
        myDatabase.update(annotationTableName, values, null, null);
    }

    public AnnotationModel getLastInsertedAnnotation() {

        int numberOfElements = AnnotationModel.class.getDeclaredFields().length;
        String[] arrayOfFields = new String[numberOfElements];
        int i = 0;
        for (Field f : AnnotationModel.class.getDeclaredFields()) {
            f.setAccessible(true);
            arrayOfFields[i] = f.getName();
            i++;
        }

        Cursor cursor = myDatabase.query(annotationTableName, arrayOfFields,
                null, null, null, null, "id desc", "1");
        AnnotationModel lastAnnotation = null;

        while (cursor.moveToNext()) {

            /*
             * LocationModel specific part
             */
            int userid = cursor.getInt(cursor.getColumnIndex("userid"));
            long annotationStartTime = cursor.getLong(cursor
                    .getColumnIndex("annotationStartTime"));
            long annotationStopTime = cursor.getLong(cursor
                    .getColumnIndex("annotationStopTime"));
            String annotationValues = cursor.getString(cursor
                    .getColumnIndex("annotationValues"));
            cursor.close();
            lastAnnotation = new AnnotationModel(userid, annotationStartTime,
                    annotationStopTime, annotationValues);
        }

        return lastAnnotation;
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
