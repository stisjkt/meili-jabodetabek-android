package activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.PasswordTransformationMethod;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import applications.Meili;
import constants.Constants;
import id.ac.stis.meili.R;
import tasks.LoginTask;
import tasks.UploadTask;
import timber.log.Timber;
import utilities.GetInfo;


public class LoginActivity extends Activity {

    boolean isOnline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());

        final GetInfo gI = new GetInfo(this);

        isOnline = gI.isOnline();

        //gI.closeThisDb();

        UploadTask uploadTask = new UploadTask(this);
        uploadTask.execute();

        /*
         *
         * FOR FIRST LOGIN USE THE ADMIN PAGE TO CHECK IF THE CONNECTION TO THE
         * SERVER IS SUCCESSFULL
         */

        ScrollView currentScrollView = new ScrollView(this);

        final LinearLayout currentLineraLayout = new LinearLayout(this);

        currentLineraLayout.setOrientation(LinearLayout.VERTICAL);

        @SuppressWarnings("deprecation")
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);

        currentScrollView.addView(currentLineraLayout);

        TextView titleText = new TextView(this);
        titleText.setSingleLine(true);

        titleText.setText(Constants.titleText);
        titleText.setPadding(0, 0, 0, 50);
        titleText.setTextSize(20);
        titleText.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView emailText = new TextView(this);
        emailText.setSingleLine(true);
        emailText.setText(Constants.regularUsernameText);
        emailText.setPadding(0, 0, 0, 20);
        emailText.setTextSize(15);
        emailText.setGravity(Gravity.START);

        TextView passwordText = new TextView(this);
        passwordText.setSingleLine(true);
        passwordText.setText(Constants.regularPasswordText);
        passwordText.setPadding(0, 0, 0, 20);
        passwordText.setTextSize(15);
        passwordText.setGravity(Gravity.START);

        TextView registerText = new TextView(this);
        registerText.setSingleLine(true);
        SpannableString mySpannableString = new SpannableString(
                Constants.registerNewUserText);
        mySpannableString.setSpan(new UnderlineSpan(), 0,
                mySpannableString.length(), 0);
        registerText.setText(mySpannableString);
        registerText.setPadding(0, 50, 0, 20);
        registerText.setTextSize(20);
        registerText.setTextColor(0x990033CC);
        registerText.setGravity(Gravity.CENTER_HORIZONTAL);

        registerText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        final EditText emailEdit = new EditText(this);
        emailEdit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        final EditText passwordEdit = new EditText(this);
        passwordEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod
                .getInstance());

        Button loginButton = new Button(this);
        loginButton.setText(Constants.loginText);
        loginButton.setGravity(Gravity.CENTER_HORIZONTAL);

        // layoutParams.setMargins(100, 500, 100, 200);

        TextView contentText = new TextView(this);
        contentText.setText(Constants.contentText);
        contentText.setTextSize(15);
        contentText.setGravity(Gravity.START);
        contentText.setPadding(0, 0, 0, 50);
        contentText.setVisibility(View.GONE);

        currentLineraLayout.addView(titleText, layoutParams);
        currentLineraLayout.addView(emailText);
        currentLineraLayout.addView(emailEdit);
        currentLineraLayout.addView(passwordText);
        currentLineraLayout.addView(passwordEdit);
        currentLineraLayout.addView(loginButton, layoutParams);
        currentLineraLayout.addView(registerText);
        currentLineraLayout.addView(contentText);

        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isOnline) {
                    if (completeCredentialsPassed())
                        loginComplete();
                } else
                    Toast.makeText(
                            LoginActivity.this,
                            Constants.internetConnectionWarning,
                            Toast.LENGTH_LONG).show();
            }

            private boolean loginComplete() {
                new LoginTask(LoginActivity.this, emailEdit.getText().toString(),
                        passwordEdit.getText().toString(),
                        LoginActivity.this).execute();
                return true;
            }

            private boolean completeCredentialsPassed() {
                String username = emailEdit.getText().toString();
                if (username.contains("@")) {
                    if (!isEmailValid(username)) {
                        Toast.makeText(LoginActivity.this,
                                Constants.emailWarning,
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                } else {
                    if (!isPhoneValid(username)) {
                        Toast.makeText(LoginActivity.this,
                                Constants.phoneNumberWarning,
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                String password = passwordEdit.getText().toString();
                if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this,
                            Constants.passwordEmpty,
                            Toast.LENGTH_LONG).show();
                    return false;
                }

                return true;
            }
        });
        this.setContentView(currentScrollView);
    }

    public boolean isEmailValid(String email) {
        String expression = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean isPhoneValid(String phone) {
        String expression = "^[+0][1-9]\\d{4,}";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Constants.menuAboutText);

        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent aboutIntent = new Intent(LoginActivity.this, AboutActivity.class);
                try {
                    startActivity(aboutIntent);
                } catch (Exception e) {
                    Timber.e(e);
                }
                return false;
            }
        });

//        MenuItem adminItem = menu.add(Constants.menuAdminText);

//        adminItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Intent adminIntent = new Intent(LoginActivity.this,
//                        AdminLoginActivity.class);
//                try {
//                    startActivity(adminIntent);
//                } catch (Exception e) {
//                    Timber.e(e);
//                }
//                return false;
//            }
//        });

        MenuItem preferenceItem = menu.add(Meili.getInstance().getString(R.string.preferences_text));
        preferenceItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(LoginActivity.this, PreferencesActivity.class));
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}
