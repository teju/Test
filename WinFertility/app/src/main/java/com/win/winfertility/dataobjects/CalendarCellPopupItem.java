package com.win.winfertility.dataobjects;

public class CalendarCellPopupItem {
    public String ID = "";
    public int Image = 0;
    public String Title = "";
    public String Text = "";

    public CalendarCellPopupItem() {

    }
    public CalendarCellPopupItem(String id, int image, String title, String text) {
        this.ID = id;
        this.Image = image;
        this.Title = title;
        this.Text = text;
    }
}
