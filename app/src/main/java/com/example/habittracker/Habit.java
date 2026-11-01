package com.example.habittracker;

public class Habit {
    public final long id;
    public final String name;
    public final long createdAt;

    public Habit(long id, String name, long createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }
}
