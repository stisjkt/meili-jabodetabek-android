package models;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class AccelerometerSimpleModel implements Serializable {

    private int id;
    private int user_id;
    private float current_x, min_x, max_x, mean_x, current_y, min_y, max_y, mean_y,
            current_z, min_z, max_z, mean_z;
    private long time_;

    public AccelerometerSimpleModel(int user_id, float current_x, float min_x, float max_x,
                                    float mean_x, float current_y, float min_y, float max_y,
                                    float mean_y, float current_z, float min_z, float max_z,
                                    float mean_z, long time_) {
        this.user_id = user_id;
        this.current_x = current_x;
        this.min_x = min_x;
        this.max_x = max_x;
        this.mean_x = mean_x;
        this.current_y = current_y;
        this.min_y = min_y;
        this.max_y = max_y;
        this.mean_y = mean_y;
        this.current_z = current_z;
        this.min_z = min_z;
        this.max_z = max_z;
        this.mean_z = mean_z;
        this.time_ = time_;
    }

    public static LinkedHashMap<String, String> getAllElements() {
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();

        for (Field f : AccelerometerSimpleModel.class.getDeclaredFields()) {
            if (!(f.getName().equalsIgnoreCase("serialVersionUID") || f.getName().equalsIgnoreCase("id"))) {
                hashMap.put(f.getName(), utilities.GetInfo.convertTypeJavaToSql(f.getType().getName()));
            }
        }
        return hashMap;
    }

    public int getId() {
        return id;
    }

    public AccelerometerSimpleModel setId(int id) {
        this.id = id;
        return this;
    }

    public int getUser_id() {
        return user_id;
    }

    public float getCurrent_x() {
        return current_x;
    }

    public float getMin_x() {
        return min_x;
    }

    public float getMax_x() {
        return max_x;
    }

    public float getMean_x() {
        return mean_x;
    }

    public float getCurrent_y() {
        return current_y;
    }

    public float getMin_y() {
        return min_y;
    }

    public float getMax_y() {
        return max_y;
    }

    public float getMean_y() {
        return mean_y;
    }

    public float getCurrent_z() {
        return current_z;
    }

    public float getMin_z() {
        return min_z;
    }

    public float getMax_z() {
        return max_z;
    }

    public float getMean_z() {
        return mean_z;
    }

    public long getTime_() {
        return time_;
    }
}
