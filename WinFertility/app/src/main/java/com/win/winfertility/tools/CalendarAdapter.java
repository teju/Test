package com.win.winfertility.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.win.winfertility.R;
import com.win.winfertility.dto.CalendarCellClickArgs;
import com.win.winfertility.dto.CalendarCellInfo;
import com.win.winfertility.dto.CalendarCellRenderArgs;
import com.win.winfertility.utils.Common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarAdapter extends BaseAdapter {
    private WinCalendarView _parent;
    private List<CalendarCellInfo> _cells;
    private LayoutInflater _inflater;
    private SimpleDateFormat _fullFormatter;
    private String _today;
    private CalendarAdapter Pointer;

    private View.OnClickListener _onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            try {
                new Handler(Pointer._parent.getContext().getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(_parent.getOnCalendarCellClickListener() != null) {
                            CalendarCellInfo cellInfo = (CalendarCellInfo) view.getTag();
                            if(cellInfo != null) {
                                if(cellInfo.Text != null && cellInfo.Text.trim().length() > 0) {
                                    CalendarCellClickArgs args = new CalendarCellClickArgs();
                                    args.Date = cellInfo.Date;
                                    args.IsEventsExist = cellInfo.RenderArgs.IsEventsExist;
                                    args.IsRemindersExist = cellInfo.RenderArgs.IsRemindersExist;
                                    args.CellType = cellInfo.RenderArgs.CellType;
                                    Pointer._parent.getOnCalendarCellClickListener().onClick(view, args);
                                }
                            }
                        }
                    }
                }, 300);
            }
            catch(Exception ex) {
            }
        }
    };

    public CalendarAdapter(WinCalendarView parent) {
        this.Pointer = this;
        this._parent = parent;
        this._cells = new ArrayList<>();
        this._fullFormatter = new SimpleDateFormat("dd MMM yyyy");
        this.refresh(this._parent.getDate());
    }

    @Override
    public int getCount() {
        return this._cells.size();
    }
    @Override
    public CalendarCellInfo getItem(int position) {
        return this._cells.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            if(this._inflater == null) {
                this._inflater = (LayoutInflater) this._parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            view = this._inflater.inflate(R.layout.view_calendar_view_cell, null);
        }

        if(view !=  null) {
            CalendarCellInfo item = this.getItem(position);
            if(item != null) {
                item.RenderArgs = new CalendarCellRenderArgs();
                item.RenderArgs.Date = item.Date;
                if(item.Text != null && item.Text.trim().length() > 0) {
                    if (this._parent.getOnCalendarCellRenderListener() != null) {
                        this._parent.getOnCalendarCellRenderListener().onRender(_parent, view, item.RenderArgs);
                    }
                }
            }

            /*----- Setting background based on configuration -----*/
            TextView vw_text = (TextView) view.findViewById(R.id.vw_text);
            if(vw_text != null) {
                vw_text.setText(item.Text);
                if(item.Text != null && item.Text.trim().length() > 0) {
                    if (this._today.compareToIgnoreCase(item.DateText) == 0) {
                        Common.setBackground(vw_text, R.drawable.draw_calendar_cell_filled);
                        vw_text.setTextColor(Color.WHITE);
                    } else {
                        vw_text.setTextColor(Color.parseColor("#666666"));
                        switch (item.RenderArgs.CellType) {
                            case DASHED:
                                Common.setBackground(vw_text, R.drawable.draw_calendar_cell_dashed);
                                break;
                            case DOTTED:
                                Common.setBackground(vw_text, R.drawable.draw_calendar_cell_dotted);
                                break;
                            case SOLID:
                                Common.setBackground(vw_text, R.drawable.draw_calendar_cell_stroke);
                                break;
                            case PREVIOUS:
                                Common.setBackground(vw_text, R.drawable.draw_calendar_prevdays_cell_stroke);
                                break;
                            case NONE:
                                Common.setBackground(vw_text, new ColorDrawable(Color.argb(0, 0, 0, 0)));
                                break;
                        }
                    }
                }
                else {
                    Common.setBackground(vw_text, new ColorDrawable(Color.argb(0, 0, 0, 0)));
                }
            }

            /*----- Manage notification and events icons visibility -----*/
            View img_checked = view.findViewById(R.id.img_checked);
            if(img_checked != null) {
                img_checked.setVisibility(item.RenderArgs.IsEventsExist ? View.VISIBLE : View.GONE);
            }
            View img_bell = view.findViewById(R.id.img_bell);
            if(img_bell != null) {
                img_bell.setVisibility(item.RenderArgs.IsRemindersExist ? View.VISIBLE : View.GONE);
            }

            /*----- Setting cell details as tag -----*/
            view.setOnClickListener(this._onClickListener);
            view.setTag(item);
        }

        return view;
    }
    public void refresh(Date date) {
        this._today = this._fullFormatter.format(new Date());
        List<CalendarCellInfo> cells = new ArrayList<>();
        /*----- Get month start -----*/
        Date startDate = this.getMonthStart(date);
        /*----- Get day of week of month start -----*/
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int month = calendar.get(Calendar.MONTH);
        /*----- Get first calendar cell date -----*/
        while (dayOfWeek != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -1);
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        }
        for (int i = 0; i < 42; i++) {
            CalendarCellInfo info = new CalendarCellInfo();
            info.Date = new Date(calendar.getTime().getTime());
            info.DateText = this._fullFormatter.format(info.Date);
            if(calendar.get(Calendar.MONTH) == month) {
                info.Text = Integer.toString(calendar.get(Calendar.DATE));
            }
            cells.add(info);
            calendar.add(Calendar.DATE, 1);
        }
        this._cells = cells;
        this.notifyDataSetChanged();
    }
    private Date getMonthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        return calendar.getTime();
    }
}