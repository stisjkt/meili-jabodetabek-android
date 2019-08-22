package adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import models.LogModel;
import id.ac.stis.meili.R;
import utilities.LoggingUtils;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class LogAdapter extends BaseAdapter {

    private Context context;
    private List<LogModel> list;

    public LogAdapter(Context context, List<LogModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                view = inflater.inflate(R.layout.item_log, viewGroup, false);

                holder = new ViewHolder();
                holder.date = (TextView) view.findViewById(R.id.text_date);
                holder.tag = (TextView) view.findViewById(R.id.text_tag);
                holder.level = (TextView) view.findViewById(R.id.text_level);
                holder.message = (TextView) view.findViewById(R.id.text_message);

                view.setTag(holder);
            }
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (holder != null) {
            holder.date.setText(((LogModel) getItem(i)).getDate());
            holder.tag.setText(((LogModel) getItem(i)).getTag());
            holder.level.setText(((LogModel) getItem(i)).getLevel());
            holder.message.setText(((LogModel) getItem(i)).getMessage());

            switch (LoggingUtils.parseLevel(((LogModel) getItem(i)).getLevel())) {
                case Log.WARN:
                    holder.date.setTextColor(context.getResources().getColor(R.color.yellow));
                    holder.tag.setTextColor(context.getResources().getColor(R.color.yellow));
                    holder.level.setTextColor(context.getResources().getColor(R.color.yellow));
                    holder.message.setTextColor(context.getResources().getColor(R.color.yellow));
                    break;
                case Log.ERROR:
                    holder.date.setTextColor(context.getResources().getColor(R.color.red));
                    holder.tag.setTextColor(context.getResources().getColor(R.color.red));
                    holder.level.setTextColor(context.getResources().getColor(R.color.red));
                    holder.message.setTextColor(context.getResources().getColor(R.color.red));
                    break;
                case Log.INFO:
                    holder.date.setTextColor(context.getResources().getColor(android.R.color.black));
                    holder.tag.setTextColor(context.getResources().getColor(android.R.color.black));
                    holder.level.setTextColor(context.getResources().getColor(android.R.color.black));
                    holder.message.setTextColor(context.getResources().getColor(android.R.color.black));
                    break;
                case Log.DEBUG:
                    holder.date.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                    holder.tag.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                    holder.level.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                    holder.message.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                    break;
            }
        }

        return view;
    }

    private class ViewHolder {
        TextView date;
        TextView tag;
        TextView level;
        TextView message;
    }
}
