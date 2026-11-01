package com.example.habittracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private long habitId;
    private HabitStore store;
    private YearMonth current;
    private GridView grid;
    private TextView monthLabel;

    public static void start(Context context, long habitId, String name) {
        Intent intent = new Intent(context, CalendarActivity.class);
        intent.putExtra("habit_id", habitId);
        intent.putExtra("habit_name", name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        habitId = getIntent().getLongExtra("habit_id", -1);
        String name = getIntent().getStringExtra("habit_name");
        store = HabitStore.get(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.calendar);
        toolbar.setSubtitle(name);
        toolbar.setNavigationOnClickListener(v -> finish());

        grid = findViewById(R.id.grid_days);
        monthLabel = findViewById(R.id.month_label);
        findViewById(R.id.btn_prev).setOnClickListener(v -> {
            current = current.minusMonths(1);
            render();
        });
        findViewById(R.id.btn_next).setOnClickListener(v -> {
            current = current.plusMonths(1);
            render();
        });

        current = YearMonth.now();
        render();
    }

    private void render() {
        monthLabel.setText(current.getYear() + "年" + current.getMonthValue() + "月");

        Set<String> checkins = store.getCheckinDates(habitId);
        LocalDate today = LocalDate.now();

        LocalDate first = current.atDay(1);
        // 周日为一周第一天：0=周日 ... 6=周六
        int leading = first.getDayOfWeek().getValue() % 7;

        List<LocalDate> cells = new ArrayList<>();
        for (int i = 0; i < leading; i++) {
            cells.add(null);
        }
        int days = current.lengthOfMonth();
        for (int d = 1; d <= days; d++) {
            cells.add(current.atDay(d));
        }

        grid.setAdapter(new CalendarGridAdapter(this, cells, checkins, today));
    }

    static String fmt(LocalDate d) {
        return d.format(FMT);
    }
}
