package listeners;

import java.util.List;

import models.LogModel;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public interface OnLogAppendedListener {
    void onLogAppended(List<LogModel> list);
}
