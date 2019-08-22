package models;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class ParameterPrefModel {
    public int pref_min_acc;
    public int pref_min_dist;
    public int pref_min_time;
    public float pref_acc_threshold;
    public float pref_acc_period;
    public float pref_acc_saving;

    public ParameterPrefModel(int pref_min_acc, int pref_min_dist, int pref_min_time, float pref_acc_threshold, float pref_acc_period, float pref_acc_saving) {
        this.pref_min_acc = pref_min_acc;
        this.pref_min_dist = pref_min_dist;
        this.pref_min_time = pref_min_time;
        this.pref_acc_threshold = pref_acc_threshold;
        this.pref_acc_period = pref_acc_period;
        this.pref_acc_saving = pref_acc_saving;
    }

    public static LinkedHashMap<String, String> getAllElements() {
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();

        for (Field f : ParameterPrefModel.class.getDeclaredFields()) {
            if (!f.getName().contains("serialVersionUID")) {
                hashMap.put(f.getName(), utilities.GetInfo.convertTypeJavaToSql(f.getType().getName()));
            }
        }
        return hashMap;
    }
}
