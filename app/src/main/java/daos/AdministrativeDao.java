package daos;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import constants.Constants;
import constants.PreferencesAPI;
import constants.Variables;
import preferences.PreferencesManager;
import timber.log.Timber;

public class AdministrativeDao {
    private SQLiteDatabase myDatabase;
    private String tableName;

    public AdministrativeDao(String dbName, String tableName_, Context context) {
        // TODO Auto-generated constructor stub
        tableName = tableName_;
        myDatabase = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE,
                null);

        String statement = "CREATE TABLE if not exists " + tableName + " ("
                + Constants.serviceColumnName
                + " boolean default FALSE, "
                + Constants.userIdColumnName + " integer, "
                + Constants.speedThresholdColumnName + " double precision, "
                + Constants.urlColumnName + " text " + ")";

        myDatabase.execSQL(statement);
    }

    public boolean getStatus() {
        boolean isOnline = false;

        Cursor cursor1 = myDatabase.query(tableName, null, null, null, null,
                null, null);
        if (cursor1.getCount() > 0)
            while (cursor1.moveToNext()) {
                isOnline = cursor1.getString(
                        cursor1.getColumnIndex(Constants.serviceColumnName))
                        .equalsIgnoreCase("true");
            }

        cursor1.close();

        return isOnline;
    }

    public void setStatus(boolean status) {

        Cursor cursor1 = myDatabase.query(tableName, null, null, null, null,
                null, null);
        try {
            if (cursor1.getCount() > 0)
                myDatabase.execSQL("Update " + tableName + " Set "
                        + Constants.serviceColumnName + " = '" + status + "'");
            else
                myDatabase.execSQL("Insert Into " + tableName + "("
                        + Constants.serviceColumnName + ","
                        + Constants.userIdColumnName + ","
                        + Constants.speedThresholdColumnName + ","
                        + Constants.urlColumnName + ") Values( '" + status
                        + "'," + getUserId() + "," + getSpeedThresh() + ",'" + getURL() + "')");
            cursor1.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
        }

    }

    public int getUserId() {
        int userID = 0;
        Cursor cursor1 = myDatabase.query(tableName, null, null, null, null,
                null, null);
        if (cursor1.getCount() > 0)
            while (cursor1.moveToNext()) {
                userID = cursor1.getInt(cursor1
                        .getColumnIndex(Constants.userIdColumnName));
            }
        else
            cursor1.close();
        return userID;
    }

    public void updateUserId(int userId) {
        Cursor cursor1 = myDatabase.query(tableName, null, null, null, null,
                null, null);
        try {
            if (cursor1 != null) {
                if (cursor1.getCount() > 0)
                    myDatabase.execSQL("Update " + tableName + " Set "
                            + Constants.userIdColumnName + " = " + userId);
                else
                    myDatabase.execSQL("Insert Into " + tableName + "("
                            + Constants.serviceColumnName + ","
                            + Constants.userIdColumnName + ","
                            + Constants.speedThresholdColumnName + ","
                            + Constants.urlColumnName + ") Values('false',"
                            + userId + "," + getSpeedThresh() + ",'" + getURL() + "')");

                cursor1.close();
            }
        } catch (SQLException e) {
            Timber.e(e);
        }

    }

    public String getURL() {
        String url = PreferencesManager.getInstance().get(PreferencesAPI.KEY_SERVER_URL).toString() + Variables.userUrl;
        Cursor cursor1 = myDatabase.query(tableName, null, null, null, null,
                null, null);
        if (cursor1 != null) {
            if (cursor1.getCount() > 0)
                while (cursor1.moveToNext()) {
                    url = cursor1.getString(cursor1
                            .getColumnIndex(Constants.urlColumnName));
                }
            cursor1.close();
        }
        return url;
    }

    public void setURL(String url) {
        Cursor cursor1 = myDatabase.query(tableName, null, null, null, null,
                null, null);
        try {
            if (cursor1 != null) {
                if (cursor1.getCount() > 0)
                    myDatabase.execSQL("Update " + tableName + " Set "
                            + Constants.urlColumnName + " = " + url);
                else
                    myDatabase.execSQL("Insert Into " + tableName + "("
                            + Constants.serviceColumnName + ","
                            + Constants.userIdColumnName + ","
                            + Constants.speedThresholdColumnName + ","
                            + Constants.urlColumnName + ") Values('false',"
                            + getUserId() + "," + getSpeedThresh() + ",'" + url + "')");
                cursor1.close();
            }
        } catch (SQLException e) {
            Timber.d(e);
        }
    }

    public double getSpeedThresh() {
        // Removed since the parameter settings is added
//		Cursor cursor1 = myDatabase.query(tableName, null, null, null, null,
//				null, null);
//		if (cursor1.getCount() > 0)
//			while (cursor1.moveToNext()) {
//				url = cursor1.getDouble(cursor1
//						.getColumnIndex(Constants.speedThresholdColumnName));
//			}
//		cursor1.close();
        return Double.parseDouble(PreferencesManager.getInstance().get(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD).toString());
    }

    public void setSpeedTresh(double speed) {
        Cursor cursor1 = myDatabase.query(tableName, null, null, null, null,
                null, null);
        try {
            if (cursor1 != null) {
                if (cursor1.getCount() > 0)
                    myDatabase.execSQL("Update " + tableName + " Set "
                            + Constants.speedThresholdColumnName + " = " + speed);
                else
                    myDatabase.execSQL("Insert Into " + tableName + "("
                            + Constants.serviceColumnName + ","
                            + Constants.userIdColumnName + ","
                            + Constants.speedThresholdColumnName + ","
                            + Constants.urlColumnName + ") Values('false',"
                            + getUserId() + "," + speed + ",'" + getURL() + "')");
                cursor1.close();
            }
        } catch (SQLException e) {
            Timber.d(e);
        }
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
