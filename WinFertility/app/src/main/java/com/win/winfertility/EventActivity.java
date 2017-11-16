package com.win.winfertility;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.EventDataArgs;
import com.win.winfertility.dataobjects.EventReadArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.tools.WinAccordion;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventActivity extends WINFertilityActivity {
    private EventActivity Pointer;

    private WinAccordion _vw_sex_type;
    private WinAccordion _vw_flow_type;
    private WinAccordion _vw_sex_drive;
    private WinAccordion _vw_symptoms;
    private WinAccordion _vw_feelings;
    private WinAccordion _vw_medications;
    private WinAccordion _vw_ovulation_tests;
    private WinAccordion _vw_pregnancy_tests;
    private WinAccordion _vw_notes;
    private TextView _lbl_day;

    private Date _date = new Date();
    private String _eventID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_event);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        Pointer._vw_sex_type = (WinAccordion) this.findViewById(R.id.vw_sex_type);
        if(Pointer._vw_sex_type != null) {
            Pointer._vw_sex_type.setEditable(false);
            Pointer._vw_sex_type.setItems(new String[]{"No", "Protected", "Unprotected", "Just Me"});
        }

        Pointer._vw_flow_type = (WinAccordion) this.findViewById(R.id.vw_flow_type);
        if(Pointer._vw_flow_type != null) {
            Pointer._vw_flow_type.setEditable(false);
            Pointer._vw_flow_type.setItems(new String[]{"None", "Spotty", "Light", "Medium", "Heavy", "1st day"});
        }

        Pointer._vw_sex_drive = (WinAccordion) this.findViewById(R.id.vw_sex_drive);
        if(Pointer._vw_sex_drive != null) {
            Pointer._vw_sex_drive.setEditable(false);
            Pointer._vw_sex_drive.setItems(new String[]{"Need some alone time", "Lead the way", "I’m ready"});
        }

        Pointer._vw_symptoms = (WinAccordion) this.findViewById(R.id.vw_symptoms);
        if(Pointer._vw_symptoms != null) {
            Pointer._vw_symptoms.setEditable(false);
            Pointer._vw_symptoms.setAllowMultiSelect(true);
            Pointer._vw_symptoms.setItems(new String[]{"Ah Ok", "Cramps", "Tender Breasts", "Headache"});
        }

        Pointer._vw_feelings = (WinAccordion) this.findViewById(R.id.vw_feelings);
        if(Pointer._vw_feelings != null) {
            Pointer._vw_feelings.setEditable(false);
            Pointer._vw_feelings.setItems(new String[]{"Happy", "Emotional", "Stressed", "Tired", "Moody"});
        }

        Pointer._vw_medications = (WinAccordion) this.findViewById(R.id.vw_medications);
        if(Pointer._vw_medications != null) {
            Pointer._vw_medications.setEditable(true);
            Pointer._vw_medications.setWaterMark("Medication Taken");
        }
        Pointer._vw_ovulation_tests = (WinAccordion) this.findViewById(R.id.vw_ovulation_tests);
        if(Pointer._vw_ovulation_tests != null) {
            Pointer._vw_ovulation_tests.setEditable(true);
            Pointer._vw_ovulation_tests.setWaterMark("Please Enter Brand and Result");
        }
        Pointer._vw_pregnancy_tests = (WinAccordion) this.findViewById(R.id.vw_pregnancy_tests);
        if(Pointer._vw_pregnancy_tests != null) {
            Pointer._vw_pregnancy_tests.setEditable(true);
            Pointer._vw_pregnancy_tests.setWaterMark("Please Enter Brand and Result");
        }
        Pointer._vw_notes = (WinAccordion) this.findViewById(R.id.vw_notes);
        if(Pointer._vw_notes != null) {
            Pointer._vw_notes.setEditable(true);
            Pointer._vw_notes.setWaterMark("Notes");
        }

        /*----- Handling button events -----*/
        View btn_save = Pointer.findViewById(R.id.btn_save);
        if(btn_save != null) {
            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.handleSave();
                }
            });
        }

        /*----- Loading selected args from intent -----*/
        Intent intent = this.getIntent();
        if(intent != null) {
            if(intent.hasExtra("DATE")) {
                Pointer._date = new Date(intent.getLongExtra("DATE", 0));
            }
            if(intent.hasExtra("ID")) {
                Pointer._eventID = intent.getStringExtra("ID");
            }
        }
        Pointer.handleEventLoading();

        Pointer._lbl_day = (TextView) this.findViewById(R.id.lbl_day);
        /*----- Loading selected date -----*/
        if(Pointer._lbl_day != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd");
            Pointer._lbl_day.setText(formatter.format(Pointer._date).toUpperCase());
        }
    }
    private void handleEventLoading() {
        if(TextUtils.isEmpty(Pointer._eventID) == false) {
            EventReadArgs args = new EventReadArgs();
            args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
            args.Date = Common.WinAppDateFormat.format(Pointer._date);
            args.EventID = Common.replaceNull(Pointer._eventID);
            /*----- Loading event details from server -----*/
            Loader.show(Pointer);
            Common.invokeAPI(Pointer, ServiceMethods.GetEventData, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if (msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if (TextUtils.isEmpty(response.Json) == false) {
                                EventDataArgs data = new Gson().fromJson(Common.suppressJsonArray(response.Json), EventDataArgs.class);
                                if (data != null && TextUtils.isEmpty(data.EventID) == false) {
                                    Pointer.loadEventData(data);
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
    private void loadEventData(EventDataArgs data) {
        Pointer._eventID = data.EventID;

        if (Pointer._vw_flow_type != null) {
            if(TextUtils.isEmpty(data.Period)) { data.Period = "";}
            Pointer._vw_flow_type.setSelectedItems(data.Period.split(","));
        }
        if (Pointer._vw_sex_type != null) {
            if(TextUtils.isEmpty(data.SexualActivity)) { data.SexualActivity = "";}
            Pointer._vw_sex_type.setSelectedItems(data.SexualActivity.split(","));
        }
        if (Pointer._vw_sex_drive != null) {
            if(TextUtils.isEmpty(data.SexDrive)) { data.SexDrive = "";}
            Pointer._vw_sex_drive.setSelectedItems(data.SexDrive.split(","));
        }
        if (Pointer._vw_symptoms != null) {
            if(TextUtils.isEmpty(data.PersonalSymptoms)) { data.PersonalSymptoms = "";}
            Pointer._vw_symptoms.setSelectedItems(data.PersonalSymptoms.split(","));
        }
        if (Pointer._vw_feelings != null) {
            if(TextUtils.isEmpty(data.Mood)) { data.Mood = "";}
            Pointer._vw_feelings.setSelectedItems(data.Mood.split(","));
        }
        if (Pointer._vw_ovulation_tests != null) {
            if(TextUtils.isEmpty(data.OvulationTests)) { data.OvulationTests = "";}
            Pointer._vw_ovulation_tests.setRemark(data.OvulationTests);
        }
        if (Pointer._vw_pregnancy_tests != null) {
            if(TextUtils.isEmpty(data.PregnancyTests)) { data.PregnancyTests = "";}
            Pointer._vw_pregnancy_tests.setRemark(data.PregnancyTests);
        }
        if (Pointer._vw_medications != null) {
            if(TextUtils.isEmpty(data.Medications)) { data.Medications = "";}
            Pointer._vw_medications.setRemark(data.Medications);
        }
        if (Pointer._vw_notes != null) {
            if(TextUtils.isEmpty(data.Notes)) { data.Notes = "";}
            Pointer._vw_notes.setRemark(data.Notes);
        }
    }
    private void handleSave() {
        EventDataArgs args = new EventDataArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        args.EventDate = Common.WinAppDateFormat.format(Pointer._date);
        if(Pointer._vw_flow_type != null)
        { args.Period = Common.join(Pointer._vw_flow_type.getSelectedItems(), ","); }
        if(Pointer._vw_sex_type != null)
        { args.SexualActivity = Common.join(Pointer._vw_sex_type.getSelectedItems(), ","); }
        if(Pointer._vw_sex_drive != null)
        { args.SexDrive = Common.join(Pointer._vw_sex_drive.getSelectedItems(), ","); }
        if(Pointer._vw_symptoms != null)
        { args.PersonalSymptoms = Common.join(Pointer._vw_symptoms.getSelectedItems(), ","); }
        if(Pointer._vw_feelings != null)
        { args.Mood = Common.join(Pointer._vw_feelings.getSelectedItems(), ","); }
        if(Pointer._vw_ovulation_tests != null)
        { args.OvulationTests = Pointer._vw_ovulation_tests.getRemark(); }
        if(Pointer._vw_pregnancy_tests != null)
        { args.PregnancyTests = Pointer._vw_pregnancy_tests.getRemark(); }
        if(Pointer._vw_medications != null)
        { args.Medications = Pointer._vw_medications.getRemark(); }
        if(Pointer._vw_notes != null)
        { args.Notes = Pointer._vw_notes.getRemark(); }

        Loader.show(Pointer);
        Common.invokeAPI(Pointer, ServiceMethods.SaveEvent, args, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String error = null;
                try {
                    if(msg.obj != null && msg.obj instanceof ApiResult) {
                        ApiResult response = (ApiResult) msg.obj;
                        if(TextUtils.isEmpty(response.Json) == false) {
                            ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                            if(data != null && data.Result == 1) {
                                if(MyFertilityActivity.Instance != null) {
                                    MyFertilityActivity.Instance.finish();
                                }
                                startActivity(new Intent(Pointer, MyFertilityActivity.class));
                                Pointer.finish();
                                return true;
                            }
                        }
                        if(TextUtils.isEmpty(response.Error) == false) {
                            error = response.Error;
                        }
                    }
                }
                catch (Exception ex) {
                }
                finally {
                    Loader.hide();
                }
                /*----- Handling Error -----*/
                if(TextUtils.isEmpty(error)) {
                    error = AppMsg.MSG_FAILED;
                }
                Notify.show(Pointer, error);
                return true;
            }
        }));
    }
    private void handleCancel() {
        Pointer.onBackPressed();
    }
}
