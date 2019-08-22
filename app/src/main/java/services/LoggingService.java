package services;

import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;

import constants.Variables;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class LoggingService extends Service {

    private Intent intent;
    private FileObserver fileObserver;

    @Override
    public void onCreate() {
        super.onCreate();

        intent = new Intent(Variables.logFilter);
        fileObserver = new FileObserver(Variables.logFilePath) {
            @Override
            public void onEvent(int i, String s) {
                if (i == FileObserver.MODIFY) {
                    broadcastLogUpdate();
                }
            }
        };
    }

    private void broadcastLogUpdate() {
        sendBroadcast(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        fileObserver.startWatching();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        fileObserver.stopWatching();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
