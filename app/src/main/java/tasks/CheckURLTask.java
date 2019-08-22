package tasks;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import timber.log.Timber;

public class CheckURLTask extends AsyncTask<Void, Void, String> {

	URL url = null;
	Context mContext;
	String testURL;

	public CheckURLTask(String newUrl, Context ctx) {
		mContext = ctx;
		testURL = newUrl;
	}

	protected String doInBackground(Void... params) {
		URL url = null;
		try {
			url = new URL((testURL + "/CheckConnection"));

			InputStream stream = url.openStream();

			DataInputStream dis = new DataInputStream(stream);
			String locPassage = dis.readUTF();

			dis.close();

			stream.close();

			return locPassage;
		} catch (MalformedURLException e) {
            Timber.e(e);
			return "nothing";
		} catch (IOException e) {
            Timber.e(e);
			return "nothing";
		}

	}

	protected void onPostExecute(String result) {
		if (result.equals("nothing")) {
			Toast.makeText(mContext, "Failure", Toast.LENGTH_LONG).show();
		} else
			Toast.makeText(mContext, "The connection is successful",
					Toast.LENGTH_LONG).show();
		super.onPostExecute(result);
	}

}