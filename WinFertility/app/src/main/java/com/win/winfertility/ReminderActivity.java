package com.win.winfertility;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.ReminderDataArgs;
import com.win.winfertility.dataobjects.ReminderDeleteArgs;
import com.win.winfertility.dataobjects.ReminderReadArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.dto.SelectDialogItem;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.SelectBox;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderActivity extends WINFertilityActivity {
    private ReminderActivity Pointer;

    private List<SelectDialogItem> _remindIntervals;
    private List<SelectDialogItem> _reminder_type=new ArrayList<>();
    private List<SelectDialogItem> _repeatIntervals;
    private SimpleDateFormat _formatter = new SimpleDateFormat("EEEE, MMM dd hh:mm a");
    private SimpleDateFormat _jsonDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private Date _date = new Date();
    private String _reminderID = "";
    private String rem_type = "";

    private EditText _txt_title;
    private ToggleButton _tb_all_days;
    private TextView _lbl_start_time;
    private TextView _lbl_end_time;
    private EditText _txt_email_to;
    private EditText _txt_reminder_text;
    private TextView _vw_alert_time;
    private TextView reminder_type;
    private TextView _lbl_alert_time;
    private EditText _txt_location;
    private TextView _vw_repeat;
    private TextView _lbl_repeat;

    private View _btn_save;
    private View _btn_cancel;
    private View _btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_reminder);
        SelectDialogItem.desc="";
        this.Pointer = this;
        this.init();
    }

    private void init() {
        Pointer.loadSelectDialogItems();

        /*----- Finding Controls -----*/
        Pointer._txt_title = (EditText) this.findViewById(R.id.txt_title);
        Pointer._tb_all_days = (ToggleButton) this.findViewById(R.id.tb_all_days);
        Pointer._lbl_start_time = (TextView) this.findViewById(R.id.lbl_start_time);
        Pointer._lbl_end_time = (TextView) this.findViewById(R.id.lbl_end_time);
        Pointer._txt_email_to = (EditText) this.findViewById(R.id.txt_email_to);
        Pointer._txt_reminder_text = (EditText) this.findViewById(R.id.txt_reminder_text);
        Pointer._vw_alert_time = (TextView) this.findViewById(R.id.vw_alert_time);
        Pointer.reminder_type = (TextView) this.findViewById(R.id.reminder_type);
        Pointer._lbl_alert_time = (TextView) this.findViewById(R.id.lbl_alert_time);
        Pointer._txt_location = (EditText) this.findViewById(R.id.txt_location);
        Pointer._vw_repeat = (TextView) this.findViewById(R.id.vw_repeat);
        Pointer._lbl_repeat = (TextView) this.findViewById(R.id.lbl_repeat);

        if(Pointer._tb_all_days != null) {
            Pointer._tb_all_days.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Pointer.setTime(Pointer._lbl_start_time, isChecked ? 0 : 8, 0);
                    Pointer.setTime(Pointer._lbl_end_time, isChecked ? 23 : 8, isChecked ? 59 : 30);
                    Pointer._lbl_start_time.setEnabled(isChecked ? false : true);
                    Pointer._lbl_start_time.setTextColor(Color.parseColor((isChecked ? "#AAAAAA" : "#E91863")));
                    Pointer._lbl_end_time.setEnabled(isChecked ? false : true);
                    Pointer._lbl_end_time.setTextColor(Color.parseColor((isChecked ? "#AAAAAA" : "#E91863")));
                }
            });
        }

        /*----- Attaching dropdown -----*/
        if(Pointer._vw_alert_time != null) {
            SelectBox.config(Pointer._vw_alert_time, Pointer._remindIntervals, "Remind Before",
                    false, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if(msg.obj != null && msg.obj instanceof SelectDialogItem) {
                        SelectDialogItem item = (SelectDialogItem) msg.obj;
                        Pointer._lbl_alert_time.setText("Remind " + item.Text + " Before");
                    }
                    return true;
                }
            }));
            SelectBox.setData(Pointer._vw_alert_time, new SelectDialogItem("15", ""));
        }

        if(Pointer.reminder_type != null) {
            SelectBox.config(Pointer.reminder_type, Pointer._reminder_type, "Reminder Type",
                    true, new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            if(msg.obj != null && msg.obj instanceof List) {
                                if(SelectDialogItem.desc.length() != 0) {
                                    Pointer.reminder_type.setText(SelectDialogItem.desc);
                                }
                            }
                            return true;
                        }
                    }));

            SelectBox.setData(Pointer.reminder_type, new SelectDialogItem("15", "Reminder Type"));
        }


        if(Pointer._vw_repeat != null) {
            SelectBox.config(Pointer._vw_repeat, Pointer._repeatIntervals, "Repeat Interval", false, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if(msg.obj != null && msg.obj instanceof SelectDialogItem) {
                        SelectDialogItem item = (SelectDialogItem) msg.obj;
                        Pointer._lbl_repeat.setText(item.Text.toUpperCase());
                    }
                    return true;
                }
            }));
            SelectBox.setData(Pointer._vw_repeat, new SelectDialogItem("0", ""));
        }

        if(Pointer._lbl_start_time != null) {
            Pointer._lbl_start_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                            Pointer.setTime(Pointer._lbl_start_time, hourOfDay, minute);
                        }
                    }, 8,0,0, false);
                    timePickerDialog.setAccentColor("#FF4081");
                    timePickerDialog.setVersion(TimePickerDialog.Version.VERSION_1);
                    timePickerDialog.show(getFragmentManager(), "TimePickerDialog");
                }
            });
        }

        if(Pointer._lbl_end_time != null) {
            Pointer._lbl_end_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                            Pointer.setTime(Pointer._lbl_end_time, hourOfDay, minute);
                        }
                    }, 8,0,0, false);
                    timePickerDialog.setAccentColor("#FF4081");
                    timePickerDialog.setVersion(TimePickerDialog.Version.VERSION_1);
                    timePickerDialog.show(getFragmentManager(), "TimePickerDialog");
                }
            });
        }

        /*----- Button Events -----*/
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_save:
                        Pointer.handleSave();
                        break;
                    case R.id.btn_cancel:
                        Pointer.handleCancel();
                        break;
                    case R.id.btn_delete:
                        Pointer.handleDelete();
                        break;
                }
            }
        };
        Pointer._btn_save = this.findViewById(R.id.btn_save);
        if(Pointer._btn_save != null) {
            Pointer._btn_save.setOnClickListener(onClickListener);
        }
        Pointer._btn_cancel = this.findViewById(R.id.btn_cancel);
        if(Pointer._btn_cancel != null) {
            Pointer._btn_cancel.setOnClickListener(onClickListener);
        }
        Pointer._btn_delete = this.findViewById(R.id.btn_delete);
        if(Pointer._btn_delete != null) {
            Pointer._btn_delete.setOnClickListener(onClickListener);
        }

        /*----- Loading selected args from intent -----*/
        Intent intent = this.getIntent();
        if(intent != null) {
            if(intent.hasExtra("DATE")) {
                Pointer._date = new Date(intent.getLongExtra("DATE", 0));
            }
            if(intent.hasExtra("ID")) {
                Pointer._reminderID = intent.getStringExtra("ID");
            }
        }
        if(Pointer._btn_delete != null) {
            Pointer._btn_delete.setVisibility(TextUtils.isEmpty(Pointer._reminderID) ? View.INVISIBLE : View.VISIBLE);
        }

        /*----- Setting control values -----*/
        if(Pointer._txt_email_to != null && TextUtils.isEmpty(Pointer._reminderID)) {
            Pointer._txt_email_to.setText(Shared.getString(Pointer, Shared.KEY_EMAIL_ID));
        }
        if(Pointer._lbl_start_time != null) {
            Pointer.setTime(Pointer._lbl_start_time, 8, 0);
        }
        if(Pointer._lbl_end_time != null) {
            Pointer.setTime(Pointer._lbl_end_time, 8, 30);
        }
        this.handleReminderLoading();
    }
    private void loadSelectDialogItems() {
        Pointer._remindIntervals = new ArrayList<>();
        Pointer._remindIntervals.add(new SelectDialogItem("5", "5 Minutes"));
        Pointer._remindIntervals.add(new SelectDialogItem("10", "10 Minutes"));
        Pointer._remindIntervals.add(new SelectDialogItem("15", "15 Minutes"));
        Pointer._remindIntervals.add(new SelectDialogItem("30", "30 Minutes"));
        Pointer._remindIntervals.add(new SelectDialogItem("45", "45 Minutes"));
        Pointer._remindIntervals.add(new SelectDialogItem("60", "1 Hour"));
        Pointer._remindIntervals.add(new SelectDialogItem("1440", "1 Day"));

        Pointer._reminder_type.add(new SelectDialogItem("remT1", "Doctor’s Appointment"));
        Pointer._reminder_type.add(new SelectDialogItem("remT2", "Receive Medication"));
        Pointer._reminder_type.add(new SelectDialogItem("remT3", "Appointment Reminder"));
        Pointer._reminder_type.add(new SelectDialogItem("remT4", "Log Event Reminder"));
        Pointer._reminder_type.add(new SelectDialogItem("remT5", "Check Personal Symptoms Reminder"));
        Pointer._reminder_type.add(new SelectDialogItem("remT6", "Other"));

        Pointer._repeatIntervals = new ArrayList<>();
        Pointer._repeatIntervals.add(new SelectDialogItem("0", "Never"));
        Pointer._repeatIntervals.add(new SelectDialogItem("1", "Every Day"));
        Pointer._repeatIntervals.add(new SelectDialogItem("7", "Every Week"));
        Pointer._repeatIntervals.add(new SelectDialogItem("14", "Every 2 Weeks"));
        Pointer._repeatIntervals.add(new SelectDialogItem("30", "Every Month"));
        Pointer._repeatIntervals.add(new SelectDialogItem("365", "Every Year"));
    }
    private void setTime(TextView view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Pointer._date);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        Date date = calendar.getTime();
        view.setTag(date);
        view.setText(Pointer._formatter.format(date));
    }
    private void handleReminderLoading() {
        if(TextUtils.isEmpty(Pointer._reminderID) == false) {
            ReminderReadArgs args = new ReminderReadArgs();
            args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
            args.Date = Common.WinAppDateFormat.format(Pointer._date);
            args.ReminderID = Common.replaceNull(Pointer._reminderID);
            /*----- Loading event details from server -----*/
            Loader.show(Pointer);
            Common.invokeAPI(Pointer, ServiceMethods.GetReminderData, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if (msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if (TextUtils.isEmpty(response.Json) == false) {
                                ReminderDataArgs data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ReminderDataArgs.class);
                                if (data != null && TextUtils.isEmpty(data.ReminderID) == false) {
                                    Pointer.loadReminderData(data);
                                    return true;
                                }
                            }
                            if (TextUtils.isEmpty(response.Error) == false) {
                                error = response.Error;
                            }
                        }
                    } catch (Exception ex) {

                    } finally {
                        Loader.hide();
                    }
                    /*----- Handling Error -----*/
                    if (TextUtils.isEmpty(error)) {
                        error = AppMsg.MSG_FAILED;
                    }
                    Notify.show(Pointer, error, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Pointer.onBackPressed();
                        }
                    });
                    return true;
                }
            }));
        }
    }
    public void loadReminderData(ReminderDataArgs data) {
        Pointer._reminderID = data.ReminderID;

        if(Pointer._tb_all_days != null) {
            Pointer._tb_all_days.setChecked(Common.replaceNull(data.AllDay,"").compareToIgnoreCase("Yes") == 0);
        }
        if(Pointer._txt_title != null) {
            Pointer._txt_title.setText(Common.replaceNull(data.ReminderTitle));
        }
        if(Pointer._txt_email_to != null) {
            Pointer._txt_email_to.setText(Common.replaceNull(data.EmailReminderTo));
        }
        if(Pointer._txt_reminder_text != null) {
            Pointer._txt_reminder_text.setText(Common.replaceNull(data.TextOfReminder));
        }
        if(Pointer._lbl_start_time != null) {
            Pointer.setTime(Pointer._lbl_start_time, Common.replaceNull(data.StartTime));
        }
        if(Pointer._lbl_end_time != null) {
            Pointer.setTime(Pointer._lbl_end_time, Common.replaceNull(data.EndTime));
        }
        if(Pointer._txt_location != null) {
            Pointer._txt_location.setText(Common.replaceNull(data.SetLocation));
        }
        if(Pointer._vw_alert_time != null) {
            SelectDialogItem item = Pointer.getSelectDialogItemByID(Pointer._remindIntervals, Integer.toString(data.RemindBefore));
            SelectBox.setData(Pointer._vw_alert_time, item);
            if(Pointer._lbl_alert_time != null) {
                Pointer._lbl_alert_time.setText("Remind " + item.Text + " Before");
            }
        }

        if(Pointer.reminder_type != null ) {
            //SelectBox.setData(Pointer._vw_alert_time, item);
            SelectDialogItem item = Pointer.getSelectDialogItemByID(Pointer._reminder_type, data.ReminderType);
            if(item.ID != null) {
                SelectDialogItem.desc="";

                System.out.println("getSelectDialogItemByID item.ID " + item.ID);
                SelectBox.setData(Pointer.reminder_type, item);
                if (Pointer.reminder_type != null) {
                    Pointer.reminder_type.setText(item.Text);
                }
            } else {
                item = Pointer.getSelectDialogItemByID(Pointer._reminder_type, "Other");
                SelectBox.setData(Pointer.reminder_type, item);
                SelectDialogItem.desc=data.ReminderType;
                Pointer.reminder_type.setText(Common.replaceNull(data.ReminderType));
            }
        }

        if(Pointer._vw_repeat != null) {
            SelectDialogItem item = Pointer.getSelectDialogItemByID(Pointer._repeatIntervals, data.Repeat);
            SelectBox.setData(Pointer._vw_repeat, item);
            if(Pointer._lbl_repeat != null) {
                Pointer._lbl_repeat.setText(item.Text.toUpperCase());
            }
        }
    }
    public void handleSave() {
        ReminderDataArgs args = new ReminderDataArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        args.ReminderDate = Common.WinAppDateFormat.format(Pointer._date);
        args.ReminderID = Common.replaceNull(Pointer._reminderID);
        if (Pointer._txt_title != null) {
            args.ReminderTitle = Pointer._txt_title.getText().toString();
        }
        if(Pointer._tb_all_days != null) {
            args.AllDay = Pointer._tb_all_days.isChecked() ? "Yes" : "No";
        }
        if (Pointer._lbl_start_time != null) {
            if(Pointer._lbl_start_time.getTag() != null && Pointer._lbl_start_time.getTag() instanceof Date) {
                args.StartTime = Pointer._jsonDateFormatter.format((Date) Pointer._lbl_start_time.getTag());
            }
        }
        if (Pointer._lbl_end_time != null) {
            if(Pointer._lbl_end_time.getTag() != null && Pointer._lbl_end_time.getTag() instanceof Date) {
                args.EndTime = Pointer._jsonDateFormatter.format((Date) Pointer._lbl_end_time.getTag());
            }
        }
        if (Pointer._txt_email_to != null) {
            args.EmailReminderTo = Pointer._txt_email_to.getText().toString();
        }
        if (Pointer._txt_reminder_text != null) {
            args.TextOfReminder = Pointer._txt_reminder_text.getText().toString();
        }
        if (Pointer._vw_alert_time != null) {
            SelectDialogItem item = SelectBox.getData(Pointer._vw_alert_time);
            if (item != null && TextUtils.isEmpty(item.ID) == false) {
                args.RemindBefore = Integer.parseInt(item.ID);
            }
        }
        if (Pointer.reminder_type != null) {
            if(SelectDialogItem.desc.length() == 0) {
                args.ReminderType = Common.replaceNull(SelectBox.getSelectBoxText(Pointer.reminder_type, ", "));
            } else {
                args.ReminderType = SelectDialogItem.desc;
            }

        }

        if (Pointer._txt_location != null) {
            args.SetLocation = Pointer._txt_location.getText().toString();
        }
        if (Pointer._vw_repeat != null) {
            SelectDialogItem item = SelectBox.getData(Pointer._vw_repeat);
            if (item != null) {
                args.Repeat = Common.replaceNull(item.ID);
            }
        }

        if (Pointer.isValidInputs(args)) {
            /*----- Saving reminder data -----*/
            Loader.show(Pointer);
            Common.invokeAPI(Pointer, ServiceMethods.SaveReminder, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if (msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if (TextUtils.isEmpty(response.Json) == false) {
                                ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                if (data != null && data.Result == 1) {
                                    if(MyFertilityActivity.Instance != null) {
                                        MyFertilityActivity.Instance.finish();
                                    }
                                    startActivity(new Intent(Pointer, MyFertilityActivity.class));
                                    Pointer.finish();
                                    return true;
                                }
                            }
                            if (TextUtils.isEmpty(response.Error) == false) {
                                error = response.Error;
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        Loader.hide();
                    }
                    /*----- Handling Error -----*/
                    if (TextUtils.isEmpty(error)) {
                        error = AppMsg.MSG_FAILED;
                    }
                    Notify.show(Pointer, error);
                    return true;
                }
            }));
        }
    }
    public void handleCancel() {
        Pointer.onBackPressed();
    }
    public void handleDelete() {
        Notify.show(Pointer, "Do you want to delete this reminder?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE) {
                    ReminderDeleteArgs args = new ReminderDeleteArgs();
                    args.ReminderID = Pointer._reminderID;

                    /*----- Deleting reminder data -----*/
                    Loader.show(Pointer);
                    Common.invokeAPI(Pointer, ServiceMethods.DeleteReminder, args, new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            String error = null;
                            try {
                                if (msg.obj != null && msg.obj instanceof ApiResult) {
                                    ApiResult response = (ApiResult) msg.obj;
                                    if (TextUtils.isEmpty(response.Json) == false) {
                                        ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                        if (data != null && data.Result == 1) {
                                            if(MyFertilityActivity.Instance != null) {
                                                MyFertilityActivity.Instance.finish();
                                            }
                                            startActivity(new Intent(Pointer, MyFertilityActivity.class));
                                            Pointer.finish();
                                            return true;
                                        }
                                    }
                                    if (TextUtils.isEmpty(response.Error) == false) {
                                        error = response.Error;
                                    }
                                }
                            } catch (Exception ex) {

                            } finally {
                                Loader.hide();
                            }
                            /*----- Handling Error -----*/
                            if (TextUtils.isEmpty(error)) {
                                error = AppMsg.MSG_FAILED;
                            }
                            Notify.show(Pointer, error);
                            return true;
                        }
                    }));
                }
            }
        }, "Yes", "No");
    }
    public void setTime(TextView view, String dateStr) {
        if(TextUtils.isEmpty(dateStr) == false) {
            Date date = Pointer._date;
            try {
                date = Pointer._jsonDateFormatter.parse(dateStr);
            } catch (Exception ex) {
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            Pointer.setTime(view, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }
    }

    public boolean isValidInputs(ReminderDataArgs data) {
        System.out.println("isValidInputs12233 "+data.ReminderType);
        if(TextUtils.isEmpty(data.ReminderTitle)) {
            Notify.show(Pointer, "Please enter reminder title.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_title.requestFocus();
                }
            });
            return false;
        } else if(TextUtils.isEmpty(data.ReminderType) || data.ReminderType.equals("Reminder Type")) {
            Notify.show(Pointer, "Please enter reminder type.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_title.requestFocus();
                }
            });
            return false;
        } else if(TextUtils.isEmpty(data.ReminderType) || data.ReminderType.equals("Other")) {
            Notify.show(Pointer, "Please enter description for reminder type.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_title.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(data.EmailReminderTo) == false && Common.isValidEmail(data.EmailReminderTo) == false) {
            Notify.show(Pointer, "Please enter valid email address.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_email_to.requestFocus();
                }
            });
            return false;
        }
        else if(((Date) Pointer._lbl_start_time.getTag()).after((Date) Pointer._lbl_end_time.getTag())) {
            Notify.show(Pointer, "End time can not be lesser than the start time.");
            return false;
        }
        else if(TextUtils.isEmpty(data.TextOfReminder)) {
            Notify.show(Pointer, "Please enter reminder text.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_reminder_text.requestFocus();
                }
            });
            return false;
        }
        return true;
    }

    public SelectDialogItem getSelectDialogItemByID(List<SelectDialogItem> items, String id) {
        SelectDialogItem selected = new SelectDialogItem();
        try {
            for(SelectDialogItem item : items) {
                if(item.ID.startsWith("rem")) {
                    if (item.Text.compareToIgnoreCase(id) == 0) {
                        selected = item;
                        break;
                    }
                } else {
                    if (item.ID.compareToIgnoreCase(id) == 0) {
                        selected = item;
                        break;
                    }
                }
            }
        }
        catch(Exception ex) {
        }

        return selected;
    }
}
