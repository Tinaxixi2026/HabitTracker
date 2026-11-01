package com.example.habittracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class ScheduleListAdapter extends BaseAdapter {

    public interface Listener {
        void onDelete(Schedule schedule);
    }

    private final Context context;
    private final List<Schedule> schedules;
    private final Listener listener;

    public ScheduleListAdapter(Context context, List<Schedule> schedules, Listener listener) {
        this.context = context;
        this.schedules = schedules;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return schedules.size();
    }

    @Override
    public Object getItem(int position) {
        return schedules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return schedules.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
            vh = new ViewHolder();
            vh.title = convertView.findViewById(R.id.schedule_title);
            vh.note = convertView.findViewById(R.id.schedule_note);
            vh.delete = convertView.findViewById(R.id.btn_delete_schedule);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        final Schedule s = schedules.get(position);
        vh.title.setText(s.title);
        if (s.note == null || s.note.trim().isEmpty()) {
            vh.note.setVisibility(View.GONE);
        } else {
            vh.note.setVisibility(View.VISIBLE);
            vh.note.setText(s.note);
        }
        vh.delete.setOnClickListener(v -> listener.onDelete(s));
        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView note;
        ImageButton delete;
    }
}
