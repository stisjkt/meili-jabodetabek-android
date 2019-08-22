package tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import applications.Meili;
import constants.PreferencesAPI;
import constants.Variables;
import daos.CentralDao;
import id.ac.stis.meili.R;
import preferences.PreferencesManager;
import timber.log.Timber;
import utilities.LoggingUtils;

public class UploadTask extends AsyncTask<Void, Void, Integer> {

    public static final int STATUS_NO_DATA = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_IN_PROGRESS = 2;
    public static final int STATUS_ERROR = -1;

    private CentralDao centralDB;
    private OnUploadCompleted listener;
    private int uploadChunk, iteration;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    public UploadTask(Context ctx) {
        centralDB = new CentralDao(ctx);
        this.uploadChunk = Integer.valueOf(PreferencesManager.getInstance()
                .get(PreferencesAPI.KEY_UPLOAD_CHUNK).toString());

        Meili.getInstance().setUploading(true);
        this.mContext=ctx;
    }

    public void setListener(OnUploadCompleted listener) {
        this.listener = listener;
    }

    protected Integer doInBackground(Void... params) {
        URL url;


        String upload;
        while (!(upload = centralDB.getUploadStatement(uploadChunk))
                .equalsIgnoreCase("method=upload&embeddedLocations_=[]&accelerometers_=[]&simpleLocations_=")) {
            Timber.d("Upload Data : %s", upload);

            iteration++;

            LoggingUtils.i(LoggingUtils.TAG_UPLOAD, "Uploading Data to the Server..");
            LoggingUtils.i(mContext, LoggingUtils.TAG_UPLOAD, mContext.getString(R.string.tag_uploading));
            try {
                url = new URL(PreferencesManager.getInstance().get(PreferencesAPI.KEY_SERVER_URL) +
                        Variables.userUrl + Variables.locationUploadEndpoint);
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

                DataOutputStream dop = new DataOutputStream(urlConn.getOutputStream());
                dop.writeBytes(upload);
                dop.flush();
                dop.close();
                BufferedReader dis = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String locPassage = dis.readLine();

                Timber.d("Response : %s", locPassage);
                dis.close();

                if (!"OK".equalsIgnoreCase(locPassage)) {
                    return STATUS_ERROR;
                } else {
                    centralDB.setUploadToTrue(centralDB.getLocationLastId(), centralDB.getAccelerometerLastId());
                    LoggingUtils.i(LoggingUtils.TAG_UPLOAD, "Data Partially Uploaded.");
                    LoggingUtils.i(mContext, LoggingUtils.TAG_UPLOAD, mContext.getString(R.string.tag_upload_partial));
                }
            } catch (MalformedURLException e) {
                Timber.e(e);
                LoggingUtils.e(LoggingUtils.TAG_UPLOAD,
                        "Data Upload Failed. URL Malformed. " + e.getMessage());
                LoggingUtils.i(mContext, LoggingUtils.TAG_UPLOAD, mContext.getString(R.string.tag_upload_failed),
                        e.getMessage());
                return STATUS_ERROR;
            } catch (IOException e) {
                Timber.e(e);
                LoggingUtils.e(LoggingUtils.TAG_UPLOAD,
                        "Data Upload Failed. IOException. " + e.getMessage());
                LoggingUtils.i(mContext, LoggingUtils.TAG_UPLOAD, mContext.getString(R.string.tag_upload_failed),
                        e.getMessage());
                return STATUS_ERROR;
            }
        }

        if (iteration > 0) {
            return STATUS_SUCCESS;
        } else {
            return STATUS_NO_DATA;
        }

    }

    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Meili.getInstance().setUploading(false);

        if (result == STATUS_SUCCESS) {
            centralDB.setUploadToTrue(centralDB.getLocationLastId(), centralDB.getAccelerometerLastId());
            LoggingUtils.i(LoggingUtils.TAG_UPLOAD, "Data Successfully Uploaded.");
            LoggingUtils.i(mContext, LoggingUtils.TAG_UPLOAD, mContext.getString(R.string.tag_upload_success));
        } else if (result == STATUS_NO_DATA) {
            LoggingUtils.i(LoggingUtils.TAG_UPLOAD, "There is No Data to be Uploaded.");
            LoggingUtils.i(mContext, LoggingUtils.TAG_UPLOAD, mContext.getString(R.string.tag_upload_no_data));
        } else {
            LoggingUtils.e(LoggingUtils.TAG_UPLOAD, "Data Upload Failed.");
            LoggingUtils.i(mContext, LoggingUtils.TAG_UPLOAD, mContext.getString(R.string.tag_upload_failed));
        }

        if (listener != null) {
            listener.onUploadCompleted(result);
        }

    }

    public interface OnUploadCompleted {
        void onUploadCompleted(int status);
    }

}
