package activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import constants.Constants;
import daos.LocationAccelerationDao;
import models.EmbeddedLocationModel;
import services.CollectingService;
import tasks.UploadTask;
import timber.log.Timber;
import utilities.GetInfo;
import utilities.ServiceUtils;

public class ServiceHandlingActivity extends Activity {
    static LinkedList<EmbeddedLocationModel> trajectorySet = new LinkedList<>();
    private static int hasTurned = 0;
    private static int noTurn = 1;
    private static int inconclusiveTurn = 2;

    /* ========================================= */
    private static int possibleFutureTurn = 3;
    private static int bearingTolerance = 30;
    boolean isServiceOn = false;
    Button serviceHandlingButton;
    AlertDialog.Builder alert = null;

    /* ======================================== */
    /*
    private static int checkForTurn() {
        boolean sameBearing = true;
        LinkedList<Integer> changedMark = new LinkedList<>();
        int i = 0;
//        EmbeddedLocationModel prevLocation = new EmbeddedLocationModel(new LocationModel(new Location(""), 0, "s"), ProcessedAccelerometerModel.getBlankAcc());
        EmbeddedLocationModel prevLocation;
        double theta[] = new double[5];
        //double speed[] = new double[5];
        prevLocation = trajectorySet.getFirst();
        for (EmbeddedLocationModel loc : trajectorySet) {
            if (i != 0) {
                if (loc.getCurrentLocation().accuracy_ <= 100) {
                    Location prevLoc = new Location("");
                    prevLoc.setLatitude(prevLocation.getCurrentLocation().lat_);
                    prevLoc.setLongitude(prevLocation.getCurrentLocation().lon_);
                    prevLoc.setTime(prevLocation.getCurrentLocation().time_);

                    Location loc2 = new Location("");
                    loc2.setLatitude(loc.getCurrentLocation().lat_);
                    loc2.setLongitude(loc.getCurrentLocation().lon_);
                    loc2.setTime(loc.getCurrentLocation().time_);

                    double distance1 = prevLoc.distanceTo(loc2) / 1000;
                    double time1 = (double) loc2.getTime() / (double) (1000 * 60 * 60) - (double) prevLoc.getTime() / (double) (1000 * 60 * 60);

                    //speed[i - 1] = distance1 / time1;

                    theta[i - 1] = prevLoc.bearingTo(loc2);
                    prevLocation = loc;
                    if (theta[i - 1] < 0)
                        theta[i - 1] = theta[i - 1] + 180;
                } else i--;

            }
            i++;
        }


        for (int j = 0; j < i - 2; j++)
            if (Math.abs(theta[j] - theta[j + 1]) >= bearingTolerance) {
                sameBearing = false;
                changedMark.add(j);
            }
        if (sameBearing)
            return noTurn;
        // single turn in the middle of the trajectory
        int numberOfDistinctDirections = 0;
        Arrays.sort(theta);
        for (int j = 0; j < 4; j++)
            if ((Math.abs(theta[j] - theta[j + 1]) >= bearingTolerance) // for
                    // regular
                    // values
                    && (Math.abs(theta[j] + theta[j + 1] - 180)) >= bearingTolerance) //  for
                // values
                // close
                // to
                // the
                // circle's
                // changing
                // point
                numberOfDistinctDirections++;
        // regular turn detected
        if ((changedMark.size() == 1) && (changedMark.getFirst() != 2)) {
            // clean the locations up to the turn
            int positionTurn = changedMark.getFirst();
            for (int j = 0; j < positionTurn; j++) {
                //feedToAlgorithm(trajectorySet.get(j));
                //trajectorySet.remove(j);
            }
            return hasTurned;
        }
        // single turn at the end of trajectory - need more
        // historical data
        if ((changedMark.size() == 1) && (changedMark.getFirst() == 2))
            return possibleFutureTurn;
        if (changedMark.size() == 2) {
            // noise within the trajectory
            int position = changedMark.getLast();
            Timber.d("NOISE DETECTED at %s", trajectorySet.get(position).getCurrentLocation().time_);
            trajectorySet.remove(position);
            return noTurn;
        }
        // no conclusion could be derived from the existing dataset
        return inconclusiveTurn;
        //return noTurn;
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        GetInfo gI = new GetInfo(this);
        isServiceOn = gI.isServiceOn();

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

        titleText.setText(Constants.titleControlCenter);
        titleText.setPadding(0, 0, 0, 50);
        titleText.setTextSize(20);
        titleText.setGravity(Gravity.CENTER_HORIZONTAL);

        serviceHandlingButton = new Button(this);
        serviceHandlingButton.setGravity(Gravity.CENTER_HORIZONTAL);
        changeText(isServiceOn);

        currentLineraLayout.addView(titleText, layoutParams);
        currentLineraLayout.addView(serviceHandlingButton, layoutParams);

		/*if (Variables.areAnnotationsAllowed) {

			AnnotationDao annotationDatabase = new AnnotationDao(
					Constants.databaseName, Constants.annotationTable, this);

			AnnotationModel previousAnnotation = annotationDatabase
					.getLastInsertedAnnotation();

			TextView annotationText = new TextView(this);
			annotationText.setSingleLine(true);

			annotationText.setText("Annotating menu");
			annotationText.setPadding(0, 50, 0, 25);
			annotationText.setTextSize(18);
			annotationText.setGravity(Gravity.LEFT);

			RadioGroup annotationRadioGroup = new RadioGroup(this);
			annotationRadioGroup.setOrientation(RadioGroup.VERTICAL);

			annotations = new String[Variables.annotationsStrings.split("!__!").length];
			annotations = Variables.annotationsStrings.split("!__!");

			final RadioButton[] annotationItem = new RadioButton[annotations.length];

			for (int i = 0; i < annotations.length; i++) {
				annotationItem[i] = new RadioButton(this);
				annotationRadioGroup.addView(annotationItem[i]);
				annotationItem[i].setText(annotations[i]);
			}

			if (previousAnnotation != null) {
				for (int i = 0; i < annotations.length; i++)
					if (annotations[i].equalsIgnoreCase(previousAnnotation
							.getAnnotationValues())) {
						annotationItem[i].setChecked(true);

					}
			}

			annotationRadioGroup
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							// TODO Auto-generated method stub

							try {
								AnnotationFeeder.getInstance(
										this).feedMe(
										annotationItem[(checkedId - 1)
												% annotations.length].getText()
												.toString());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								Timber.e(e);
							}

						}
					});

			currentLineraLayout.addView(annotationText);

			currentLineraLayout.addView(annotationRadioGroup);
		}*/

	/*	TextView contentText = new TextView(this);
		contentText
				.setText("This mobile application seamlessly collects trips (movement trace, transitions and stays/destinations) of its user and is part of the KTH Mobility Collection Project and the semi-automatic KTH Mobility Collector, which is composed of a this mobile application and a web application that allows the periodic convenient travel-diary-annotation of the collected trips. The collected travel diaries give valuable insight into the travel needs of individuals and the population, which are essential in transportation planning. The use of KTH Mobility Collector is voluntary, the mobile and web applications give user-controls for suspending collection or deleting a collected trip and the collected data is only analyzed in an anonymous form for research purposes.");
		contentText.setTextSize(15);
		contentText.setGravity(Gravity.LEFT);
		contentText.setPadding(0, 0, 0, 50);
		currentLineraLayout.addView(contentText);
*/
        serviceHandlingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Intent collectionServiceIntent = new Intent(
                        CollectingService.class.getName());

                if (!isServiceOn) {
                    try {
                        if (passGPSFilter()) {
                            isServiceOn = true;
                            changeText(true);
                            //startService(collectionServiceIntent);
                            ServiceUtils.startCollectService(getApplicationContext(),collectionServiceIntent);
                        } else
                            buildGpsAlarm();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Timber.e(e);
                    }
                } else {
                    buildAlert(collectionServiceIntent);
                    /*
                     * isServiceOn = false;
                     * stopService(collectionServiceIntent);
                     * changeText(isServiceOn);
                     */
                }

            }

            private boolean passGPSFilter() {
                // TODO Auto-generated method stub
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (locationManager != null) {
                    return locationManager
                            .isProviderEnabled(LocationManager.GPS_PROVIDER);
                }
                return false;
            }

            private void buildGpsAlarm() {
                // TODO Auto-generated method stub

                final AlertDialog.Builder alert2 = new AlertDialog.Builder(
                        ServiceHandlingActivity.this);
                alert2.setMessage(Constants.enableGPSBody);
                alert2.setTitle(Constants.enableGPSTitle);

                alert2.setPositiveButton(Constants.yesText,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });

                alert2.setNegativeButton(Constants.noText,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int which) {

                            }
                        });
                AlertDialog dialog2 = alert2.create();
                dialog2.show();
            }
        });
        this.setContentView(currentScrollView);

    }

    private void buildAlert(final Intent collectionServiceIntent_) {
        // TODO Auto-generated method stub
        alert = new AlertDialog.Builder(this);
        alert.setMessage(Constants.serviceDisableBody);
        alert.setTitle(Constants.serviceDisableTitle);

        alert.setPositiveButton(Constants.yesText, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                isServiceOn = false;
                stopService(collectionServiceIntent_);
                changeText(isServiceOn);
            }
        });

        alert.setNegativeButton(Constants.noText, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void changeText(boolean isServiceOn2) {
        if (isServiceOn2) {
            serviceHandlingButton.setText(Constants.stopCollectionText);
            serviceHandlingButton.setTextColor(Color.RED);
        } else {
            serviceHandlingButton.setText(Constants.startCollectionText);
            serviceHandlingButton.setTextColor(Color.BLUE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem aboutItem = menu.add(Constants.menuAboutText);

        aboutItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent aboutIntent = new Intent(ServiceHandlingActivity.this,
                        AboutActivity.class);
                try {
                    startActivity(aboutIntent);
                } catch (Exception e) {
                    Timber.e(e);
                }
                return false;
            }
        });

        MenuItem adminItem = menu.add(Constants.menuAdminText);

        adminItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent adminIntent = new Intent(ServiceHandlingActivity.this,
                        AdminLoginActivity.class);
                try {
                    startActivity(adminIntent);
                } catch (Exception e) {
                    Timber.e(e);
                }
                return false;
            }
        });

        MenuItem dbStatus = menu.add(Constants.menuStatusText);

        dbStatus.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @SuppressWarnings("static-access")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LocationAccelerationDao locationDb = new LocationAccelerationDao(
                        Constants.databaseName, Constants.locationTable,
                        ServiceHandlingActivity.this);
                Toast.makeText(
                        ServiceHandlingActivity.this,
                        Constants.menuStatusText + ": "
                                + locationDb.getAllLocationsFromDatabase()
                                .size(), Toast.LENGTH_LONG).show();
                locationDb.closeDb();
                return false;
            }
        });

        MenuItem manualDb = menu.add(Constants.menuUploadText);

        manualDb.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                UploadTask uploadTask = new UploadTask(ServiceHandlingActivity.this);
                uploadTask.execute();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
