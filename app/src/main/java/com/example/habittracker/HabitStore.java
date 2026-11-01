package com.example.habittracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据存储：用 SharedPreferences 持久化习惯、打卡日期和日程。
 * 全部使用 commit() 同步写盘，保证退出程序和关机后数据不丢失。
 */
public class HabitStore {

    private static final String PREFS = "habit_tracker";
    private static final String KEY_HABITS = "habits";
    private static final String KEY_SCHEDULES = "schedules";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static HabitStore instance;
    private final SharedPreferences prefs;

    private HabitStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized HabitStore get(Context context) {
        if (instance == null) {
            instance = new HabitStore(context);
        }
        return instance;
    }

    // ---------- 习惯 ----------

    public List<Habit> getHabits() {
        List<Habit> list = new ArrayList<>();
        String raw = prefs.getString(KEY_HABITS, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Habit(o.getLong("id"), o.getString("name"), o.getLong("createdAt")));
            }
        } catch (JSONException ignored) {
        }
        return list;
    }

    public Habit addHabit(String name) {
        List<Habit> habits = getHabits();
        long now = System.currentTimeMillis();
        Habit habit = new Habit(now, name, now);
        habits.add(habit);
        saveHabits(habits);
        return habit;
    }

    public void deleteHabit(long id) {
        List<Habit> habits = getHabits();
        for (int i = 0; i < habits.size(); i++) {
            if (habits.get(i).id == id) {
                habits.remove(i);
                break;
            }
        }
        saveHabits(habits);
        prefs.edit().remove(key(id)).commit();
    }

    // ---------- 打卡 ----------

    public Set<String> getCheckinDates(long habitId) {
        Set<String> set = new HashSet<>();
        String raw = prefs.getString(key(habitId), "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                set.add(arr.getString(i));
            }
        } catch (JSONException ignored) {
        }
        return set;
    }

    public boolean isCheckedIn(long habitId, LocalDate date) {
        return getCheckinDates(habitId).contains(fmt(date));
    }

    public void checkIn(long habitId, LocalDate date) {
        Set<String> set = getCheckinDates(habitId);
        set.add(fmt(date));
        saveDates(habitId, set);
    }

    public void uncheckIn(long habitId, LocalDate date) {
        Set<String> set = getCheckinDates(habitId);
        set.remove(fmt(date));
        saveDates(habitId, set);
    }

    public long computeStreak(long habitId) {
        Set<String> dates = getCheckinDates(habitId);
        LocalDate d = LocalDate.now();
        if (!dates.contains(fmt(d))) {
            d = d.minusDays(1);
        }
        long streak = 0;
        while (dates.contains(fmt(d))) {
            streak++;
            d = d.minusDays(1);
        }
        return streak;
    }

    // ---------- 日程 ----------

    public List<Schedule> getSchedules() {
        List<Schedule> list = new ArrayList<>();
        String raw = prefs.getString(KEY_SCHEDULES, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Schedule(o.getLong("id"), o.getString("title"),
                        o.optString("note", ""), o.getString("date")));
            }
        } catch (JSONException ignored) {
        }
        return list;
    }

    public List<Schedule> getSchedulesOn(LocalDate date) {
        String d = fmt(date);
        List<Schedule> result = new ArrayList<>();
        for (Schedule s : getSchedules()) {
            if (s.date.equals(d)) {
                result.add(s);
            }
        }
        return result;
    }

    /** 有日程的日期集合，用于日历上标点。 */
    public Set<String> getScheduleDates() {
        Set<String> set = new HashSet<>();
        for (Schedule s : getSchedules()) {
            set.add(s.date);
        }
        return set;
    }

    public Schedule addSchedule(String title, String note, LocalDate date) {
        List<Schedule> list = getSchedules();
        Schedule s = new Schedule(System.currentTimeMillis(), title, note, fmt(date));
        list.add(s);
        saveSchedules(list);
        return s;
    }

    public void deleteSchedule(long id) {
        List<Schedule> list = getSchedules();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                list.remove(i);
                break;
            }
        }
        saveSchedules(list);
    }

    // ---------- 统计 ----------

    public int totalSchedules() {
        return getSchedules().size();
    }

    public int totalCheckins() {
        int total = 0;
        for (Habit h : getHabits()) {
            total += getCheckinDates(h.id).size();
        }
        return total;
    }

    /** 所有习惯的打卡日期并集（用于“我的”页日历填色）。 */
    public Set<String> getAllCheckinDates() {
        Set<String> all = new HashSet<>();
        for (Habit h : getHabits()) {
            all.addAll(getCheckinDates(h.id));
        }
        return all;
    }

    /** 近 N 个月（含当月）每个月的打卡总次数。 */
    public List<MonthCount> monthlyCheckins(int months) {
        Map<String, Integer> map = new HashMap<>();
        for (Habit h : getHabits()) {
            for (String d : getCheckinDates(h.id)) {
                String mk = d.substring(0, 7); // "yyyy-MM"
                map.put(mk, map.getOrDefault(mk, 0) + 1);
            }
        }
        YearMonth now = YearMonth.now();
        List<MonthCount> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth m = now.minusMonths(i);
            String mk = String.format("%04d-%02d", m.getYear(), m.getMonthValue());
            result.add(new MonthCount(m.getMonthValue() + "月", map.getOrDefault(mk, 0)));
        }
        return result;
    }

    /** 打卡次数最多的习惯。 */
    public HabitCount mostCheckedHabit() {
        Habit best = null;
        int bestCount = 0;
        for (Habit h : getHabits()) {
            int c = getCheckinDates(h.id).size();
            if (c > bestCount) {
                bestCount = c;
                best = h;
            }
        }
        if (best == null) {
            return null;
        }
        return new HabitCount(best.name, bestCount);
    }

    // ---------- 内部 ----------

    private void saveHabits(List<Habit> habits) {
        JSONArray arr = new JSONArray();
        try {
            for (Habit h : habits) {
                JSONObject o = new JSONObject();
                o.put("id", h.id);
                o.put("name", h.name);
                o.put("createdAt", h.createdAt);
                arr.put(o);
            }
        } catch (JSONException ignored) {
        }
        prefs.edit().putString(KEY_HABITS, arr.toString()).commit();
    }

    private void saveSchedules(List<Schedule> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Schedule s : list) {
                JSONObject o = new JSONObject();
                o.put("id", s.id);
                o.put("title", s.title);
                o.put("note", s.note == null ? "" : s.note);
                o.put("date", s.date);
                arr.put(o);
            }
        } catch (JSONException ignored) {
        }
        prefs.edit().putString(KEY_SCHEDULES, arr.toString()).commit();
    }

    private void saveDates(long habitId, Set<String> set) {
        JSONArray arr = new JSONArray(new ArrayList<>(set));
        prefs.edit().putString(key(habitId), arr.toString()).commit();
    }

    private static String key(long id) {
        return "ck_" + id;
    }

    static String fmt(LocalDate d) {
        return d.format(FMT);
    }

    public static class MonthCount {
        public final String label;
        public final int count;

        public MonthCount(String label, int count) {
            this.label = label;
            this.count = count;
        }
    }

    public static class HabitCount {
        public final String name;
        public final int count;

        public HabitCount(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }
}
