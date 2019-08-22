package applications;

import android.app.Application;

import timber.log.Timber;

public class Meili extends Application {

    private static Meili singleton = null;

    public boolean uploading;
    public String helloFromGlobalApplication = "foo text";

    public static Meili getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        singleton = this;

        Timber.plant(new Timber.DebugTree());

        super.onCreate();
    }

    public boolean isUploading() {
        return uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }

    public void setAccelerometerUploading(boolean uploading) {
        this.uploading = uploading;
    }
}
