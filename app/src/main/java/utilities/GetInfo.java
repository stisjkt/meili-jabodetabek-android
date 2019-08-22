package utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import constants.Constants;
import daos.AdministrativeDao;
import timber.log.Timber;

public class GetInfo {
    private Context mContext;
    private AdministrativeDao adminDb;

    /**
     * Gets information specific to the project
     *
     * @param ctx context
     */

    public GetInfo(Context ctx) {
        this.mContext = ctx;
        adminDb = new AdministrativeDao(Constants.databaseName,
                Constants.adminTable, mContext);

    }

    public static String convertTypeJavaToSql(String s) {
        String converted = "";

        if (s.equalsIgnoreCase("double"))
            converted = "double precision";
        if (s.equalsIgnoreCase("float"))
            converted = "real";
        if (s.equalsIgnoreCase("long"))
            converted = "bigint";
        if (s.equalsIgnoreCase("int"))
            converted = "integer";
        if (s.equalsIgnoreCase("boolean"))
            converted = "boolean";
        if (s.equalsIgnoreCase("string"))
            converted = "character varying(30)";
        if (s.equalsIgnoreCase("java.lang.string"))
            converted = "character varying(30)";

        // Timber.d("Converted from %s to %s", s, converted);

        return converted;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String generateSqlStatement(String nameOfTable,
                                              LinkedList<LinkedHashMap<String, String>> listOfMaps) {
        nameOfTable = nameOfTable.replace(" ", "_");
        StringBuilder statement = new StringBuilder("CREATE TABLE if not exists "
                + nameOfTable
                + " (id integer primary key autoincrement, upload boolean default FALSE, ");
        for (LinkedHashMap<String, String> map : listOfMaps) {
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) iterator.next();

                if (!pairs.getValue().contains("$") && !pairs.getKey().contains("$"))
                    statement.append(pairs.getKey()).append(" ").append(pairs.getValue()).append(" , ");
                iterator.remove(); // avoids a ConcurrentModificationException
            }
        }

//        HashMap<String, String> prefMap = PreferencesAPI.DB_COLUMNS;
//        for (String key : prefMap.keySet()) {
//            statement = statement + key + " " + prefMap.get(key) + " , ";
//        }

        statement = new StringBuilder(statement.substring(0, statement.length() - 3));

        statement.append(")");

        return statement.toString();
    }

    /**
     * @return true if the device is connected to the internet
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null) {
            netInfo = cm.getActiveNetworkInfo();
        }
        return netInfo != null && netInfo.isConnected();

    }

    public boolean isServiceOn() {
        return adminDb.getStatus();
    }

    public void setServiceOn() {
        Timber.d("SET SERVICE ON");
        adminDb.setStatus(true);
    }

    public void setServiceOff() {
        Timber.d("SET SERVICE OFF");

        adminDb.setStatus(false);
    }

    @Override
    protected void finalize() throws Throwable {
        adminDb.closeDb();
        super.finalize();
    }

    public boolean attemptConnect(String string) {
        return false;
    }

}
