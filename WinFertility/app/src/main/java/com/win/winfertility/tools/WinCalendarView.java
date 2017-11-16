package com.win.winfertility.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.win.winfertility.R;
import com.win.winfertility.utils.Common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WinCalendarView extends RelativeLayout {
    private GridView _grd_day_cells;
    private TextView _vw_month_name;
    private SimpleDateFormat _formatter;

    /*----- Date Property -----*/
    private Date _date;
    public Date getDate() {
        return this._date;
    }
    public void setDate(Date date) {
        String oldMonth = Common.replaceNull(this._formatter.format(this._date));
        String newMonth = Common.replaceNull(this._formatter.format(date));
        this._date = date;
        if(oldMonth.compareTo(newMonth) != 0) {
            if(this._onCalendarMonthChangedListener != null) {
                this._onCalendarMonthChangedListener.onChanged(this, this._date);
            }
        }
        this.reload();
    }
    /*----- Cell Render Event -----*/
    private OnCalendarCellRenderListener _onCalendarCellRenderListener;
    public void setOnCalendarCellRenderListener(OnCalendarCellRenderListener onCalendarCellRenderListener) {
        this._onCalendarCellRenderListener = onCalendarCellRenderListener;
    }
    public OnCalendarCellRenderListener getOnCalendarCellRenderListener() {
        return this._onCalendarCellRenderListener;
    }
    /*----- Month Changed Event -----*/
    private OnCalendarMonthChangedListener _onCalendarMonthChangedListener;
    public void setOnCalendarMonthChangedListener(OnCalendarMonthChangedListener onCalendarMonthChangedListener) {
        this._onCalendarMonthChangedListener = onCalendarMonthChangedListener;
    }
    public OnCalendarMonthChangedListener getOnCalendarMonthChangedListener() {
        return this._onCalendarMonthChangedListener;
    }
    /*----- Cell Click Event -----*/
    private OnCalendarCellClickListener _onCalendarCellClickListener;
    public void setOnCalendarCellClickListener(OnCalendarCellClickListener onCalendarCellClickListener) {
        this._onCalendarCellClickListener = onCalendarCellClickListener;
    }
    public OnCalendarCellClickListener getOnCalendarCellClickListener() {
        return this._onCalendarCellClickListener;
    }

    /*----- Constructor -----*/
    public WinCalendarView(Context context) {
        super(context);
        this.init(context, null);
    }
    public WinCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    /*----- Private Methods -----*/
    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater != null) {
            inflater.inflate(R.layout.view_calendar_view, this);
        }

        this._grd_day_cells = (GridView) this.findViewById(R.id.grd_day_cells);
        this._vw_month_name = (TextView) this.findViewById(R.id.vw_month_name);

        this._formatter = new SimpleDateFormat("MMMM yyyy");
        this._date = new Date();
        this.reload();
    }

    /*----- Public Methods -----*/
    public void reload() {
        if(this._vw_month_name != null) {
            this._vw_month_name.setText(this._formatter.format(this._date));
        }
        if(this._grd_day_cells != null) {
            if(this._grd_day_cells.getAdapter() == null) {
                this._grd_day_cells.setAdapter(new CalendarAdapter(this));
            }
            else {
                ((CalendarAdapter) this._grd_day_cells.getAdapter()).refresh(this.getDate());
            }
        }
    }
}
