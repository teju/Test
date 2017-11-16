package com.win.winfertility.tools;

import android.view.View;

import com.win.winfertility.dto.CalendarCellRenderArgs;

public interface OnCalendarCellRenderListener {
    void onRender(WinCalendarView calendarView, View cell, CalendarCellRenderArgs args);
}
