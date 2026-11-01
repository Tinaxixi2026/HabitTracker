package com.example.habittracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.List;

public class HabitFragment extends Fragment {

    private HabitStore store;
    private ListView listView;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        store = HabitStore.get(requireContext());
        listView = view.findViewById(R.id.list_habits);
        emptyView = view.findViewById(R.id.empty_view);

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (store != null) {
            refresh();
        }
    }

    private void refresh() {
        List<Habit> habits = store.getHabits();

        HabitListAdapter adapter = new HabitListAdapter(requireContext(), habits, new HabitListAdapter.Listener() {
            @Override
            public void onCheckIn(Habit habit) {
                showCheckinDialog(habit);
            }

            @Override
            public void onDelete(Habit habit) {
                confirmDelete(habit);
            }

            @Override
            public void onOpenCalendar(Habit habit) {
                CalendarActivity.start(requireContext(), habit.id, habit.name);
            }
        });

        listView.setAdapter(adapter);
        boolean empty = habits.isEmpty();
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showCheckinDialog(Habit habit) {
        LocalDate today = LocalDate.now();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    LocalDate chosen = LocalDate.of(year, month + 1, dayOfMonth);
                    if (store.isCheckedIn(habit.id, chosen)) {
                        confirmUncheck(habit, chosen);
                    } else {
                        store.checkIn(habit.id, chosen);
                        Toast.makeText(requireContext(), R.string.checkin_success, Toast.LENGTH_SHORT).show();
                        refresh();
                    }
                },
                today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
        // 不允许选择未来日期
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.setTitle(R.string.check_in);
        dialog.show();
    }

    private void confirmUncheck(Habit habit, LocalDate date) {
        String dateStr = date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.uncheck_title)
                .setMessage(getString(R.string.uncheck_message, habit.name, dateStr))
                .setPositiveButton(R.string.uncheck, (dialog, which) -> {
                    store.uncheckIn(habit.id, date);
                    Toast.makeText(requireContext(), R.string.uncheck_done, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton(R.string.keep, null)
                .show();
    }

    private void showAddDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.habit_name_hint);
        input.setSingleLine(true);

        FrameLayout container = new FrameLayout(requireContext());
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, 0, pad, 0);
        container.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_habit)
                .setView(container)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.habit_name_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.addHabit(name);
                    refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(Habit habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.confirm_delete_habit, habit.name))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    store.deleteHabit(habit.id);
                    refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
