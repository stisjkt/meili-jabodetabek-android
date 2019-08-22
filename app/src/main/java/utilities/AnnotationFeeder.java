package utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import constants.Constants;
import constants.Variables;
import daos.AdministrativeDao;
import daos.AnnotationDao;
import daos.LocationAccelerationDao;
import models.AnnotationModel;

public class AnnotationFeeder {

    @SuppressLint("StaticFieldLeak")
    private static AnnotationFeeder instance = null;
	private Context mContext;
	private int userID;
	private String lastAnnotationName = "";
	private String currentAnnotationName = "";
	private long lastAnnotationTime;

	private AnnotationFeeder(Context ctx) {

		this.mContext = ctx;
        AdministrativeDao adminDb = new AdministrativeDao(Constants.databaseName,
				Constants.adminTable, mContext);
		this.userID = adminDb.getUserId();
        adminDb.closeDb();

	}

	public static AnnotationFeeder getInstance(Context ctx) {
		if (instance == null)
			instance = new AnnotationFeeder(ctx);
		return instance;
	}

	public void feedMe(String annotationName) {
        AnnotationDao annotationDao = new AnnotationDao(Constants.databaseName,
                Constants.annotationTable, mContext);
        LocationAccelerationDao locationDatabase = new LocationAccelerationDao(
                Constants.databaseName, Constants.locationTable, mContext);

		if (this.userID != 0) {
			AnnotationModel previousAnnotation = annotationDao
					.getLastInsertedAnnotation();

            long currentAnnotationTime;
			if (Variables.periodAnnotations) {
				// PERIOD BASED ANNOTATIONS
				if (previousAnnotation != null) {
					annotationDao
							.insertAnnotationIntoDatabase(new AnnotationModel(
									userID, previousAnnotation
											.getAnnotationStopTime(), System
											.currentTimeMillis(),
									lastAnnotationName));
					lastAnnotationName = annotationName;
				} else {
					if (lastAnnotationName.equals("")) {
						lastAnnotationName = annotationName;
						lastAnnotationTime = System.currentTimeMillis();
					} else if (currentAnnotationName.equals("")) {
						currentAnnotationName = annotationName;
						currentAnnotationTime = System.currentTimeMillis();
						annotationDao
								.insertAnnotationIntoDatabase(new AnnotationModel(
										userID, lastAnnotationTime,
										currentAnnotationTime,
										lastAnnotationName));
						lastAnnotationName = currentAnnotationName;
					}
				}
			}

			// POINT BASED ANNOTATIONS
			else {
				if (previousAnnotation != null) {
					currentAnnotationTime = locationDatabase.getLastLocation().time_;

					if (currentAnnotationTime != lastAnnotationTime) {
						annotationDao
								.insertAnnotationIntoDatabase(new AnnotationModel(
										userID, previousAnnotation
												.getAnnotationStopTime(),
										System.currentTimeMillis(),
										lastAnnotationName));
						lastAnnotationName = annotationName;
						lastAnnotationTime = currentAnnotationTime;
					}
				} else {
					if (lastAnnotationName.equals("")) {
						lastAnnotationName = annotationName;
						lastAnnotationTime = locationDatabase.getLastLocation().time_;
					} else if (currentAnnotationName.equals("")) {
						currentAnnotationTime = locationDatabase
								.getLastLocation().time_;
						if (currentAnnotationTime != lastAnnotationTime) {
							currentAnnotationName = annotationName;
							annotationDao
									.insertAnnotationIntoDatabase(new AnnotationModel(
											userID, lastAnnotationTime,
											currentAnnotationTime,
											lastAnnotationName));
							lastAnnotationName = currentAnnotationName;
							lastAnnotationTime = currentAnnotationTime;
						}
					}
				}
			}
		} else {
			Toast.makeText(mContext,
                    "Please login first new userid = " + this.userID,
					Toast.LENGTH_LONG).show();
		}
        annotationDao.closeDb();
        locationDatabase.closeDb();
	}

}
