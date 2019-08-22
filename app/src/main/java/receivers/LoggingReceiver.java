package receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.LinkedList;
import java.util.List;

import constants.PreferencesAPI;
import listeners.OnLogAppendedListener;
import models.LogModel;
import preferences.PreferencesManager;
import utilities.LoggingUtils;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class LoggingReceiver extends BroadcastReceiver {

    private OnLogAppendedListener onLogAppendedListener;

    public void setOnLogAppendedListener(OnLogAppendedListener onLogAppendedListener) {
        this.onLogAppendedListener = onLogAppendedListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        List<LogModel> log = new LinkedList<>();
//        if ((Boolean) PreferencesManager.getInstance().get(PreferencesAPI.KEY_SHOW_LOGS)) {
//            log = LoggingUtils.readLogToModelList();
//        }

        if (onLogAppendedListener != null) {
            onLogAppendedListener.onLogAppended(log);
        }
    }
}
