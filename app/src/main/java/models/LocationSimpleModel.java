package models;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class LocationSimpleModel {
    LocationModel currentLocation;
    ParameterPrefModel currentParams;
    int id;

    public LocationSimpleModel(LocationModel currentLocation, ParameterPrefModel currentParams) {
        this.currentLocation = currentLocation;
        this.currentParams = currentParams;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
