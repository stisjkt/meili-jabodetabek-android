package activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import constants.PreferencesAPI;
import constants.SessionManagerAPI;
import constants.Variables;
import id.ac.stis.meili.R;
import listeners.OnWebInterfaceCalledListener;
import preferences.PreferencesManager;
import preferences.SessionManager;
import timber.log.Timber;
import utilities.WebAppInterface;

import static android.view.View.GONE;

/**
 * Created by Rahadi on 15/03/2018.
 */

public class AnnotationActivity extends Activity {

    private WebView mainContent;
    private ProgressBar progressBar;
    private ImageButton reloadButton, pageButton, prevButton, nextButton,
            saveButton, undoButton;
    private Activity activity;

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (String aChildren : children) {
                    deletedAll = deleteFile(new File(file, aChildren)) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && mainContent.canGoBack()) {
//            mainContent.goBack();
//            return true;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }

    private void setupAnnotationButton(final WebView webView, boolean toOpen, boolean toAnnotate) {
        if (toOpen) {
            pageButton.setImageResource(R.drawable.annotate_button);
            pageButton.setOnClickListener(null);
            pageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    webView.loadUrl("javascript: (function() {openNavAndroid();})()");
                }
            });

            saveButton.setVisibility(GONE);
            saveButton.setOnClickListener(null);
        } else {
            pageButton.setImageResource(R.drawable.close_button);
            pageButton.setOnClickListener(null);
            pageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    webView.loadUrl("javascript: (function() {closeNavAndroid();})()");
                }
            });

            if (toAnnotate) {
                saveButton.setVisibility(View.VISIBLE);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        webView.loadUrl("javascript: (function() {annotateTripAndroid();})()");
                    }
                });
            } else {
                saveButton.setVisibility(GONE);
                saveButton.setOnClickListener(null);
            }
        }
    }

    private void requestPage() {
        try {
            SessionManager sm = new SessionManager(this);
            String username = sm.getUserSession().get(SessionManagerAPI.Keys.KEY_USERNAME);
            String password = sm.getUserSession().get(SessionManagerAPI.Keys.KEY_PASSWORD);


            String postData = "username=" + URLEncoder.encode(username, "UTF-8")
                    + "&password=" + URLEncoder.encode(password, "UTF-8");
            mainContent.postUrl(PreferencesManager.getInstance().get(PreferencesAPI.KEY_SERVER_URL).toString()
                    + Variables.webViewUrl, postData.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        activity = this;

        setContentView(R.layout.activity_annotation);

        mainContent = findViewById(R.id.main_content);
        progressBar = findViewById(R.id.progressBar);
        reloadButton = findViewById(R.id.reload_button);
        pageButton = findViewById(R.id.page_button);
        prevButton = findViewById(R.id.prev_button);
        nextButton = findViewById(R.id.next_button);
        saveButton = findViewById(R.id.save_button);
        undoButton = findViewById(R.id.undo_button);

        final String login = PreferencesManager.getInstance().get(PreferencesAPI.KEY_SERVER_URL).toString()
                + Variables.login;
        final String beranda = PreferencesManager.getInstance().get(PreferencesAPI.KEY_SERVER_URL).toString()
                + Variables.beranda;

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPage();
            }
        });

        prevButton.setVisibility(GONE);
        nextButton.setVisibility(GONE);
        pageButton.setVisibility(GONE);

        requestPage();

        syncRecording(getApplicationContext());

        mainContent.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, final String url) {
                progressBar.setVisibility(GONE);

                Timber.d("FINISHED : %s", url);
//                    mainContent.setVisibility(View.VISIBLE);

//                Toast.makeText(AnnotationActivity.this, "Page Loading Finished\n" + url,
//                        Toast.LENGTH_LONG).show();


//                view.loadUrl("javascript: (function() {" +
//                        "$(document).ready(function () {" +
//                        "$('#open').remove();" +
//                        "$('#close').remove();" +
//                        "$('#logout-button').remove();" +
//                        "});" +
//                        "})()");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                view.loadUrl("javascript: (function() {hideForAndroid();})()");
                            }
                        }, 1000);
//                mainContent.loadUrl("javascript: (function() {notifyReadyAndroid();})()");



                pageButton.setVisibility(View.VISIBLE);
                prevButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);

//                setupAnnotationButton(view, true, false);
                pageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.loadUrl("javascript: (function() {switchNavAndroid();})()");
                    }
                });


                prevButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        mainContent.loadUrl("javascript: (function() {previewPrevTripAndroid()})()");
//                            mainContent.loadUrl("javascript: (function() {" +
//                                    "loadingOverlay();" +
//                                    "tempTimeline.emit('move-to-previous-trip', tempTimeline.trip);" +
//                                    "closeNav();" +
//                                    "})()");
                        mainContent.loadUrl("javascript: (function() {tempTimeline.previewPreviousTripMobility()})()");
                    }
                });

                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        mainContent.loadUrl("javascript: (function() {previewNextTripAndroid()})()");
//                        mainContent.loadUrl("javascript: (function() {" +
//                                "loadingOverlay();" +
//                                "tempTimeline.emit('move-preview-next-trip', tempTimeline.trip);" +
//                                "closeNav();" +
//                                "})()");
                        mainContent.loadUrl("javascript: (function() {tempTimeline.previewNextTripMobility()})()");
                    }
                });

                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);

                    progressBar.setVisibility(View.VISIBLE);
                    pageButton.setVisibility(GONE);
                    prevButton.setVisibility(GONE);
                    nextButton.setVisibility(GONE);

                    saveButton.setVisibility(GONE);
                    undoButton.setVisibility(GONE);

                    Timber.d("Current URL : %s", url);
//                    mainContent.setVisibility(View.GONE);
                }
        });

        mainContent.setWebChromeClient(new WebChromeClient() {
                                           public void onProgressChanged(WebView view, int progress) {
                                               progressBar.setProgress(progress);
            }
        });//end resources

        mainContent.addJavascriptInterface(new WebAppInterface(new OnWebInterfaceCalledListener() {
            @Override
            public void onEvent(String type) {
                Timber.d("TYPE : %s", type);

                if (type.equalsIgnoreCase("openNav")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            setupAnnotationButton(mainContent, false, false);
                        }
                    });
                } else if (type.equalsIgnoreCase("openNavAnnotate")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupAnnotationButton(mainContent, false, true);
                        }
                    });
                } else if (type.equalsIgnoreCase("closeNav")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupAnnotationButton(mainContent, true, false);
                        }
                    });
                } else if (type.equalsIgnoreCase("ready")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainContent.loadUrl("javascript: (function() {hideForAndroid();})()");
                        }
                    });
                }
            }
        }), "Android");

        WebSettings webSettings = mainContent.getSettings();
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            webSettings.setJavaScriptEnabled(true);
        }

    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            switch (extension) {
                case "js":
                    return "text/javascript";
                case "woff":
                    return "application/font-woff";
                case "woff2":
                    return "application/font-woff2";
                case "ttf":
                    return "application/x-font-ttf";
                case "eot":
                    return "application/vnd.ms-fontobject";
                case "svg":
                    return "image/svg+xml";
            }
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public void clearApplicationData() {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    private void syncRecording(final Context mContext){
        String username = PreferencesManager.getInstance().get(PreferencesAPI.KEY_USERNAME).toString();

        FirebaseApp.initializeApp(mContext);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference meiliDB = ref
                .child("meili")
                .child(username)
                .child("preference");
        meiliDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(PreferencesAPI.KEY_RECORDING).exists()) {
                    if (!(Boolean) dataSnapshot.child(PreferencesAPI.KEY_RECORDING).getValue()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            activity.finishAndRemoveTask();
                            clearApplicationData();
                            System.exit(0);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            activity.finishAffinity();
                            clearApplicationData();
                            System.exit(0);
                        } else {
                            activity.finish();
                            clearApplicationData();
                            System.exit(0);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        meiliDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() instanceof Boolean && dataSnapshot.getKey().equals(PreferencesAPI.KEY_RECORDING)){
                    if(!(Boolean)dataSnapshot.getValue()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            activity.finishAndRemoveTask();
                            clearApplicationData();
                            System.exit(0);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            activity.finishAffinity();
                            clearApplicationData();
                            System.exit(0);
                        } else {
                            activity.finish();
                            clearApplicationData();
                            System.exit(0);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
