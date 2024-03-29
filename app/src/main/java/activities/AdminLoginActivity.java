package activities;

import android.app.Activity;
import android.content.Intent;
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

import constants.Constants;
import constants.Variables;
import timber.log.Timber;

public class AdminLoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        titleText.setText(Constants.adminTitleText);
        titleText.setPadding(0, 0, 0, 50);
        titleText.setTextSize(20);
        titleText.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView emailText = new TextView(this);
        emailText.setSingleLine(true);
        emailText.setText(Constants.sudoEmailText);
        emailText.setPadding(0, 0, 0, 20);
        emailText.setTextSize(15);
        emailText.setGravity(Gravity.LEFT);

        TextView passwordText = new TextView(this);
        passwordText.setSingleLine(true);
        passwordText.setText(Constants.sudoPasswordText);
        passwordText.setPadding(0, 0, 0, 20);
        passwordText.setTextSize(15);
        passwordText.setGravity(Gravity.LEFT);

        final EditText emailEdit = new EditText(this);
        emailEdit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        final EditText passwordEdit = new EditText(this);
        passwordEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod
                .getInstance());

        Button adminLogin = new Button(this);
        adminLogin.setText(Constants.loginText);
        adminLogin.setGravity(Gravity.CENTER_HORIZONTAL);

        // layoutParams.setMargins(100, 500, 100, 200);

        currentLineraLayout.addView(titleText, layoutParams);
        currentLineraLayout.addView(emailText);
        currentLineraLayout.addView(emailEdit);
        currentLineraLayout.addView(passwordText);
        currentLineraLayout.addView(passwordEdit);
        currentLineraLayout.addView(adminLogin, layoutParams);

        adminLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (completeCredentialsPassed()) {
                    try {
                        startActivity(new Intent(AdminLoginActivity.this,
                                AdminActivity.class));
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                    finish();
                }

            }

            private boolean completeCredentialsPassed() {
                if (emailEdit.getText().toString().equals(Variables.admin))
                    if (passwordEdit.getText().toString().equals(Variables.password))
                        return true;
                Toast.makeText(getApplicationContext(),
                        Constants.sudoCredentialsWrongMessage, Toast.LENGTH_LONG)
                        .show();
                return false;
            }
        });
        this.setContentView(currentScrollView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Constants.menuAboutText);

        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent aboutIntent = new Intent(AdminLoginActivity.this,
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
