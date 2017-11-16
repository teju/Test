package com.win.winfertility.dto;

public class EventItem {
    public String ID = "";
    public String Text = "";
    public boolean IsSelected = false;
    public EventItem(String id) {
        this(id, "", false);
    }
    public EventItem(String id, String text) {
        this(id, text, false);
    }
    public EventItem(String id, String text, boolean isSelected) {
        this.ID = id;
        this.Text = text;
        this.IsSelected = isSelected;
    }
}
