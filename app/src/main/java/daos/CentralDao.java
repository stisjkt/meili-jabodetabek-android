package daos;

import android.content.Context;

import com.google.gson.Gson;

import java.util.LinkedList;

import constants.Constants;
import constants.Variables;
import models.AccelerometerSimpleModel;
import models.EmbeddedLocationModel;
import models.LocationSimpleModel;
import timber.log.Timber;

public class CentralDao {
    private Context mContext;
    private int locationLastId;
    private int accelerometerLastId;

    public CentralDao(Context ctx) {
        mContext = ctx;
    }

    public Context getContext() {
        return mContext;
    }

    public String getUploadStatement(int chunk) {

        String uploadString = "method=upload";

        try {
            if (Variables.isAccelerometerEmbedded) {
                LocationAccelerationDao locationDb = new LocationAccelerationDao(
                        Constants.databaseName, Constants.locationTable,
                        mContext);
                LinkedList<EmbeddedLocationModel> locationModels = locationDb.getLocationsForUploadFromDatabase(chunk);
                locationDb.closeDb();
                String embeddedLocation = new Gson().toJson(locationModels);

                AccelerometerDao accelerometerDao = new AccelerometerDao(Constants.databaseName,
                        Constants.accelerometerTable, mContext);
                LinkedList<AccelerometerSimpleModel> simpleModels = accelerometerDao.getAccelerometersForUploadFromDatabase(chunk);
                accelerometerDao.closeDb();
                String accelerometers = new Gson().toJson(simpleModels);
                uploadString = uploadString + "&embeddedLocations_="
                        + embeddedLocation + "&accelerometers_=" + accelerometers + "&simpleLocations_=";

//                Timber.d("Upload Count : %s", locationModels.size() + "");

                if (!locationModels.isEmpty()) {
                    this.locationLastId = locationModels.getLast().getId();
                }

                if (!simpleModels.isEmpty()) {
                    this.accelerometerLastId = simpleModels.getLast().getId();
                }

            } else {
                LocationSimpleDao locationDb = new LocationSimpleDao(
                        Constants.databaseName, Constants.locationTable,
                        mContext);
                LinkedList<LocationSimpleModel> locationModels = locationDb.getLocationsForUploadFromDatabase(chunk);
                locationDb.closeDb();
                String locationsSimple = new Gson().toJson(locationModels);

                AccelerometerDao accelerometerDao = new AccelerometerDao(Constants.databaseName,
                        Constants.accelerometerTable, mContext);
                LinkedList<AccelerometerSimpleModel> simpleModels = accelerometerDao.getAccelerometersForUploadFromDatabase(chunk);
                accelerometerDao.closeDb();
                String accelerometers = new Gson().toJson(simpleModels);

                uploadString = uploadString + "&embeddedLocations_=[]" + "&accelerometers_=" +
                        accelerometers + "&simpleLocations_=" + locationsSimple;

//                Timber.d("Upload Count : %s", locationModels.size() + "");

                if (!locationModels.isEmpty()) {
                    this.locationLastId = locationModels.getLast().getId();
                }

                if (!simpleModels.isEmpty()) {
                    this.accelerometerLastId = simpleModels.getLast().getId();
                }
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            Timber.e(e1);
            uploadString += "&error=" + e1.toString();
        }

        try {
            if (Variables.areAnnotationsAllowed) {
                AnnotationDao annotationDb = new AnnotationDao(
                        Constants.databaseName, Constants.annotationTable,
                        mContext);
                String annotations = annotationDb.getJSONFromAllForUpload();
                annotationDb.closeDb();
                uploadString = uploadString + "&annotations_=" + annotations;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Timber.e(e);
            uploadString += "&error2=" + e.toString();
        }

        // Timber.d("upload string : %s", uploadString);

        return uploadString;
    }

    public void setUploadToTrue(int locationLastId, int accelerometerLastId) {

        if (Variables.isAccelerometerEmbedded) {
            LocationAccelerationDao locationDb = new LocationAccelerationDao(
                    Constants.databaseName, Constants.locationTable, mContext);
            locationDb.setUploadToTrue(locationLastId);
            locationDb.closeDb();
        } else {
            LocationSimpleDao locationDb = new LocationSimpleDao(
                    Constants.databaseName, Constants.locationTable, mContext);
            locationDb.setUploadToTrue(locationLastId);
            locationDb.closeDb();
        }

        if (Variables.areAnnotationsAllowed) {
            AnnotationDao annotationDb = new AnnotationDao(
                    Constants.databaseName, Constants.annotationTable, mContext);
            annotationDb.setUploadToTrue();
            annotationDb.closeDb();
        }

        AccelerometerDao accelerometerDao = new AccelerometerDao(
                Constants.databaseName, Constants.accelerometerTable, mContext);
        accelerometerDao.setUploadToTrue(accelerometerLastId);
        accelerometerDao.closeDb();
    }

    public int getLocationLastId() {
        return locationLastId;
    }

    public int getAccelerometerLastId() {
        return accelerometerLastId;
    }

}