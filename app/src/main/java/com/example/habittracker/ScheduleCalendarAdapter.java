package com.example.habittracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 日程页的月历格子：支持选中日期高亮、今天描边、有日程的日期标点。
 */
public class ScheduleCalendarAdapter extends BaseAdapter {

    private final Context context;
    private final List<LocalDate> cells;
    private final Set<String> scheduleDates;
    private final LocalDate selected;
    private final LocalDate today;

    public ScheduleCalendarAdapter(Context context, List<LocalDate> cells,
                                   Set<String> scheduleDates, LocalDate selected, LocalDate today) {
        this.context = context;
        this.cells = cells;
        this.scheduleDates = scheduleDates;
        this.selected = selected;
        this.today = today;
    }

    @Override
    public int getCount() {
        return cells.size();
    }

    @Override
    public Object getItem(int position) {
        return cells.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_schedule_day, parent, false);
            vh = new ViewHolder();
            vh.day = convertView.findViewById(R.id.day_text);
            vh.dot = convertView.findViewById(R.id.day_dot);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        LocalDate d = cells.get(position);
        if (d == null) {
            vh.day.setText("");
            vh.day.setBackground(null);
            vh.dot.setVisibility(View.INVISIBLE);
            return convertView;
        }

        vh.day.setText(String.valueOf(d.getDayOfMonth()));

        String key = HabitStore.fmt(d);
        vh.dot.setVisibility(scheduleDates.contains(key) ? View.VISIBLE : View.INVISIBLE);

        if (d.equals(selected)) {
            vh.day.setBackgroundResource(R.drawable.bg_day_checked);
            vh.day.setTextColor(Color.WHITE);
        } else if (d.equals(today)) {
            vh.day.setBackgroundResource(R.drawable.bg_day_today);
            vh.day.setTextColor(0xFF1B1B1B);
        } else if (d.isAfter(today)) {
            vh.day.setBackground(null);
            vh.day.setTextColor(0xFFB9B9B9);
        } else {
            vh.day.setBackground(null);
            vh.day.setTextColor(0xFF1B1B1B);
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView day;
        TextView dot;
    }
}
