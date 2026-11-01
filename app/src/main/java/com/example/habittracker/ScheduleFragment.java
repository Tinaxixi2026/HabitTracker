package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScheduleFragment extends Fragment {

    private HabitStore store;
    private YearMonth current;
    private LocalDate selected;

    private GridView grid;
    private TextView monthLabel;
    private TextView dayTitle;
    private ListView listView;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        store = HabitStore.get(requireContext());

        grid = view.findViewById(R.id.grid_schedule);
        monthLabel = view.findViewById(R.id.month_label);
        dayTitle = view.findViewById(R.id.day_title);
        listView = view.findViewById(R.id.list_schedules);
        emptyView = view.findViewById(R.id.empty_schedules);

        view.findViewById(R.id.btn_prev).setOnClickListener(v -> {
            current = current.minusMonths(1);
            renderCalendar();
        });
        view.findViewById(R.id.btn_next).setOnClickListener(v -> {
            current = current.plusMonths(1);
            renderCalendar();
        });

        MaterialButton add = view.findViewById(R.id.btn_add_schedule);
        add.setOnClickListener(v -> showAddDialog());

        grid.setOnItemClickListener((parent, v, position, id) -> {
            Object o = grid.getItemAtPosition(position);
            if (o instanceof LocalDate) {
                selected = (LocalDate) o;
                current = YearMonth.from(selected);
                renderCalendar();
            }
        });

        selected = LocalDate.now();
        current = YearMonth.now();
        renderCalendar();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (store != null) {
            renderCalendar();
        }
    }

    private void renderCalendar() {
        monthLabel.setText(current.getYear() + "年" + current.getMonthValue() + "月");
        Set<String> scheduleDates = store.getScheduleDates();
        LocalDate today = LocalDate.now();

        LocalDate first = current.atDay(1);
        int leading = first.getDayOfWeek().getValue() % 7; // 0=周日
        List<LocalDate> cells = new ArrayList<>();
        for (int i = 0; i < leading; i++) {
            cells.add(null);
        }
        int days = current.lengthOfMonth();
        for (int d = 1; d <= days; d++) {
            cells.add(current.atDay(d));
        }

        grid.setAdapter(new ScheduleCalendarAdapter(requireContext(), cells, scheduleDates, selected, today));
        renderScheduleList();
    }

    private void renderScheduleList() {
        dayTitle.setText(getString(R.string.day_schedules, selected.getMonthValue(), selected.getDayOfMonth()));
        List<Schedule> schedules = store.getSchedulesOn(selected);
        listView.setAdapter(new ScheduleListAdapter(requireContext(), schedules, this::confirmDelete));
        boolean empty = schedules.isEmpty();
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showAddDialog() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        final EditText titleInput = new EditText(requireContext());
        titleInput.setHint(R.string.schedule_title_hint);
        titleInput.setSingleLine(true);
        container.addView(titleInput);

        final EditText noteInput = new EditText(requireContext());
        noteInput.setHint(R.string.schedule_note_hint);
        noteInput.setSingleLine(true);
        container.addView(noteInput);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.day_schedules, selected.getMonthValue(), selected.getDayOfMonth()))
                .setView(container)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.name_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.addSchedule(title, noteInput.getText().toString().trim(), selected);
                    renderCalendar();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(Schedule schedule) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.confirm_delete_schedule, schedule.title))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    store.deleteSchedule(schedule.id);
                    renderCalendar();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
