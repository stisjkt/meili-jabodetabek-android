package receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;

import id.ac.stis.meili.R;
import preferences.SessionManager;
import services.CollectingService;
import timber.log.Timber;
import utilities.LoggingUtils;
import utilities.ServiceUtils;

public class OnBootReceiver extends BroadcastReceiver {

    private Class serviceClass;
    private Intent collectingServiceIntent;
    private HashMap<String, String> userSession;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        // CHECK IF THE SERVICE WAS RUNNING WHEN THE PHONE DIED
        serviceClass = CollectingService.class;
        collectingServiceIntent = new Intent(context, serviceClass);
        userSession = new SessionManager(context).getUserSession();

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            LoggingUtils.i(LoggingUtils.TAG_SYSTEM,
                    "Booting Completed.");
            LoggingUtils.i(context, LoggingUtils.TAG_SYSTEM, context.getString(R.string.tag_system_booting_completed));

            if (!ServiceUtils.isMyServiceRunning(context, serviceClass)) {
                LoggingUtils.i(LoggingUtils.TAG_SERVICE,
                        "Service is Off. Turning On..");
                LoggingUtils.i(context, LoggingUtils.TAG_SERVICE, context.getString(R.string.tag_service_off),
                        context.getString(R.string.tag_service_turning_on));
                try {
                    //context.startService(collectingServiceIntent);
                    ServiceUtils.startCollectService(context,collectingServiceIntent);
                    new SessionManager(context).updateDate();
                    LoggingUtils.i(LoggingUtils.TAG_SERVICE,
                            "Service Successfully Turned On.");
                    LoggingUtils.i(context, LoggingUtils.TAG_SERVICE, context.getString(R.string.tag_service_off),
                            context.getString(R.string.tag_service_success));
                } catch (Exception e) {
                    Timber.e(e);
                    new SessionManager(context).updateDate();
                    LoggingUtils.e(LoggingUtils.TAG_SERVICE,
                            "Failed to Turn On the Service. " + e.getMessage());
                    LoggingUtils.i(context, LoggingUtils.TAG_SERVICE, context.getString(R.string.tag_service_off),
                            context.getString(R.string.tag_service_failed_on), e.getMessage());
                }
            }
        }

    }

}
