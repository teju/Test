package com.win.winfertility;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.BaseUserReqArgs;
import com.win.winfertility.dataobjects.ProfileDataArgs;
import com.win.winfertility.dataobjects.ProfilePage1Data;
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

public class ProfilePage1Activity extends WINFertilityActivity {
    public static ProfilePage1Activity Instance;
    private ProfilePage1Activity Pointer;

    private TextView _sel_relationship;
    private TextView _sel_sexual_orientation;
    private TextView _sel_sleep_hours;
    private TextView _sel_exercise;
    private TextView _sel_stress_level;
    private boolean _isEdit;

    public static ProfileDataArgs SavedProfileData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ProfilePage1Activity.Instance = this;
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_profile_page1);
        this.Pointer = this;
        this.init();
    }

    @Override
    public void onBackPressed() {
        Intent intent = Pointer.getIntent();
        if (intent != null) {
            String parentClass = intent.getStringExtra(Shared.EXTRA_PARENT_CLASS);
            if (TextUtils.isEmpty(parentClass) == false && parentClass.compareTo(HomeActivity.class.getName()) == 0) {
                super.onBackPressed();
                return;
            }
        }

        super.onBackPressed();

    }

    private void init() {
        /*----- Finding Controls -----*/
        Pointer._sel_relationship = (TextView) Pointer.findViewById(R.id.sel_relationship);
        Pointer._sel_sexual_orientation = (TextView) Pointer.findViewById(R.id.sel_sexual_orientation);
        Pointer._sel_sleep_hours = (TextView) Pointer.findViewById(R.id.sel_sleep_hours);
        Pointer._sel_exercise = (TextView) Pointer.findViewById(R.id.sel_exercise);
        Pointer._sel_stress_level = (TextView) Pointer.findViewById(R.id.sel_stress_level);

        /*----- Configuring select controls -----*/
        Pointer.initRelationshipStatusControl(Pointer._sel_relationship);
        Pointer.initSexualOrientationControl(Pointer._sel_sexual_orientation);
        Pointer.initSleepHoursControl(Pointer._sel_sleep_hours);
        Pointer.initExerciseControl(Pointer._sel_exercise);
        Pointer.initStressLevelControl(Pointer._sel_stress_level);

        /*----- Button event handlers -----*/
        View btn_next = this.findViewById(R.id.btn_next);
        if(btn_next != null) {
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfilePage1Data data = Pointer.getInputs();
                    if(Pointer.isValidInputs(data)) {
                        Pointer.moveToNextScreen(data);
                    }
                }
            });
        }
        View btn_skip_for_now = this.findViewById(R.id.btn_skip_for_now);
        if(btn_skip_for_now != null) {
            btn_skip_for_now.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfilePage1Activity.this, ProfilePage2Activity.class);
                    intent.putExtra(Shared.EXTRA_IS_EDIT, Pointer._isEdit);
                    Pointer.startActivity(intent);
                }
            });
        }

        /*----- Get Data From Calling Intent -----*/
        ProfilePage1Activity.SavedProfileData = null;
        Intent intent = Pointer.getIntent();
        if(intent != null && intent.hasExtra(Shared.EXTRA_IS_EDIT)) {
            Pointer._isEdit = intent.getBooleanExtra(Shared.EXTRA_IS_EDIT, false);
            if(Pointer._isEdit) {
                Pointer.readProfileData();
            }
        }
    }
    private void initRelationshipStatusControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("a man"));
            items.add(new SelectDialogItem("a woman"));
            items.add(new SelectDialogItem("more than one person"));
            items.add(new SelectDialogItem("I am not in a relationship"));
            SelectBox.config(view, items, "Relationship Status");
        }
        catch(Exception ex) {
        }
    }
    private void initSexualOrientationControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("Heterosexual"));
            items.add(new SelectDialogItem("Homosexual"));
            items.add(new SelectDialogItem("Bi-sexual"));
            SelectBox.config(view, items, "Sexual Orientation");
        }
        catch(Exception ex) {
        }
    }
    private void initSleepHoursControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("I sleep for less than 5 hours"));
            items.add(new SelectDialogItem("I sleep for 5-6 hours"));
            items.add(new SelectDialogItem("I sleep for 7-8 hours"));
            items.add(new SelectDialogItem("I sleep for 9-10 hours"));
            items.add(new SelectDialogItem("I sleep for more than 10 hours"));
            SelectBox.config(view, items, "Hours of sleep");
        }
        catch(Exception ex) {
        }
    }
    private void initExerciseControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("I exercise for less than 1 hours"));
            items.add(new SelectDialogItem("I exercise for 1-3 hours"));
            items.add(new SelectDialogItem("I exercise for 3-6 hours"));
            items.add(new SelectDialogItem("I exercise for 6-10 hours"));
            items.add(new SelectDialogItem("I exercise for more than 10 hours"));
            SelectBox.config(view, items, "Exercise per week");
        }
        catch(Exception ex) {
        }
    }
    private void initStressLevelControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("Life is good"));
            items.add(new SelectDialogItem("I’m doing great!"));
            items.add(new SelectDialogItem("Neither good nor bad"));
            items.add(new SelectDialogItem("It’s tough out there"));
            items.add(new SelectDialogItem("Barely getting through the day"));
            SelectBox.config(view, items, "General Stress Level");
        }
        catch(Exception ex) {
        }
    }
    private void moveToNextScreen(ProfilePage1Data data) {
        /*----- Saving Profile Page-1 Data -----*/
        Shared.setString(Pointer, Shared.KEY_PROF_PAGE_1_INFO, new Gson().toJson(data));
        Shared.setInt(this, Shared.KEY_PROFILE_INFO_SAVED, 0);
        WINFertilityService.start(Pointer);
        Intent intent = new Intent(ProfilePage1Activity.this, ProfilePage2Activity.class);
        intent.putExtra(Shared.EXTRA_IS_EDIT, Pointer._isEdit);
        Pointer.startActivity(intent);
    }
    private void readProfileData() {
        Loader.show(Pointer);
        BaseUserReqArgs args = new BaseUserReqArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        Common.invokeAPI(Pointer, ServiceMethods.LoadFertilityProfileData, args, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                String error = "";
                try {
                    if (msg != null && msg.obj instanceof ApiResult) {
                        ApiResult data = (ApiResult) msg.obj;
                        if (data != null) {
                            if (TextUtils.isEmpty(data.Json) == false) {
                                ProfilePage1Activity.SavedProfileData = new Gson().fromJson(data.Json, ProfileDataArgs.class);
                                Pointer.loadProfileData();
                                return true;
                            }
                            error = data.Error;
                        }
                    }
                } catch(Exception ex) {
                }
                finally {
                    Loader.hide();
                }
                if(TextUtils.isEmpty(error)) {
                    error = AppMsg.MSG_FAILED;
                }
                Notify.show(Pointer, error);
                return false;
            }
        }));
    }
    private void loadProfileData() {
        if(ProfilePage1Activity.SavedProfileData != null) {
            if(TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.RelationshipStatus) == false) {
                SelectBox.setData(Pointer._sel_relationship, new SelectDialogItem(ProfilePage1Activity.SavedProfileData.RelationshipStatus));
            }
            if(TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.SexualOrientation) == false) {
                SelectBox.setData(Pointer._sel_sexual_orientation, new SelectDialogItem(ProfilePage1Activity.SavedProfileData.SexualOrientation));
            }
            if(TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.NumberOfHoursOfSleepPerNight) == false) {
                SelectBox.setData(Pointer._sel_sleep_hours, new SelectDialogItem(ProfilePage1Activity.SavedProfileData.NumberOfHoursOfSleepPerNight));
            }
            if(TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.Exercise) == false) {
                SelectBox.setData(Pointer._sel_exercise, new SelectDialogItem(ProfilePage1Activity.SavedProfileData.Exercise));
            }
            if(TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.GeneralStressLevel) == false) {
                SelectBox.setData(Pointer._sel_stress_level, new SelectDialogItem(ProfilePage1Activity.SavedProfileData.GeneralStressLevel));
            }
        }
    }
    private ProfilePage1Data getInputs() {
        ProfilePage1Data data = new ProfilePage1Data();
        try {
            data.RelationshipStatus = Common.replaceNull(SelectBox.getData(Pointer._sel_relationship).Text);
            data.SexualOrientation = Common.replaceNull(SelectBox.getData(Pointer._sel_sexual_orientation).Text);
            data.NumberOfHoursOfSleepPerNight = Common.replaceNull(SelectBox.getData(Pointer._sel_sleep_hours).Text);
            data.Exercise = Common.replaceNull(SelectBox.getData(Pointer._sel_exercise).Text);
            data.GeneralStressLevel = Common.replaceNull(SelectBox.getData(Pointer._sel_stress_level).Text);
        }
        catch(Exception ex) {
        }
        return data;
    }
    private boolean isValidInputs(ProfilePage1Data data) {
        boolean isValid = true;
        try {
            Gson gson = new Gson();
            String actualJson = gson.toJson(data);
            String emptyJson = gson.toJson(new ProfilePage1Data());
            if(actualJson.compareTo(emptyJson) == 0) {
                Notify.show(Pointer, AppMsg.MSG_EMPTY_PROFILE_INFO);
                return false;
            }
        }
        catch(Exception ex) {
        }
        return isValid;
    }
}
