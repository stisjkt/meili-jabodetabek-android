package activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
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

import constants.Constants;
import tasks.RegisterTask;
import timber.log.Timber;
import utilities.GetInfo;

public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final GetInfo gI = new GetInfo(this);

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
        emailText.setText(Constants.regularEmailText);
        emailText.setPadding(0, 0, 0, 20);
        emailText.setTextSize(15);
        emailText.setGravity(Gravity.START);

        TextView passwordText = new TextView(this);
        passwordText.setSingleLine(true);
        passwordText.setText(Constants.regularPasswordText);
        passwordText.setPadding(0, 0, 0, 20);
        passwordText.setTextSize(15);
        passwordText.setGravity(Gravity.START);

        TextView confirmPasswordText = new TextView(this);
        confirmPasswordText.setSingleLine(true);
        confirmPasswordText.setText(Constants.confirmPassword);
        confirmPasswordText.setPadding(0, 0, 0, 20);
        confirmPasswordText.setTextSize(15);
        confirmPasswordText.setGravity(Gravity.START);

        TextView phoneNumberText = new TextView(this);
        phoneNumberText.setSingleLine(true);
        phoneNumberText.setText(Constants.phoneNumber);
        phoneNumberText.setPadding(0, 0, 0, 20);
        phoneNumberText.setTextSize(15);
        phoneNumberText.setGravity(Gravity.START);

        final TextView regNoText = new TextView(this);
        regNoText.setSingleLine(true);
        regNoText.setText(Constants.regNumber);
        regNoText.setPadding(0, 0, 0, 20);
        regNoText.setTextSize(15);
        regNoText.setGravity(Gravity.START);

        final EditText emailEdit = new EditText(this);
        emailEdit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        final EditText passwordEdit = new EditText(this);
        passwordEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod
                .getInstance());

        final EditText confirmPasswordEdit = new EditText(this);
        confirmPasswordEdit
                .setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordEdit
                .setTransformationMethod(PasswordTransformationMethod
                        .getInstance());

        final EditText phoneNumberEdit = new EditText(this);
        phoneNumberEdit.setInputType(InputType.TYPE_CLASS_PHONE);

        final EditText regNoEdit = new EditText(this);
        regNoEdit.setInputType(InputType.TYPE_CLASS_NUMBER);

        Button registerAndLoginButton = new Button(this);
        registerAndLoginButton.setText(Constants.registerAndLoginButton);
        registerAndLoginButton.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView contentText = new TextView(this);
        contentText.setText(Constants.contentText);
        contentText.setTextSize(15);
        contentText.setGravity(Gravity.START);
        contentText.setPadding(0, 0, 0, 50);
        contentText.setVisibility(View.GONE);

        currentLineraLayout.addView(titleText, layoutParams);
        currentLineraLayout.addView(regNoText);
        currentLineraLayout.addView(regNoEdit);
        currentLineraLayout.addView(emailText);
        currentLineraLayout.addView(emailEdit);
        currentLineraLayout.addView(phoneNumberText);
        currentLineraLayout.addView(phoneNumberEdit);
        currentLineraLayout.addView(passwordText);
        currentLineraLayout.addView(passwordEdit);
        currentLineraLayout.addView(confirmPasswordText);
        currentLineraLayout.addView(confirmPasswordEdit);
        currentLineraLayout.addView(registerAndLoginButton, layoutParams);
        currentLineraLayout.addView(contentText);

        registerAndLoginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (gI.isOnline()) {
                    if (completeCredentialsPassed())
                        registrationComplete();
                } else
                    Toast.makeText(
                            RegisterActivity.this,
                            Constants.internetConnectionWarning,
                            Toast.LENGTH_LONG).show();

            }

            private boolean registrationComplete() {
                new RegisterTask(RegisterActivity.this, emailEdit.getText()
                        .toString(), passwordEdit.getText().toString(),
                        RegisterActivity.this, Build.MODEL + "",
                        Build.VERSION.SDK_INT + "", phoneNumberEdit.getText().toString(),
                        regNoEdit.getText().toString()).execute();
                return true;
            }

            private boolean completeCredentialsPassed() {
                String regNo = regNoEdit.getText().toString();
                if (!isRegNoValid(regNo)) {
                    Toast.makeText(RegisterActivity.this,
                            Constants.regNoWarning,
                            Toast.LENGTH_LONG).show();
                    return false;
                }

                String email = emailEdit.getText().toString();
                String phone = phoneNumberEdit.getText().toString();
                if (email.isEmpty() && phone.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            Constants.noUsernameWarning,
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                if (!email.isEmpty()) {
                    if (!isEmailValid(email)) {
                        Toast.makeText(RegisterActivity.this,
                                Constants.emailWarning,
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                if (!phone.isEmpty()) {
                    if (!isPhoneValid(phone)) {
                        Toast.makeText(RegisterActivity.this,
                                Constants.phoneNumberWarning,
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                String password = passwordEdit.getText().toString();
                if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            Constants.passwordEmpty,
                            Toast.LENGTH_LONG).show();
                    return false;
                }

                String confirmPassword = confirmPasswordEdit.getText().toString();
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this,
                            Constants.passwordError,
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });
        this.setContentView(currentScrollView);
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email email
     * @return boolean true for valid false for invalid
     */
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

    public boolean isRegNoValid(String regNo) {
        String expression = "\\d{7}";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(regNo);
        return matcher.matches();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Constants.menuAboutText);

        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent aboutIntent = new Intent(RegisterActivity.this,
                        AboutActivity.class);
                try {
                    startActivity(aboutIntent);
                } catch (Exception e) {
                    Timber.e(e);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}
