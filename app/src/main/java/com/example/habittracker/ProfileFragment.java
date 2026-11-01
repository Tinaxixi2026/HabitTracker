package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProfileFragment extends Fragment {

    private HabitStore store;
    private TextView statSchedules;
    private TextView statCheckins;
    private BarChartView barChart;
    private TextView topHabit;

    private YearMonth current;
    private GridView grid;
    private TextView monthLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        store = HabitStore.get(requireContext());
        statSchedules = view.findViewById(R.id.stat_schedules);
        statCheckins = view.findViewById(R.id.stat_checkins);
        barChart = view.findViewById(R.id.bar_chart);
        topHabit = view.findViewById(R.id.top_habit);

        grid = view.findViewById(R.id.profile_grid);
        monthLabel = view.findViewById(R.id.profile_month_label);
        view.findViewById(R.id.btn_profile_prev).setOnClickListener(v -> {
            current = current.minusMonths(1);
            renderCalendar();
        });
        view.findViewById(R.id.btn_profile_next).setOnClickListener(v -> {
            current = current.plusMonths(1);
            renderCalendar();
        });

        current = YearMonth.now();
        renderCalendar();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (store != null) {
            refresh();
        }
    }

    private void refresh() {
        statSchedules.setText(String.valueOf(store.totalSchedules()));
        statCheckins.setText(String.valueOf(store.totalCheckins()));
        barChart.setData(store.monthlyCheckins(12));

        HabitStore.HabitCount hc = store.mostCheckedHabit();
        if (hc == null) {
            topHabit.setText(R.string.no_checkin_data);
        } else {
            topHabit.setText(hc.name + " · " + getString(R.string.times, hc.count));
        }

        renderCalendar();
    }

    private void renderCalendar() {
        monthLabel.setText(current.getYear() + "年" + current.getMonthValue() + "月");

        Set<String> checkins = store.getAllCheckinDates();
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

        grid.setAdapter(new CalendarGridAdapter(requireContext(), cells, checkins, today));
    }
}
