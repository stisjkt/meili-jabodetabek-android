package activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import constants.SessionManagerAPI;
import id.ac.stis.meili.R;
import io.fabric.sdk.android.Fabric;
import preferences.SessionManager;

/**
 * Created by Rahadi on 01/03/2018.
 */

public class SplashActivity extends Activity {

    private static final int REQUEST_RESULT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(linearLayoutParams);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(350, 350);
        iconParams.setMargins(0, 0, 0, 50);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_mobility);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(50, 0, 50, 0);

        TextView title = new TextView(this);
        title.setLayoutParams(titleParams);
        title.setText(R.string.app_name);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(Typeface.create(title.getTypeface(), Typeface.BOLD));

        linearLayout.addView(icon);
        linearLayout.addView(title);

        setContentView(linearLayout);

        runtimePermissionCheck();
    }

    private void launchActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> userSession = new SessionManager(SplashActivity.this).getUserSession();
                String username = userSession.get(SessionManagerAPI.Keys.KEY_USERNAME);

                if (username != null) {
//                    startActivity(new Intent(SplashActivity.this, ServiceHandlingActivity.class));
                    startActivity(new Intent(SplashActivity.this, ServiceActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }

                finish();
            }
        }, 1500);
    }

    /*
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   <uses-permission android:name="android.permission.WAKE_LOCK" />
   <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   */
    private void runtimePermissionCheck() {
        List<String> needsPermission = new LinkedList<>();

        String[] permissionList = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
        };

        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                needsPermission.add(permission);
            }
        }

        if (needsPermission.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needsPermission.toArray(new String[needsPermission.size()]), REQUEST_RESULT_CODE);
        } else {
            launchActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RESULT_CODE) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                runtimePermissionCheck();
            } else {
                launchActivity();
            }
        }
    }
}
