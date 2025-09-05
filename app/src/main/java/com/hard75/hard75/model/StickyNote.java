package com.hard75.hard75.model;

public class StickyNote {
    private final long id;
    private final String title;
    private final String body;
    private final int colorRes;

    public StickyNote(long id, String title, String body, int colorRes) {
        this.id = id; this.title = title; this.body = body; this.colorRes = colorRes;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public int getColorRes() { return colorRes; }
}
