package com.win.winfertility;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.CalendarCellDataArgs;
import com.win.winfertility.dataobjects.CalendarCellDataResult;
import com.win.winfertility.dataobjects.CalendarCellPopupItem;
import com.win.winfertility.dataobjects.CalendarDataArgs;
import com.win.winfertility.dataobjects.CalendarDataResult;
import com.win.winfertility.dataobjects.CalendarTaggedData;
import com.win.winfertility.dataobjects.ReminderSummary;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.dto.CalendarCellClickArgs;
import com.win.winfertility.dto.CalendarCellRenderArgs;
import com.win.winfertility.dto.CalendarCellType;
import com.win.winfertility.tools.OnCalendarCellClickListener;
import com.win.winfertility.tools.OnCalendarCellRenderListener;
import com.win.winfertility.tools.OnCalendarMonthChangedListener;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.tools.WinCalendarView;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyFertilityActivity extends WINFertilityActivity {
    public static MyFertilityActivity Instance;
    private MyFertilityActivity Pointer;
    /*----- Main page controls -----*/
    private WinCalendarView _vw_calendar_1;
    private WinCalendarView _vw_calendar_2;
    private CalendarDataResult _menstrualData;
    private TextView _vw_ovulation_countdown;
    private TextView _vw_period_countdown;
    /*----- Popup controls -----*/
    private View _vw_overlay;
    private View _vw_add_event;
    private View _vw_add_reminder;
    private View _vw_popup_panel;
    private TextView _lbl_month_name;
    private TextView _lbl_day_name;
    private TextView _lbl_day_no;
    private ListView _lvw_events;
    private View _vw_no_events;
    private ListView _lvw_reminders;
    private View _vw_no_reminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyFertilityActivity.Instance = this;
        this.setContentView(R.layout.activity_my_fertility);
        this.Pointer = this;
        this.init();
        this.initCalendarDataPopup();
        this.loadMenstrualData();
        this.handleFCMNotificationMsg();
    }
    @Override
    public void onBackPressed() {
        if(Pointer._vw_overlay.getVisibility() != View.GONE) {
            Pointer._vw_overlay.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    public void showPopupByDate(String date) {
        Date selected = Calendar.getInstance().getTime();
        try {
            selected = new SimpleDateFormat("M/d/yyyy h:m:s a").parse(date); //"9/28/2017 7:57:26 AM"
        } catch (Exception e) {
        }
        if (this._vw_calendar_1 != null) {
            this._vw_calendar_1.setDate(selected);
        }
        if (this._vw_calendar_2 != null) {
            this._vw_calendar_2.setDate(selected);
        }
        CalendarCellClickArgs args = new CalendarCellClickArgs();
        args.Date = selected;
        handleCalendarCellClick(null, args);
    }

    private void init() {
        /*----- Handling Events -----*/
        OnCalendarCellClickListener onCalendarCellClickListener = new OnCalendarCellClickListener() {
            @Override
            public void onClick(View cell, CalendarCellClickArgs args) {
                Pointer.handleCalendarCellClick(cell, args);
            }
        };
        OnCalendarMonthChangedListener onCalendarMonthChangedListener = new OnCalendarMonthChangedListener() {
            @Override
            public void onChanged(View calendar, Date date) {
                Pointer.handleCalendarMonthChanged(calendar, date);
            }
        };
        OnCalendarCellRenderListener onCalendarCellRenderListener = new OnCalendarCellRenderListener() {
            @Override
            public void onRender(WinCalendarView calendarView, View cell, CalendarCellRenderArgs args) {
                Pointer.handleCalendarCellRender(calendarView, cell, args);
            }
        };
        /*----- Finding Controls -----*/
        Pointer._vw_ovulation_countdown = (TextView) this.findViewById(R.id.vw_ovulation_countdown);
        Pointer._vw_period_countdown = (TextView) this.findViewById(R.id.vw_period_countdown);

        Pointer._vw_calendar_1 = (WinCalendarView) this.findViewById(R.id.vw_calendar_1);
        if(Pointer._vw_calendar_1 != null) {
            Pointer._vw_calendar_1.setOnCalendarCellRenderListener(onCalendarCellRenderListener);
            Pointer._vw_calendar_1.setOnCalendarCellClickListener(onCalendarCellClickListener);
            Pointer._vw_calendar_1.setOnCalendarMonthChangedListener(onCalendarMonthChangedListener);
        }

        Pointer._vw_calendar_2 = (WinCalendarView) this.findViewById(R.id.vw_calendar_2);
        if(Pointer._vw_calendar_2 != null) {
            Pointer._vw_calendar_2.setOnCalendarCellRenderListener(onCalendarCellRenderListener);
            Pointer._vw_calendar_2.setOnCalendarCellClickListener(onCalendarCellClickListener);
            Pointer._vw_calendar_2.setOnCalendarMonthChangedListener(onCalendarMonthChangedListener);
        }

        View lnk_update_menstrual_data = this.findViewById(R.id.lnk_update_menstrual_data);
        if(lnk_update_menstrual_data != null) {
            lnk_update_menstrual_data.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Intent intent = new Intent(Pointer, MenstrualInfoActivity.class);
                    intent.putExtra(Shared.EXTRA_PARENT_CLASS, MyFertilityActivity.class.getName());
                    Pointer.startActivity(intent);
                    return false;
                }
            });
        }
    }
    private void handleFCMNotificationMsg() {
        Intent intent = this.getIntent();
        if(intent != null) {
            if(intent.hasExtra(Shared.EXTRA_NOTIFICATION_DATE)){
                String date = intent.getStringExtra(Shared.EXTRA_NOTIFICATION_DATE);
                if(TextUtils.isEmpty(date) == false) {
                    showPopupByDate(date);
                }
            }
        }
    }
    private void initCalendarDataPopup() {
        /*----- Overlay popup -----*/
        Pointer._vw_overlay = this.findViewById(R.id.vw_overlay);
        if(Pointer._vw_overlay != null) {
            Pointer._vw_overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer._vw_overlay.setVisibility(View.GONE);
                }
            });
        }
        Pointer._vw_add_event = this.findViewById(R.id.vw_add_event);
        if(Pointer._vw_add_event != null) {
            Pointer._vw_add_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.openCalendarDataSetupScreen(EventActivity.class, "");
                }
            });
        }
        Pointer._vw_add_reminder = this.findViewById(R.id.vw_add_reminder);
        if(Pointer._vw_add_reminder != null) {
            Pointer._vw_add_reminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.openCalendarDataSetupScreen(ReminderActivity.class, "");
                }
            });
        }
        Pointer._vw_popup_panel = this.findViewById(R.id.vw_popup_panel);
        if(Pointer._vw_popup_panel != null) {
            Pointer._vw_popup_panel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
        Pointer._lbl_month_name = (TextView) this.findViewById(R.id.lbl_month_name);
        Pointer._lbl_day_name = (TextView) this.findViewById(R.id.lbl_day_name);
        Pointer._lbl_day_no = (TextView) this.findViewById(R.id.lbl_day_no);

        Pointer._lvw_events = (ListView) this.findViewById(R.id.lvw_events);
        Pointer._lvw_reminders = (ListView) this.findViewById(R.id.lvw_reminders);

        Pointer._vw_no_reminders = this.findViewById(R.id.vw_no_reminders);
        Pointer._vw_no_events = this.findViewById(R.id.vw_no_events);
    }
    private void handleCalendarCellRender(WinCalendarView calendarView, View cell, CalendarCellRenderArgs args) {
        String date = Common.WinAppDateFormat.format(args.Date);
        CalendarTaggedData tag = new CalendarTaggedData();
        if(calendarView.getTag() != null && calendarView.getTag() instanceof CalendarTaggedData) {
            tag = (CalendarTaggedData) calendarView.getTag();
        }
        /*----- Setting event status -----*/
        String events = Common.replaceNull(tag.EventDates);
        args.IsEventsExist = events.contains(date);
        /*----- Setting reminder status -----*/
        String reminders = Common.replaceNull(tag.ReminderDates);
        args.IsRemindersExist = reminders.contains(date);
        /*----- Setting menstrual cycle date status -----*/
        String monthlyCycle = Common.replaceNull(tag.MonthlyCycle);
        if(monthlyCycle.contains(date)) {
            args.CellType = CalendarCellType.PREVIOUS;
        }
        else {
            if (Pointer._menstrualData != null) {
                if (Pointer._menstrualData.CurrentCycle.contains(date)) {
                    args.CellType = CalendarCellType.SOLID;
                } else if (Pointer._menstrualData.OvulationCycle.contains(date)) {
                    args.CellType = CalendarCellType.DASHED;
                } else if (Pointer._menstrualData.NextCycle.contains(date)) {
                    args.CellType = CalendarCellType.DOTTED;
                }
            }
        }
    }
    private void handleCalendarMonthChanged(final View calendar, final Date date) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(calendar != null && calendar instanceof WinCalendarView) {
                    final WinCalendarView calendarView = (WinCalendarView) calendar;
                    calendarView.setTag(new CalendarTaggedData());

                    Pointer.fetchCalendarData(date, new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            if(msg != null && msg.obj != null && msg.obj instanceof CalendarDataResult) {
                                CalendarDataResult data = (CalendarDataResult) msg.obj;
                                if(data != null) {
                                    CalendarTaggedData tag = new CalendarTaggedData();
                                    tag.EventDates = Common.replaceNull(data.EventDate);
                                    tag.ReminderDates = Common.replaceNull(data.ReminderDate);
                                    tag.MonthlyCycle = Common.replaceNull(data.MonthlyCycle);
                                    calendarView.setTag(tag);
                                    calendarView.reload();
                                    return true;
                                }
                            }
                            calendarView.reload();
                            return true;
                        }
                    }));
                    calendarView.reload();
                }
            }
        });
    }
    private void handleCalendarCellClick(View cell, final CalendarCellClickArgs args) {
        if(Pointer._vw_overlay != null) {
            Loader.show(Pointer);
            /*----- Setting selected date -----*/
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM");
            Pointer._lbl_month_name.setText(formatter.format(args.Date).toUpperCase());
            formatter = new SimpleDateFormat("EEEE");
            Pointer._lbl_day_name.setText(formatter.format(args.Date).toUpperCase());
            formatter = new SimpleDateFormat("dd");
            Pointer._lbl_day_no.setText(formatter.format(args.Date));

            /*----- Fetching calendar date related events and reminders -----*/
            String dateType = "C";
            Date selected = Common.getDateWithoutTime(args.Date);
            Date today = Common.getDateWithoutTime(new Date());
            if(selected.compareTo(today) != 0) {
                if(selected.before(today)) {
                    dateType = "P";
                }
                else if(selected.after(today)) {
                    dateType = "N";
                }
            }
            if(Pointer._vw_add_reminder != null) {
                Pointer._vw_add_reminder.setVisibility(("CN").contains(dateType) ? View.VISIBLE : View.GONE);
            }
            if(Pointer._vw_add_event != null) {
                Pointer._vw_add_event.setVisibility(("CP").contains(dateType) ? View.VISIBLE : View.GONE);
            }

            /*----- Loading data from server -----*/
            CalendarCellDataArgs data = new CalendarCellDataArgs();
            data.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
            data.Date = Common.WinAppDateFormat.format(args.Date);
            Common.invokeAPI(Pointer, ServiceMethods.GetCalendarDataByDate, data, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if (msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult result = (ApiResult) msg.obj;
                            if (result != null && TextUtils.isEmpty(result.Json) == false) {
                                CalendarCellDataResult cellData = new Gson().fromJson(result.Json, CalendarCellDataResult.class);
                                try {
                                    Pointer.loadCalendarCellData(cellData);
                                } catch (Exception ex) {
                                }
                                Pointer._vw_overlay.setTag(args.Date);
                                Pointer._vw_overlay.setVisibility(View.VISIBLE);
                                return true;
                            }
                            if(TextUtils.isEmpty(result.Error) == false) {
                                error = result.Error;
                            }
                        }
                    }
                    catch(Exception ex) {
                    }
                    finally {
                        Loader.hide();
                    }
                    /*----- Handling Error -----*/
                    if(TextUtils.isEmpty(error)) {
                        error = AppMsg.MSG_FAILED;
                    }
                    Notify.show(Pointer, error);
                    return false;
                }
            }));
        }
    }
    private void loadCalendarCellData(CalendarCellDataResult data) {
        if(data == null) {
            data = new CalendarCellDataResult();
        }

        List<CalendarCellPopupItem> events = new ArrayList<>();
        /*----- Loading events from data object -----*/
        if(data.Events != null) {
            if(TextUtils.isEmpty(data.Events.SexualActivity) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_sex, "", data.Events.SexualActivity));
            }
            if(TextUtils.isEmpty(data.Events.Period) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_your_flow, "", data.Events.Period));
            }
            if(TextUtils.isEmpty(data.Events.SexDrive) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_sex_drive, "", data.Events.SexDrive));
            }
            if(TextUtils.isEmpty(data.Events.PersonalSymptoms) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_symptoms, "", data.Events.PersonalSymptoms));
            }
            if(TextUtils.isEmpty(data.Events.Mood) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_feeling, "", data.Events.Mood));
            }
            if(TextUtils.isEmpty(data.Events.Medications) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_medication, "", data.Events.Medications));
            }
            if(TextUtils.isEmpty(data.Events.OvulationTests) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_ovulation_test, "", data.Events.OvulationTests));
            }
            if(TextUtils.isEmpty(data.Events.PregnancyTests) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_pregnancy_test, "", data.Events.PregnancyTests));
            }
            if(TextUtils.isEmpty(data.Events.Notes) == false) {
                events.add(new CalendarCellPopupItem(data.Events.EventID, R.drawable.win_notes, "", data.Events.Notes));
            }
        }
        if(Pointer._lvw_events != null) {
            Pointer._lvw_events.setAdapter(new CellDataAdapter(Pointer, events, R.layout.view_event_summary_item, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if(msg != null && msg.obj != null && msg.obj instanceof CalendarCellPopupItem) {
                        CalendarCellPopupItem item = (CalendarCellPopupItem) msg.obj;
                        Pointer.openCalendarDataSetupScreen(EventActivity.class, item.ID);
                    }
                    return true;
                }
            })));
        }
        if(Pointer._vw_no_events != null) {
            Pointer._vw_no_events.setVisibility(events.size() > 0 ? View.GONE : View.VISIBLE);
        }

        List<CalendarCellPopupItem> reminders = new ArrayList<>();
        /*----- Loading reminders from data object -----*/
        if(data.Reminders != null) {
            SimpleDateFormat jsonDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            for(ReminderSummary reminder : data.Reminders) {
                if(TextUtils.isEmpty(reminder.ReminderID) == false) {
                    String time = "";
                    try {
                        time = timeFormat.format(jsonDateFormatter.parse(reminder.StartTime)) + " - " +
                                timeFormat.format(jsonDateFormatter.parse(reminder.EndTime));
                    } catch (Exception ex) {
                    }
                    reminders.add(new CalendarCellPopupItem(reminder.ReminderID, 0, reminder.ReminderTitle, time));
                }
            }
        }
        if(Pointer._lvw_reminders != null) {
            Pointer._lvw_reminders.setAdapter(new CellDataAdapter(Pointer, reminders, R.layout.view_reminder_summary_item, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if(msg != null && msg.obj != null && msg.obj instanceof CalendarCellPopupItem) {
                        CalendarCellPopupItem item = (CalendarCellPopupItem) msg.obj;
                        Pointer.openCalendarDataSetupScreen(ReminderActivity.class, item.ID);
                    }
                    return true;
                }
            })));
        }
        if(Pointer._vw_no_reminders != null) {
            Pointer._vw_no_reminders.setVisibility(reminders.size() > 0 ? View.GONE : View.VISIBLE);
        }
    }
    private void loadMenstrualData() {
        Loader.show(Pointer);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Pointer._menstrualData = new CalendarDataResult();
                Pointer.fetchCalendarData(new Date(), new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        if(msg != null && msg.obj != null && msg.obj instanceof CalendarDataResult) {
                            CalendarDataResult data = (CalendarDataResult) msg.obj;
                            if(data != null) {
                                Pointer._menstrualData = data;
                                CalendarTaggedData tag = new CalendarTaggedData();
                                tag.EventDates = data.EventDate;
                                tag.ReminderDates = data.ReminderDate;
                                tag.MonthlyCycle = data.MonthlyCycle;
                                Pointer._vw_calendar_1.setTag(tag);
                                Pointer._vw_calendar_2.setTag(tag);
                                /*----- Re-rendering calendar month cells -----*/
                                Pointer._vw_calendar_1.reload();
                                Pointer._vw_calendar_2.reload();

                                /*----- Setting cycle countdowns -----*/
                                if(Pointer._vw_ovulation_countdown != null) {
                                    Pointer._vw_ovulation_countdown.setText(Integer.toString(Pointer._menstrualData.OvulationCountdown));
                                    Pointer._vw_period_countdown.setText(Integer.toString(Pointer._menstrualData.PeriodCountdown));
                                }
                            }
                        }
                        Loader.hide();
                        return true;
                    }
                }));
            }
        }, 1000);
    }
    private void fetchCalendarData(Date date, final Handler handler) {
        CalendarDataArgs args = new CalendarDataArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        args.Date = Common.WinAppDateFormat.format(date);

        Common.invokeAPI(Pointer, ServiceMethods.GetCalendarData, args, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                CalendarDataResult data = null;
                try {
                    if (msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                        ApiResult response = (ApiResult) msg.obj;
                        if (response != null && TextUtils.isEmpty(response.Json) == false) {
                            data = new Gson().fromJson(response.Json, CalendarDataResult.class);
                            if (data != null) {
                                data.CurrentCycle = Common.replaceNull(data.CurrentCycle);
                                data.OvulationCycle = Common.replaceNull(data.OvulationCycle);
                                data.NextCycle = Common.replaceNull(data.NextCycle);
                                data.MonthlyCycle = Common.replaceNull(data.MonthlyCycle);
                                data.EventDate = Common.replaceNull(data.EventDate);
                                data.ReminderDate = Common.replaceNull(data.ReminderDate);
                            }
                        }
                    }
                } catch (Exception ex) {
                }
                if(handler != null) {
                    handler.sendMessage(handler.obtainMessage(0, data));
                }
                return true;
            }
        }));
    }
    private <T> void openCalendarDataSetupScreen(Class<T> type, String id) {
        if(Pointer._vw_overlay.getTag() != null && Pointer._vw_overlay.getTag() instanceof Date) {
            if(type == EventActivity.class) {
                /*----- Selecting the existing event id -----*/
                if (TextUtils.isEmpty(id)) {
                    if (Pointer._lvw_events != null &&
                            Pointer._lvw_events.getAdapter() != null &&
                            Pointer._lvw_events.getAdapter() instanceof CellDataAdapter) {
                        CellDataAdapter adapter = (CellDataAdapter) Pointer._lvw_events.getAdapter();
                        if (adapter.getCount() > 0) {
                            id = adapter.getItem(0).ID;
                        }
                    }
                }
            }

            Date date = (Date) Pointer._vw_overlay.getTag();
            /*----- Opening data entry screen -----*/
            Intent intent = new Intent(MyFertilityActivity.this, type);
            intent.putExtra("DATE", date.getTime());
            intent.putExtra("ID", id);
            Pointer.startActivity(intent);
        }
        Pointer._vw_overlay.setVisibility(View.GONE);
    }
    private String getTagText(View view, int key) {
        String tag = "";
        try {
            tag = view.getTag(key).toString();
        } catch (Exception ex) {
        }
        return tag;
    }
    private class CellDataAdapter extends BaseAdapter {
        private CellDataAdapter Pointer;

        private Handler _handler;
        private int _layout;
        private Context _context;
        private List<CalendarCellPopupItem> _items;
        private LayoutInflater _inflater;

        public CellDataAdapter(Context context, List<CalendarCellPopupItem> items, int layout, Handler handler) {
            this.Pointer = this;
            this._context = context;
            this._handler = handler;
            this._items = items;
            this._layout = layout;

            if(this._items == null) {
                this._items = new ArrayList<>();
            }
        }
        @Override
        public int getCount() {
            return this._items.size();
        }
        @Override
        public CalendarCellPopupItem getItem(int position) {
            return this._items.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if(view == null) {
                if(this._inflater == null) {
                    this._inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                view = this._inflater.inflate(this._layout, null);
            }

            if(view != null) {
                CalendarCellPopupItem item = this.getItem(position);
                if(item == null) {
                    item = new CalendarCellPopupItem();
                }

                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v != null && v.getTag() != null && v.getTag() instanceof CalendarCellPopupItem) {
                            if (Pointer._handler != null) {
                                Pointer._handler.sendMessage(Pointer._handler.obtainMessage(0, v.getTag()));
                            }
                        }
                    }
                };

                view.setTag(item);
                view.setOnClickListener(onClickListener);

                ImageView vw_image = (ImageView) view.findViewById(R.id.vw_image);
                if(vw_image != null) {
                    vw_image.setImageDrawable(Common.getDrawable(this._context, item.Image));
                }
                TextView vw_title = (TextView) view.findViewById(R.id.vw_title);
                if(vw_title != null) {
                    vw_title.setText(item.Title);
                    vw_title.setTag(item);
                    vw_title.setOnClickListener(onClickListener);
                }
                TextView vw_text = (TextView) view.findViewById(R.id.vw_text);
                if(vw_text != null) {
                    vw_text.setText(item.Text);
                    vw_text.setTag(item);
                    vw_text.setOnClickListener(onClickListener);
                }
            }

            return view;
        }
    }
}
