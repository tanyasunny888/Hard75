package com.hard75.hard75.model;

public class DaySticker {
    public final int dayIndex;
    public final boolean completed; // из day_progress
    public final int percent;       // округлённый прогресс 0..100
    public DaySticker(int dayIndex, boolean completed, int percent) {
        this.dayIndex = dayIndex;
        this.completed = completed;
        this.percent = percent;
    }
}
