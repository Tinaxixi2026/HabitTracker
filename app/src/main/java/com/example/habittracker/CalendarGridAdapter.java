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

public class CalendarGridAdapter extends BaseAdapter {

    private final Context context;
    private final List<LocalDate> cells;
    private final Set<String> checkins;
    private final LocalDate today;

    public CalendarGridAdapter(Context context, List<LocalDate> cells, Set<String> checkins, LocalDate today) {
        this.context = context;
        this.cells = cells;
        this.checkins = checkins;
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
        TextView tv;
        if (convertView == null) {
            tv = (TextView) LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);
        } else {
            tv = (TextView) convertView;
        }

        LocalDate d = cells.get(position);
        if (d == null) {
            tv.setText("");
            tv.setBackground(null);
            return tv;
        }

        tv.setText(String.valueOf(d.getDayOfMonth()));
        String key = CalendarActivity.fmt(d);

        if (checkins.contains(key)) {
            // 已打卡：绿色圆底 + 白字
            tv.setBackgroundResource(R.drawable.bg_day_checked);
            tv.setTextColor(Color.WHITE);
        } else if (d.isAfter(today)) {
            // 未来日期：灰色
            tv.setBackground(null);
            tv.setTextColor(0xFFB9B9B9);
        } else if (d.equals(today)) {
            // 今天：蓝色描边圆
            tv.setBackgroundResource(R.drawable.bg_day_today);
            tv.setTextColor(0xFF1B1B1B);
        } else {
            tv.setBackground(null);
            tv.setTextColor(0xFF1B1B1B);
        }
        return tv;
    }
}
