package com.win.winfertility.dto;

import java.util.Date;

public class CalendarCellRenderArgs {
    public boolean IsRemindersExist;
    public boolean IsEventsExist;
    public CalendarCellType CellType;
    public Date Date;

    public CalendarCellRenderArgs() {
        this.CellType = CalendarCellType.NONE;
    }
    public CalendarCellRenderArgs(boolean isRemindersExist, boolean isEventsExist, CalendarCellType cellType) {
        this.IsRemindersExist = isRemindersExist;
        this.IsEventsExist = isEventsExist;
        this.CellType = cellType;
    }
}
