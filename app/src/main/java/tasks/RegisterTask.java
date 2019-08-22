package tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import activities.ServiceActivity;
import applications.Meili;
import constants.Constants;
import constants.PreferencesAPI;
import constants.Variables;
import daos.AdministrativeDao;
import preferences.PreferencesManager;
import preferences.SessionManager;
import timber.log.Timber;

public class RegisterTask extends AsyncTask<Void, Void, String> {

    private Context mContext;
    private String userName;
    private String passWord;
    private String phoneModel;
    private String sdkInt;
    private String phoneNumber;
    private String regNo;

    private Activity referencingActivity;

    public RegisterTask(Activity ref, String username, String password,
                        Context ctx, String phoneModel, String sdkInt, String phoneNumber, String regNo) {
        this.referencingActivity = ref;
        this.mContext = ctx;
        this.userName = username;
        this.passWord = password;
        this.phoneModel = phoneModel;
        this.sdkInt = sdkInt;
        this.phoneNumber = phoneNumber;
        this.regNo = regNo;
    }

    protected String doInBackground(Void... params) {
        URL url;
        try {
            url = new URL(PreferencesManager.getInstance().get(PreferencesAPI.KEY_SERVER_URL) +
                    Variables.userUrl + Variables.userRegisterEndpoint);
            HttpURLConnection urlConn = (HttpURLConnection) url
                    .openConnection();

            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setUseCaches(false);
            urlConn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            urlConn.setRequestProperty("Charset", "utf-8");
            // to connect to the server side
            urlConn.connect();

            DataOutputStream dop = new DataOutputStream(
                    urlConn.getOutputStream());
            dop.writeBytes("method=" + URLEncoder.encode("register", "utf-8"));
            // it is essential that to add "&" to separate two strings
            dop.writeBytes("&username=" + URLEncoder.encode(userName, "utf-8"));
            dop.writeBytes("&password=" + URLEncoder.encode(passWord, "utf-8"));
            dop.writeBytes("&phone_model=" + URLEncoder.encode(phoneModel, "utf-8"));
            dop.writeBytes("&phone_os=" + URLEncoder.encode(sdkInt, "utf-8"));
            dop.writeBytes("&phone_number=" + URLEncoder.encode(phoneNumber, "utf-8"));
            dop.writeBytes("&reg_no=" + URLEncoder.encode(regNo, "utf-8"));
            dop.flush();
            dop.close();
            BufferedReader dis = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String locPassage = dis.readLine();
            Timber.d("Response : %s", locPassage);
            dis.close();
            // to disconnect the server side
            urlConn.disconnect();


            if (locPassage.equalsIgnoreCase("email exists")) return "email exists";
            if (locPassage.equalsIgnoreCase("phonenumber exists")) return "phonenumber exists";
            if (locPassage.equalsIgnoreCase("invalid regno")) return "invalid regno";

            return locPassage;
        } catch (MalformedURLException e) {
            Timber.e(e);
        } catch (IOException e) {
            Timber.e(e);
        }
        return "Failed";

    }

    protected void onPostExecute(String result) {
        if (result.equalsIgnoreCase("email exists")) {
            Toast.makeText(mContext, "Email telah terdaftar", Toast.LENGTH_LONG).show();
        } else if (result.equalsIgnoreCase("phonenumber exists")) {
            Toast.makeText(mContext, "No. Telp. telah terdaftar", Toast.LENGTH_LONG).show();
        } else if (result.equalsIgnoreCase("invalid data")) {
            Toast.makeText(mContext, "Data registrasi tidak lengkap", Toast.LENGTH_LONG).show();
        } else if (result.equalsIgnoreCase("invalid regno")) {
            Toast.makeText(mContext, "Harap mengisi No. Registrasi dengan benar", Toast.LENGTH_LONG).show();
        } else {
            String uuid;
            String token = "token";
            try {
                JSONArray jArray = new JSONArray(result);
                uuid =  jArray.getJSONObject(0).getInt("id") + "";
                token = jArray.getJSONObject(0).getString("device_session");
            } catch (Exception e) {
                uuid =  "Failed";
            }
            if (uuid.equals("Failed")) {
                Toast.makeText(mContext, "Registrasi Gagal. Silahkan Ulangi Kembali", Toast.LENGTH_LONG).show();
            } else{

                Timber.d("Register Result : %s", result);
                Toast.makeText(mContext, "Successful registration",
                        Toast.LENGTH_LONG).show();

                AdministrativeDao adminDb = new AdministrativeDao(
                        Constants.databaseName, Constants.adminTable, mContext);

                adminDb.updateUserId(Integer.valueOf(uuid.replace(" ", "")));
                adminDb.closeDb();

                Date date = new Date(System.currentTimeMillis());
                String formattedDate = SimpleDateFormat.getInstance().format(date);
                if (userName.trim().isEmpty()) {
                    userName = phoneNumber;
                }
                new SessionManager(referencingActivity).setUserSession(userName, passWord, token, formattedDate);

                LoginTask.sendDeviceInfo(Meili.getInstance().getApplicationContext(),userName);
                LoginTask.syncPreferences(Meili.getInstance().getApplicationContext(),userName);

                this.referencingActivity.startActivity(new Intent(
                        this.referencingActivity.getApplicationContext(),
                        ServiceActivity.class));

                this.referencingActivity.finish();
            }
        }
        super.onPostExecute(result);
    }

}