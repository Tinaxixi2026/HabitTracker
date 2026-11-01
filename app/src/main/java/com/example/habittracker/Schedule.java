package com.example.habittracker;

public class Schedule {
    public final long id;
    public final String title;
    public final String note;
    public final String date; // "yyyy-MM-dd"

    public Schedule(long id, String title, String note, String date) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.date = date;
    }
}
