package utilities;

import android.location.Location;
import android.util.Log;

import java.util.LinkedList;

import constants.PreferencesAPI;
import preferences.PreferencesManager;
import timber.log.Timber;

/*
 * CLASS USED FOR EQUIDISTANT TRACKING
 */

public class EquidistanceTracking {

    public static double currentFrequency;
    static double Threshold_Frequency = 0.1;
    static LinkedList<Location> locationList;
    static int requiredSize;

    private static EquidistanceTracking instance = null;

    protected EquidistanceTracking() {
        // Exists only to defeat instantiation.
        locationList = new LinkedList<Location>();
        currentFrequency = Double.parseDouble(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_TIME).toString()) / 1000;
        Timber.d("initial frequency : %s", Double.parseDouble(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_TIME).toString()));
    }

    public static EquidistanceTracking getInstance() {
        if (instance == null) {
            instance = new EquidistanceTracking();
        }
        return instance;
    }

    /*
     * adds an extra location to the list
     */
    public static boolean addLocationToList(Location l) {

        /*
         * remove the tail if the size is greater than expected
         */

        if (locationList.size() >= getRequiredSize()) {
            for (int i = 0; i <= (locationList.size() - getRequiredSize()) + 1; i++)
                locationList.remove(i);
        }

        return locationList.add(l);
    }

    private static int getRequiredSize() {
        // TODO Auto-generated method stub
        if (currentFrequency <= 5)
            requiredSize = 12;
        else if (currentFrequency <= 10)
            requiredSize = 6;
        else
            requiredSize = 3;

        return requiredSize;
    }

    private static double getPredictedFrequency() {
        double predictedFrequency;
        double incrementalFrequencyMinimum = 0;
        double incrementalFrequencyCurrent = 0;
        Location fromLocation = null;
        Location toLocation = null;

        for (Location l : locationList) {
            if (fromLocation != null && toLocation != null) {

                /*
                 * Consequent valid sets
                 */
                fromLocation = toLocation;
                toLocation = l;

                if (fromLocation.distanceTo(toLocation) > 0) {
                    incrementalFrequencyCurrent = 50
                            * ((toLocation.getTime() - fromLocation.getTime()) / 1000)
                            / (fromLocation.distanceTo(toLocation));

                    if ((incrementalFrequencyMinimum > incrementalFrequencyCurrent)
                            || (incrementalFrequencyMinimum == 0))
                        incrementalFrequencyMinimum = incrementalFrequencyCurrent;
                }
            } else {
                if (fromLocation == null)
                    fromLocation = l;
                else {

                    /*
                     * First valid set
                     */
                    toLocation = l;
                    incrementalFrequencyMinimum = 50
                            * ((toLocation.getTime() - fromLocation.getTime()) / 1000)
                            / (fromLocation.distanceTo(toLocation));
                }
            }
        }

        if (incrementalFrequencyMinimum >= Double.parseDouble(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_TIME).toString()) / 1000)
            predictedFrequency = Double.parseDouble(PreferencesManager.getInstance().get(PreferencesAPI.KEY_MIN_TIME).toString()) / 1000;
        else if (incrementalFrequencyMinimum <= 5)
            predictedFrequency = 1;
        else
            predictedFrequency = incrementalFrequencyMinimum;

        return predictedFrequency;
    }

    public static double checkForLocationAdjustment() {
        if (locationList.size() == getRequiredSize()) {

            double predictedFrequency = getPredictedFrequency();

            if (predictedFrequency != currentFrequency) {

                if (Math.abs(predictedFrequency - currentFrequency) >= Threshold_Frequency
                        * predictedFrequency) {

                    /*
                     * new frequency is needed to maintain equidistant tracking
                     */

                    Timber.d("Debug changed frequency from %s to %s",
                            currentFrequency, predictedFrequency);

                    setCurrentFrequency(predictedFrequency);
                    return predictedFrequency * 1000;
                }
            }
        }

        return -1;
    }

    private static void setCurrentFrequency(double currentFrequency) {
        Timber.d("Debug tag set frequency : %s", currentFrequency);
        locationList.removeFirst();
        EquidistanceTracking.currentFrequency = currentFrequency;
    }
}
