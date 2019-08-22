package models;

import android.location.Location;
import android.util.Log;

import timber.log.Timber;

public class EmbeddedLocationModel {
    LocationModel currentLocation;
    ProcessedAccelerometerModel currentAcc;
    ParameterPrefModel currentParams;
    int id;

    public EmbeddedLocationModel(Location l, int userID, String provider) {
        this.currentLocation = new LocationModel(l, userID, provider);
    }

    public EmbeddedLocationModel(Location l, ProcessedAccelerometerModel a, int userID, String provider) {
        Timber.d("User ID : %s", userID);
        this.currentLocation = new LocationModel(l, userID, provider);
        this.currentAcc = a;
    }

    public EmbeddedLocationModel(LocationModel lV, ProcessedAccelerometerModel a, ParameterPrefModel pM) {
        this.currentLocation = lV;
        this.currentAcc = a;
        this.currentParams = pM;
    }

    public EmbeddedLocationModel(Location location, int userId, boolean b, String provider) {
        // TODO Auto-generated constructor stub
        this.currentLocation = new LocationModel(location, userId, provider);
        this.currentAcc = ProcessedAccelerometerModel.getBlankAcc();
    }

    public LocationModel getCurrentLocation() {
        return currentLocation;
    }

    public ProcessedAccelerometerModel getCurrentAcc() {
        return currentAcc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
