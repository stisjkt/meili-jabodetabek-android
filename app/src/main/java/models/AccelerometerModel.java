package models;

import android.hardware.Sensor;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class AccelerometerModel {

    public float[] values;
    public int accuracy;
    public long timestamp;

    public AccelerometerModel(float[] values, int accuracy, long timestamp) {
        this.values = values;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
    }
}
