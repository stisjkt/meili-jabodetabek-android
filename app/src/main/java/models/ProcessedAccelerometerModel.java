package models;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import constants.PreferencesAPI;
import preferences.PreferencesManager;
import timber.log.Timber;

public class ProcessedAccelerometerModel {

    float xMean, yMean, zMean, totalMean;
    float xStdDev, yStdDev, zStdDev, totalStdDev;
    float xMinimum, xMaximum, yMin, yMax, zMin, zMax, totalMin, totalMax;
    int xNumberOfPeaks, yNumberOfPeaks, zNumberOfPeaks, totalNumberOfPeaks;
    int totalNumberOfSteps;
    boolean xIsMoving = false, yIsMoving = false, zIsMoving = false,
            totalIsMoving = false;
    int size;

    public ProcessedAccelerometerModel(LinkedList<AccelerometerModel> accVal) {
        feedAccelerometerValues(accVal);
    }

    public ProcessedAccelerometerModel(float xMean, float yMean, float zMean,
                                       float totalMean, float xStdDev, float yStdDev, float zStdDev,
                                       float totalStdDev, float xMin, float xMax, float yMin, float yMax,
                                       float zMin, float zMax, float totalMin, float totalMax,
                                       int xNumberOfPeaks, int yNumberOfPeaks, int zNumberOfPeaks,
                                       int totalNumberOfPeaks, int totalNumberOfSteps, boolean xIsMoving,
                                       boolean yIsMoving, boolean zIsMoving, boolean totalIsMoving,
                                       int size) {
        super();
        this.xMean = xMean;
        this.yMean = yMean;
        this.zMean = zMean;
        this.totalMean = totalMean;
        this.xStdDev = xStdDev;
        this.yStdDev = yStdDev;
        this.zStdDev = zStdDev;
        this.totalStdDev = totalStdDev;
        this.xMinimum = xMin;
        this.xMaximum = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
        this.totalMin = totalMin;
        this.totalMax = totalMax;
        this.xNumberOfPeaks = xNumberOfPeaks;
        this.yNumberOfPeaks = yNumberOfPeaks;
        this.zNumberOfPeaks = zNumberOfPeaks;
        this.totalNumberOfPeaks = totalNumberOfPeaks;
        this.totalNumberOfSteps = totalNumberOfSteps;
        this.xIsMoving = xIsMoving;
        this.yIsMoving = yIsMoving;
        this.zIsMoving = zIsMoving;
        this.totalIsMoving = totalIsMoving;
        this.size = size;
    }

    //    public static LinkedHashMap<String, String> getAllElements() {
//        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();
//        for (Field f : ProcessedAccelerometerModel.class.getDeclaredFields()) {
//            hashMap.put(f.getName(), utilities.GetInfo.convertTypeJavaToSql(f
//                    .getType().getName()));
//        }
//        return hashMap;
//    }
    public static LinkedHashMap<String, String> getAllElements() {
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();

        for (Field f : ProcessedAccelerometerModel.class.getDeclaredFields()) {
            if (!f.getName().contains("serialVersionUID"))
                hashMap.put(f.getName(), utilities.GetInfo.convertTypeJavaToSql(f.getType().getName()));
        }
        return hashMap;
    }

    public static ProcessedAccelerometerModel getBlankAcc() {
        // TODO Auto-generated method stub
        return new ProcessedAccelerometerModel(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0, 0, 0, 0, false, false, false, false, 0);
    }

    public ProcessedAccelerometerModel feedAccelerometerValues(
            LinkedList<AccelerometerModel> accVal) {
        return triggerComputationsAndGenerateObject(accVal);
    }

    private ProcessedAccelerometerModel triggerComputationsAndGenerateObject(
            LinkedList<AccelerometerModel> accVals) {
        LinkedList<Float> xValues = new LinkedList<Float>();
        LinkedList<Float> yValues = new LinkedList<Float>();
        LinkedList<Float> zValues = new LinkedList<Float>();
        LinkedList<Float> totalValues = new LinkedList<Float>();

        for (AccelerometerModel vals : accVals) {
            xValues.add(vals.values[0]);
            yValues.add(vals.values[1]);
            zValues.add(vals.values[2]);
            totalValues.add((float) Math.pow(vals.values[0] * vals.values[0] + vals.values[1]
                    * vals.values[1] + vals.values[2] * vals.values[2], (double) 0.5));
        }

        setxMean(getMean(xValues));
        setyMean(getMean(yValues));
        setzMean(getMean(zValues));
        setTotalMean(getMean(totalValues));

        setxMin(getMin(xValues));
        setyMin(getMin(yValues));
        setzMin(getMin(zValues));
        setTotalMin(getMin(totalValues));

        setxMax(getMax(xValues));
        setyMax(getMax(yValues));
        setzMax(getMax(zValues));
        setTotalMax(getMax(totalValues));

        setxStdDev(getStdDev(xValues, getxMean()));
        setyStdDev(getStdDev(yValues, getyMean()));
        setzStdDev(getStdDev(zValues, getzMean()));
        setTotalStdDev(getStdDev(totalValues, getTotalMean()));

        setxIsMoving(getMovement(xValues, getxMean()));
        setyIsMoving(getMovement(yValues, getyMean()));
        setzIsMoving(getMovement(zValues, getzMean()));
        setTotalIsMoving(getMovement(totalValues, getTotalMean()));

        setxNumberOfPeaks(getNumberOfPeaks(xValues, getxMean(), getxStdDev()));
        setyNumberOfPeaks(getNumberOfPeaks(yValues, getyMean(), getyStdDev()));
        setzNumberOfPeaks(getNumberOfPeaks(zValues, getzMean(), getzStdDev()));
        setTotalNumberOfPeaks(getNumberOfPeaks(totalValues, getTotalMean(),
                getTotalStdDev()));

        setTotalNumberOfSteps(getNumberOfSteps(totalValues, getTotalMean(),
                getTotalStdDev()));

        setSize(totalValues.size());

        Timber.d("Accelerometer Detected %s %s %s %s %s %s", getTotalMax(),
                getTotalMin(), getTotalMean(),
                getTotalNumberOfPeaks(), getTotalStdDev(), isTotalIsMoving());

        return null;
    }

    public int getTotalNumberOfSteps() {
        return this.totalNumberOfSteps;
    }

    public void setTotalNumberOfSteps(int totalNumberOfSteps) {
        this.totalNumberOfSteps = totalNumberOfSteps;
    }

    /**
     * Gets the relevant number of picks in the current iteration of the
     * recorded accelerometer values
     *
     * @param values  accelerometer values in a linked list
     * @param xMean   the mean of the accelerometer values
     * @param xStdDev the standard devation of the accelerometer values
     * @return the number of relevant peaks
     */
    private int getNumberOfPeaks(LinkedList<Float> values, float xMean,
                                 float xStdDev) {

        float comparisonValue = xMean + (float) (Math.sqrt((double) xStdDev));

        boolean prevValue = false;
        boolean currentValue = false;
        int index = 0;
        int numberOfPeaks = 0;

        for (float f : values) {
            if (index < 1) {
                if (index == 0)
                    prevValue = f > comparisonValue;
            } else {
                currentValue = f > comparisonValue;
                if (currentValue && !prevValue)
                    numberOfPeaks++;
                prevValue = currentValue;
            }
            index++;
        }
        return numberOfPeaks;
    }

    /*
     *
     * IGNORE THIS IN THE MOBILE GHENT CONFERENCE
     */
    private int getNumberOfSteps(LinkedList<Float> xValues, float xMean,
                                 float xStdDev) {
        float comparisonValue = xMean + (float) (Math.sqrt((double) xStdDev));

        boolean prevValue = false;
        boolean currentValue = false;
        int index = 0;
        int numberOfSteps = 0;

        if (comparisonValue > 3) {
            for (float f : xValues) {
                if (index < 1) {
                    if (index == 0)
                        prevValue = f > comparisonValue;
                } else {
                    currentValue = f > comparisonValue;
                    if (currentValue && !prevValue)
                        numberOfSteps++;
                    prevValue = currentValue;
                }
                index++;
            }
            /**
             * Maybe this is implied
             */
            /*
             * for (LinkedList<Float> clst : listOfClusters){ if
             * (getMax(clst)>comparisonValue) numberOfSteps++; }
             */
        }
        return numberOfSteps;
    }

    /**
     * @param values accelerometer values linked list
     * @param mean   previously computed mean of the accelerometer values
     * @return true if the user is moving
     */
    private boolean getMovement(LinkedList<Float> values, float mean) {

        /*
         * Test data: 0.08 for standing still 0.5-1 for having phone inside the
         * pocket while tapping 1-2 hand-derived movement while grabbing the
         * phone >2.5 moving
         */

        float threshold = Float.parseFloat(PreferencesManager.getInstance()
                .get(PreferencesAPI.KEY_ACCELEROMETER_THRESHOLD).toString());
        return mean > threshold;
    }

    private float getMax(LinkedList<Float> values) {
        float xMax;
        xMax = values.getFirst();
        for (float f : values)
            if (xMax < f)
                xMax = f;
        return xMax;
    }

    private float getMin(LinkedList<Float> values) {
        float xMin;
        xMin = values.getFirst();
        for (float f : values)
            if (xMin > f)
                xMin = f;
        return xMin;
    }

    private float getStdDev(LinkedList<Float> values, float mean) {
        float squareSum = 0;
        int numberOfValues = 0;
        for (float f : values) {
            squareSum = (squareSum + (float) Math.pow((f - mean), 2));
            numberOfValues++;
        }
        if (numberOfValues == 0)
            return 0;
        return (float) Math.sqrt(squareSum / numberOfValues);
    }

    private float getMean(LinkedList<Float> values) {

        int nr = 0;
        float sum = 0;

        for (float f : values) {
            sum = sum + f;
            nr++;
        }

        if (nr == 0) {
            return 0;
        }

        return (float) sum / nr;
    }

    public float getxMean() {
        return xMean;
    }

    private void setxMean(float xMean) {
        this.xMean = xMean;
    }

    public float getyMean() {
        return yMean;
    }

    private void setyMean(float yMean) {
        this.yMean = yMean;
    }

    public float getzMean() {
        return zMean;
    }

    private void setzMean(float zMean) {
        this.zMean = zMean;
    }

    public float getTotalMean() {
        return totalMean;
    }

    private void setTotalMean(float totalMean) {
        this.totalMean = totalMean;
    }

    public float getxStdDev() {
        return xStdDev;
    }

    private void setxStdDev(float xStdDev) {
        this.xStdDev = xStdDev;
    }

    public float getyStdDev() {
        return yStdDev;
    }

    private void setyStdDev(float yStdDev) {
        this.yStdDev = yStdDev;
    }

    public float getzStdDev() {
        return zStdDev;
    }

    private void setzStdDev(float zStdDev) {
        this.zStdDev = zStdDev;
    }

    public float getTotalStdDev() {
        return totalStdDev;
    }

    private void setTotalStdDev(float totalStdDev) {
        this.totalStdDev = totalStdDev;
    }

    public int getxNumberOfPeaks() {
        return xNumberOfPeaks;
    }

    private void setxNumberOfPeaks(int xNumberOfPeaks) {
        this.xNumberOfPeaks = xNumberOfPeaks;
    }

    public int getyNumberOfPeaks() {
        return yNumberOfPeaks;
    }

    private void setyNumberOfPeaks(int yNumberOfPeaks) {
        this.yNumberOfPeaks = yNumberOfPeaks;
    }

    public int getzNumberOfPeaks() {
        return zNumberOfPeaks;
    }

    private void setzNumberOfPeaks(int zNumberOfPeaks) {
        this.zNumberOfPeaks = zNumberOfPeaks;
    }

    public int getTotalNumberOfPeaks() {
        return totalNumberOfPeaks;
    }

    private void setTotalNumberOfPeaks(int totalNumberOfPeaks) {
        this.totalNumberOfPeaks = totalNumberOfPeaks;
    }

    public boolean isxIsMoving() {
        return xIsMoving;
    }

    private void setxIsMoving(boolean xIsMoving) {
        this.xIsMoving = xIsMoving;
    }

    public boolean isyIsMoving() {
        return yIsMoving;
    }

    private void setyIsMoving(boolean yIsMoving) {
        this.yIsMoving = yIsMoving;
    }

    public boolean iszIsMoving() {
        return zIsMoving;
    }

    private void setzIsMoving(boolean zIsMoving) {
        this.zIsMoving = zIsMoving;
    }

    public boolean isTotalIsMoving() {
        return totalIsMoving;
    }

    private void setTotalIsMoving(boolean totalIsMoving) {
        this.totalIsMoving = totalIsMoving;
    }

    public int getSize() {
        return size;
    }

    private void setSize(int size) {
        this.size = size;
    }

    public float getxMin() {
        return xMinimum;
    }

    private void setxMin(float xMin) {
        this.xMinimum = xMin;
    }

    public float getxMax() {
        return xMaximum;
    }

    private void setxMax(float xMax) {
        this.xMaximum = xMax;
    }

    public float getyMin() {
        return yMin;
    }

    private void setyMin(float yMin) {
        this.yMin = yMin;
    }

    public float getyMax() {
        return yMax;
    }

    private void setyMax(float yMax) {
        this.yMax = yMax;
    }

    public float getzMin() {
        return zMin;
    }

    private void setzMin(float zMin) {
        this.zMin = zMin;
    }

    public float getzMax() {
        return zMax;
    }

    private void setzMax(float zMax) {
        this.zMax = zMax;
    }

    public float getTotalMin() {
        return totalMin;
    }

    private void setTotalMin(float totalMin) {
        this.totalMin = totalMin;
    }

    public float getTotalMax() {
        return totalMax;
    }

    private void setTotalMax(float totalMax) {
        this.totalMax = totalMax;
    }

    public boolean isTotalIsMoving2() {
        //Timber.d("Total Mean : %s", getTotalMean());
        if (getTotalNumberOfSteps() >= 1) return true;
        return false;
    }
}
