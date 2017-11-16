package com.win.winfertility.tools;

import android.view.View;

import com.win.winfertility.dto.CalendarCellClickArgs;

public interface OnCalendarCellClickListener {
    void onClick(View cell, CalendarCellClickArgs args);
}
