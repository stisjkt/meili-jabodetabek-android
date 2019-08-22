package utilities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import constants.PreferencesAPI;
import preferences.PreferencesManager;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class ServiceUtils {

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void startCollectService(Context context, Intent collectingServiceIntent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(collectingServiceIntent);
        } else {
            context.startService(collectingServiceIntent);
        }
    }

}
