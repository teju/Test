package com.win.winfertility;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.MenstrualArgs;
import com.win.winfertility.dataobjects.MenstrualReadArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.dto.SelectDialogItem;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.tools.WINFertilityService;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.SelectBox;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

import java.util.ArrayList;
import java.util.List;

public class MenstrualInfoActivity extends WINFertilityActivity {
    private MenstrualInfoActivity Pointer;

    private TextView _sel_months;
    private TextView _sel_days;
    private TextView _sel_period_days;
    private TextView _sel_cycle_days;
    private List<SelectDialogItem> _months;
    private Class<?> _nextActivityClass = HomeActivity.class;
    private boolean _isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_menstrual_info);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        Pointer._months = Pointer.getMonths();

        /*----- Fetching controls from view -----*/
        Pointer._sel_months = (TextView) this.findViewById(R.id.sel_months);
        Pointer._sel_days = (TextView) this.findViewById(R.id.sel_days);
        Pointer._sel_period_days = (TextView) this.findViewById(R.id.sel_period_days);
        Pointer._sel_cycle_days = (TextView) this.findViewById(R.id.sel_cycle_days);

        /*----- Handling select controls -----*/
        Pointer.initMonthControl(Pointer._sel_months);
        Pointer.initDayControl(Pointer._sel_days);
        Pointer.initNumberSelectControl(Pointer._sel_period_days, "Number of days", 1, 10);
        Pointer.initNumberSelectControl(Pointer._sel_cycle_days, "Number of days", 18, 90);

        /*----- Attaching control events -----*/
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText control = (EditText) view;
                if(control != null) {
                    Pointer.setValidText(control, hasFocus);
                }
            }
        };
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Pointer.setValidText((EditText) view, true);
                return false;
            }
        };

        Button btn_next = (Button) this.findViewById(R.id.btn_next);
        if(btn_next != null) {
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final MenstrualArgs data = Pointer.getInputs();
                    Pointer.validateAndProceedNext(data, new Runnable() {
                        @Override
                        public void run() {
                            Pointer.moveToNextScreen(data);
                        }
                    });
                }
            });
        }
        View btn_skip_for_now = this.findViewById(R.id.btn_skip_for_now);
        if(btn_skip_for_now != null) {
            btn_skip_for_now.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.validateAndProceedSkip(new Runnable() {
                        @Override
                        public void run() {
                            Pointer.moveToNextScreen(new MenstrualArgs());
                        }
                    });
                }
            });
        }

        /*----- Load saved data to controls -----*/
        Pointer.loadMenstrualData();

        /*----- Handling menstrual update link -----*/
        Intent intent = this.getIntent();
        if(intent != null && intent.hasExtra(Shared.EXTRA_PARENT_CLASS)) {
            String parentClass = Common.replaceNull(intent.getStringExtra(Shared.EXTRA_PARENT_CLASS));
            if(TextUtils.isEmpty(parentClass) == false) {
                if(parentClass.trim().compareToIgnoreCase(MyFertilityActivity.class.getName()) == 0) {
                    Pointer._nextActivityClass = MyFertilityActivity.class;
                }

                Pointer._isEdit = true;
                btn_next.setText("Update");
                btn_skip_for_now.setVisibility(View.GONE);
            }
        }
    }
    private void loadMenstrualData() {
        String email = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        if(TextUtils.isEmpty(email) == false) {
            final Handler handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if(msg.obj != null && msg.obj instanceof MenstrualArgs) {
                        Pointer.loadDataToControls((MenstrualArgs) msg.obj);
                    }
                    Loader.hide();
                    return true;
                }
            });
            Loader.show(Pointer);
            if (Shared.getInt(Pointer, Shared.KEY_MENSTRUAL_INFO_SAVED) == 1) {
                MenstrualReadArgs args = new MenstrualReadArgs();
                args.EmailID = email;
                Common.invokeAPI(Pointer, ServiceMethods.GetMenstrualData, args, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        MenstrualArgs data = null;
                        if(msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult result = (ApiResult) msg.obj;
                            if(result != null && TextUtils.isEmpty(result.Json) == false) {
                                data = new Gson().fromJson(result.Json, MenstrualArgs.class);
                            }
                        }
                        if(data == null) {
                            data = new MenstrualArgs();
                        }
                        if(handler != null) {
                            handler.sendMessage(handler.obtainMessage(0, data));
                        }
                        return true;
                    }
                }));
            } else {
                MenstrualArgs data = new MenstrualArgs();
                String json = Shared.getString(Pointer, Shared.KEY_MENSTRUAL_INFO);
                if (TextUtils.isEmpty(json) == false) {
                    try {
                        data = new Gson().fromJson(json, MenstrualArgs.class);
                    } catch (Exception ex) {
                    }
                }
                if (data == null) {
                    data = new MenstrualArgs();
                }
                handler.sendMessage(handler.obtainMessage(0, data));
            }
        }
    }
    private void loadDataToControls(MenstrualArgs data) {
        /*----- Loading last menstrual month -----*/
        data.Month = Common.replaceNull(data.Month);
        if (data.Month.trim().replace("0", "").length() == 0) {
            data.Month = "";
        } else if (data.Month.length() == 1) {
            data.Month = "0" + data.Month;
        }
        SelectDialogItem selected = new SelectDialogItem(data.Month, "");
        if (Pointer._months != null) {
            for (SelectDialogItem item : Pointer._months) {
                if (Common.replaceNull(item.ID).compareTo(data.Month) == 0) {
                    selected = item;
                    break;
                }
            }
        }
        SelectBox.setData(Pointer._sel_months, selected);
        Pointer.initDayControl(Pointer._sel_days);
        /*----- Loading last menstrual day -----*/
        data.Day = Common.replaceNull(data.Day);
        if (data.Day.trim().replace("0", "").length() == 0) {
            data.Day = "";
        } else if (data.Day.length() == 1) {
            data.Day = "0" + data.Day;
        }
        SelectBox.setData(Pointer._sel_days, new SelectDialogItem(data.Day, data.Day));
        String displayText = data.LastPeriodDays;
        if (displayText.compareToIgnoreCase("1") == 0) {
            displayText += " day";
        } else if (displayText.compareToIgnoreCase("0") != 0) {
            displayText += " days";
        }
        else {
            displayText = "";
        }
        if(TextUtils.isEmpty(displayText) == false) {
            SelectBox.setData(Pointer._sel_period_days, new SelectDialogItem(data.LastPeriodDays, displayText));
        }
        displayText = data.CycleDays;
        if (displayText.compareToIgnoreCase("1") == 0) {
            displayText += " day";
        } else if (displayText.compareToIgnoreCase("0") != 0) {
            displayText += " days";
        }
        else {
            displayText = "";
        }
        if(TextUtils.isEmpty(displayText) == false) {
            SelectBox.setData(Pointer._sel_cycle_days, new SelectDialogItem(data.CycleDays, displayText));
        }
    }
    private void initMonthControl(TextView view) {
        try {
            SelectBox.config(view, Pointer._months, "Month", false, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    SelectBox.setData(Pointer._sel_days, new SelectDialogItem());
                    Pointer.initDayControl(Pointer._sel_days);
                    return true;
                }
            }));
        }
        catch(Exception ex) {
        }
    }
    private void initNumberSelectControl(TextView view, String title, int start, int end) {
        List<SelectDialogItem> items = new ArrayList<>();
        for(int i = start; i <= end; i++) {
            String text = Integer.toString(i);
            items.add(new SelectDialogItem(text, text + (i == 1 ? " day" : " days")));
        }
        SelectBox.config(view, items, title);
    }
    private void initDayControl(TextView view) {
        try {
            String selectedMonth = SelectBox.getData(Pointer._sel_months).ID;
            int days = 31;
            if(TextUtils.isEmpty(selectedMonth) == false) {
                try {
                    days = Common.daysInMonth(Integer.parseInt(selectedMonth));
                } catch (Exception ex) {
                }
            }
            List<SelectDialogItem> items = new ArrayList<>();
            for(int i = 1; i<= days; i++) {
                String text = Integer.toString(i);
                if(text.length() == 1) {
                    text = "0" + text;
                }
                items.add(new SelectDialogItem(text, text));
            }
            SelectBox.config(view, items, "Day");
        }
        catch(Exception ex) {
        }
    }
    private void validateAndProceedNext(MenstrualArgs data, final Runnable runnable) {
        try {
            Gson gson = new Gson();
            String actualJson = gson.toJson(data);
            String emptyJson = gson.toJson(new MenstrualArgs());
            if (actualJson.compareTo(emptyJson) == 0) {
                Notify.show(Pointer, AppMsg.MSG_EMPTY_MENSTRUAL_INFO);
                return;
            }

            if (TextUtils.isEmpty(data.Month) ||
                    TextUtils.isEmpty(data.Day) ||
                    TextUtils.isEmpty(data.LastPeriodDays) ||
                    data.LastPeriodDays.replace("0","").length() == 0 ||
                    TextUtils.isEmpty(data.CycleDays) ||
                    data.CycleDays.replace("0","").length() == 0) {
                Notify.show(Pointer,
                        "If all requested information is not entered then we will not be able to properly track your menstrual cycle or be able to suggest your peak ovulation time.\n\nAre you sure you want to proceed?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == DialogInterface.BUTTON_POSITIVE) {
                                    if(runnable != null) {
                                        runnable.run();
                                    }
                                }
                            }
                        }, "Proceed", null, true);
                return;
            }
        }
        catch(Exception ex) {
        }

        /*----- Running the runnable if validation is success -----*/
        if(runnable != null) {
            runnable.run();
        }
    }
    private void validateAndProceedSkip(Runnable runnable) {
        /*----- Running the runnable if validation is success -----*/
        if(runnable != null) {
            runnable.run();
        }
    }
    private void moveToNextScreen(MenstrualArgs data) {
        data.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        if(TextUtils.isEmpty(data.EmailID) == false || Pointer._isEdit == false) {
            Shared.setString(Pointer, Shared.KEY_MENSTRUAL_INFO, new Gson().toJson(data));
            Shared.setInt(this, Shared.KEY_MENSTRUAL_INFO_SAVED, 0);
            WINFertilityService.start(Pointer);
        }
        if(Pointer._nextActivityClass != null) {
            if(Pointer._nextActivityClass == MyFertilityActivity.class) {
                if(MyFertilityActivity.Instance != null) {
                    MyFertilityActivity.Instance.finish();
                }
                Pointer.finish();
            }
            else if(Pointer._isEdit) {
                Pointer.startActivity(new Intent(MenstrualInfoActivity.this,MyFertilityActivity.class));
                Pointer.finish();
                return;
            }
            Pointer.startActivity(new Intent(MenstrualInfoActivity.this, Pointer._nextActivityClass));
        }
        else {
            super.onBackPressed();
        }
    }
    private void setValidText(EditText control, boolean hasFocus) {
        if(control != null) {
            String text = control.getText().toString();
            if(TextUtils.isEmpty(text) == false) {
                int textInt = Integer.parseInt(Common.filterOnlyDigits(text));
                text = Integer.toString(textInt);
                if (hasFocus == false) {
                    control.setText(textInt == 0 ? "" : (textInt == 1 ? "1 day" : text + " days"));
                } else {
                    control.setText(text);
                    control.selectAll();
                }
            }
        }
    }
    private MenstrualArgs getInputs() {
        MenstrualArgs data = new MenstrualArgs();
        try {
            data.Month = Common.replaceNull(SelectBox.getData(Pointer._sel_months).ID);
            if(TextUtils.isEmpty(data.Month)) {
                data.Month = "0";
            }
            data.Day = Common.replaceNull(SelectBox.getData(Pointer._sel_days).ID);
            if(TextUtils.isEmpty(data.Day)) {
                data.Day = "0";
            }
            data.LastPeriodDays = Common.replaceNull(Common.filterOnlyDigits(Pointer._sel_period_days.getText().toString()));
            if(TextUtils.isEmpty(data.LastPeriodDays)) {
                data.LastPeriodDays = "0";
            }
            data.CycleDays = Common.replaceNull(Common.filterOnlyDigits(Pointer._sel_cycle_days.getText().toString()));
            if(TextUtils.isEmpty(data.CycleDays)) {
                data.CycleDays = "0";
            }
        }
        catch(Exception ex) {
        }
        return data;
    }
    private List<SelectDialogItem> getMonths() {
        List<SelectDialogItem> items = new ArrayList<>();
        items.add(new SelectDialogItem("01", "January"));
        items.add(new SelectDialogItem("02", "February"));
        items.add(new SelectDialogItem("03", "March"));
        items.add(new SelectDialogItem("04", "April"));
        items.add(new SelectDialogItem("05", "May"));
        items.add(new SelectDialogItem("06", "June"));
        items.add(new SelectDialogItem("07", "July"));
        items.add(new SelectDialogItem("08", "August"));
        items.add(new SelectDialogItem("09", "September"));
        items.add(new SelectDialogItem("10", "October"));
        items.add(new SelectDialogItem("11", "November"));
        items.add(new SelectDialogItem("12", "December"));
        return items;
    }
}
