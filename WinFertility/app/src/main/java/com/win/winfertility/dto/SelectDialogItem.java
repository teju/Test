package com.win.winfertility.dto;

public class SelectDialogItem {
    public boolean IsSelected;
    public Object Data;
    public String ID;
    public String Text;
    public static String desc="";

    public SelectDialogItem() { }
    public SelectDialogItem(String text) {
        this(text, text, false);
    }
    public SelectDialogItem(String id, String text) {
        this(id, text, false);
    }
    public SelectDialogItem(String id, String text, boolean selected) {
        this.ID = id;
        this.Text = text;
        this.IsSelected = selected;
    }

}
