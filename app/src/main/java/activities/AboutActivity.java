package activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import constants.Constants;

public class AboutActivity extends Activity {

    Button closeAboutSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
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
        titleText.setSingleLine(false);

        titleText.setText(Constants.titleText);
        titleText.setPadding(0, 0, 0, 50);
        titleText.setTextSize(20);
        titleText.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView titleText2 = new TextView(this);
        TextView titleText3 = new TextView(this);
        titleText3.setText(Constants.subTitleText);
        titleText3.setPadding(0, 0, 0, 50);
        titleText3.setTextSize(16);
        titleText3.setGravity(Gravity.CENTER_HORIZONTAL);

        titleText2.setText(Constants.versionText);
        titleText2.setPadding(0, 0, 0, 50);
        titleText2.setTextSize(17);
        titleText2.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView contentText = new TextView(this);
        contentText.setText(Constants.contentText);
        contentText.setTextSize(15);
        contentText.setGravity(Gravity.LEFT);
        contentText.setPadding(0, 0, 0, 50);

        closeAboutSection = new Button(this);
        closeAboutSection.setGravity(Gravity.CENTER_HORIZONTAL);
        closeAboutSection.setText(Constants.closeButtonText);

        // layoutParams.setMargins(100, 500, 100, 200);

        // Please add your own logo if you desire
        /*
		ImageButton imageButton = new ImageButton(this);
		imageButton.setImageResource(R.drawable.small_logo);
		imageButton.setBackgroundColor(Color.TRANSPARENT);	
		
		currentLineraLayout.addView(imageButton);
		*/
        currentLineraLayout.addView(titleText, layoutParams);
        currentLineraLayout.addView(titleText2);
        currentLineraLayout.addView(titleText3);
        currentLineraLayout.addView(contentText);
        currentLineraLayout.addView(closeAboutSection, layoutParams);

        closeAboutSection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                AboutActivity.this.finish();
            }
        });

        this.setContentView(currentScrollView);
    }
}
