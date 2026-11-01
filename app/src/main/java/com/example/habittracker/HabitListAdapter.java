package com.example.habittracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.util.List;

public class HabitListAdapter extends BaseAdapter {

    public interface Listener {
        void onCheckIn(Habit habit);

        void onDelete(Habit habit);

        void onOpenCalendar(Habit habit);
    }

    private final Context context;
    private final List<Habit> habits;
    private final Listener listener;
    private final HabitStore store;

    public HabitListAdapter(Context context, List<Habit> habits, Listener listener) {
        this.context = context;
        this.habits = habits;
        this.listener = listener;
        this.store = HabitStore.get(context);
    }

    @Override
    public int getCount() {
        return habits.size();
    }

    @Override
    public Object getItem(int position) {
        return habits.get(position);
    }

    @Override
    public long getItemId(int position) {
        return habits.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
            vh = new ViewHolder();
            vh.name = convertView.findViewById(R.id.habit_name);
            vh.streak = convertView.findViewById(R.id.habit_streak);
            vh.check = convertView.findViewById(R.id.btn_check);
            vh.calendar = convertView.findViewById(R.id.btn_calendar);
            vh.delete = convertView.findViewById(R.id.btn_delete);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        final Habit habit = habits.get(position);
        vh.name.setText(habit.name);

        long streak = store.computeStreak(habit.id);
        vh.streak.setText(streak > 0
                ? context.getString(R.string.streak, streak)
                : context.getString(R.string.no_streak));

        boolean todayChecked = store.isCheckedIn(habit.id, LocalDate.now());
        vh.check.setText(todayChecked ? R.string.checked_in : R.string.check_in);

        vh.check.setOnClickListener(v -> listener.onCheckIn(habit));
        vh.calendar.setOnClickListener(v -> listener.onOpenCalendar(habit));
        vh.delete.setOnClickListener(v -> listener.onDelete(habit));

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView streak;
        MaterialButton check;
        ImageButton calendar;
        ImageButton delete;
    }
}
