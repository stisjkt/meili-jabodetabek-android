package models;

import java.io.Serializable;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class LogModel implements Serializable {

    private String date;
    private String tag;
    private String level;
    private String message;

    public LogModel(String date, String tag, String level, String message) {
        this.date = date;
        this.tag = tag;
        this.level = level;
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public String getTag() {
        return tag;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }
}
