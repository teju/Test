package com.win.winfertility;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.ProfilePage2Data;
import com.win.winfertility.dto.SelectDialogItem;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.tools.WINFertilityService;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.SelectBox;
import com.win.winfertility.utils.Shared;

import java.util.ArrayList;
import java.util.List;

public class ProfilePage2Activity extends WINFertilityActivity {
    private ProfilePage2Activity Pointer;

    private EditText _txt_height;
    private EditText _txt_weight;
    private TextView _sel_thyroid;
    private TextView _sel_diabetes;
    private TextView _sel_tobacco_use;
    private TextView _sel_recreational_drugs;
    private TextView _sel_std;
    private TextView _sel_prev_pregnancy;
    private TextView _txt_medications;
    private Class<?> _nextActivityClass = HomeActivity.class;
    private boolean _isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_profile_page2);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        /*----- Finding Controls -----*/
        Pointer._txt_height = (EditText) this.findViewById(R.id.txt_height);
        Pointer._txt_weight = (EditText) this.findViewById(R.id.txt_weight);
        Pointer._sel_thyroid = (TextView) this.findViewById(R.id.sel_thyroid);
        Pointer._sel_diabetes = (TextView) this.findViewById(R.id.sel_diabetes);
        Pointer._sel_tobacco_use = (TextView) this.findViewById(R.id.sel_tobacco_use);
        Pointer._sel_recreational_drugs = (TextView) this.findViewById(R.id.sel_recreational_drugs);
        Pointer._sel_std = (TextView) this.findViewById(R.id.sel_std);
        Pointer._sel_prev_pregnancy = (TextView) this.findViewById(R.id.sel_prev_pregnancy);
        Pointer._txt_medications = (TextView) this.findViewById(R.id.txt_medications);

        /*----- Configuring select controls -----*/
        Pointer.initThyroidControl(Pointer._sel_thyroid);
        Pointer.initDiabetesControl(Pointer._sel_diabetes);
        Pointer.initTobaccoUseControl(Pointer._sel_tobacco_use);
        Pointer.initRecreationalDrugUseControl(Pointer._sel_recreational_drugs);
        Pointer.initSTDControl(Pointer._sel_std);
        Pointer.initPreviousPregnanciesControl(Pointer._sel_prev_pregnancy);

        /*----- Button event handlers -----*/
        View btn_next = this.findViewById(R.id.btn_next);
        if(btn_next != null) {
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfilePage2Data data = Pointer.getInputs();
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
                    Pointer.startActivity(new Intent(Pointer, ViewProfileActivity.class));
                }
            });
        }

        /*----- Get Data From Calling Intent -----*/
        Intent intent = Pointer.getIntent();
        if(intent != null && intent.hasExtra(Shared.EXTRA_IS_EDIT)) {
            Pointer._isEdit = intent.getBooleanExtra(Shared.EXTRA_IS_EDIT, false);
            if(Pointer._isEdit) {
                Pointer.loadProfileData();
            }
        }
    }
    private void initThyroidControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("1", "Yes"));
            items.add(new SelectDialogItem("2", "No"));
            SelectBox.config(view, items, "Thyroid");
        }
        catch(Exception ex) {
        }
    }
    private void initDiabetesControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("1", "Type |"));
            items.add(new SelectDialogItem("2", "Type ||"));
            items.add(new SelectDialogItem("3", "None"));
            SelectBox.config(view, items, "Diabetes");
        }
        catch(Exception ex) {
        }
    }
    private void initTobaccoUseControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("Cigarettes"));
            items.add(new SelectDialogItem("Cigars"));
            items.add(new SelectDialogItem("Smokeless/Chew"));
            items.add(new SelectDialogItem("Other"));
            items.add(new SelectDialogItem("None"));
            SelectBox.config(view, items, "Tobacco Use", true);
        }
        catch(Exception ex) {
        }
    }
    private void initRecreationalDrugUseControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("Marijuana/Cannabis"));
            items.add(new SelectDialogItem("Psychedelic Mushrooms"));
            items.add(new SelectDialogItem("Opioids"));
            items.add(new SelectDialogItem("LSD"));
            items.add(new SelectDialogItem("Barbiturates"));
            items.add(new SelectDialogItem("Amphetamines/Methamphetamines"));
            items.add(new SelectDialogItem("Ecstasy"));
            items.add(new SelectDialogItem("Cocaine/Crack Cocaine"));
            items.add(new SelectDialogItem("Heroin"));
            items.add(new SelectDialogItem("Other"));
            items.add(new SelectDialogItem("None"));
            SelectBox.config(view, items, "Recreational Drug Use", true);
        }
        catch(Exception ex) {
        }
    }
    private void initSTDControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("HPV (Human Papillomavirus)"));
            items.add(new SelectDialogItem("Chlamydia"));
            items.add(new SelectDialogItem("Gonorrhea"));
            items.add(new SelectDialogItem("Syphilis"));
            items.add(new SelectDialogItem("Herpes"));
            items.add(new SelectDialogItem("Trichomoniasis"));
            items.add(new SelectDialogItem("HIV/AIDS"));
            items.add(new SelectDialogItem("Other"));
            items.add(new SelectDialogItem("None"));
            SelectBox.config(view, items, "STD's", true);
        }
        catch(Exception ex) {
        }
    }
    private void initPreviousPregnanciesControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("No"));
            items.add(new SelectDialogItem("Yes – single birth"));
            items.add(new SelectDialogItem("Yes – twins, triplets, etc."));
            items.add(new SelectDialogItem("Miscarriage"));
            items.add(new SelectDialogItem("Terminated"));
            items.add(new SelectDialogItem("Other"));
            SelectBox.config(view, items, "Previous Pregnancies", true);
        }
        catch(Exception ex) {
        }
    }
    private void moveToNextScreen(ProfilePage2Data data) {
        /*----- Saving Profile Page-2 Data -----*/
        Shared.setString(Pointer, Shared.KEY_PROF_PAGE_2_INFO, new Gson().toJson(data));
        Shared.setInt(this, Shared.KEY_PROFILE_INFO_SAVED, 0);
        WINFertilityService.start(Pointer);

        if (this._isEdit) {
            if (ViewProfileActivity.Instance != null) {
                ViewProfileActivity.Instance.finish();
            }
            if (AccountOptionActivity.Instance != null) {
                AccountOptionActivity.Instance.finish();
            }
            if (AccountInfoActivity.Instance != null) {
                AccountInfoActivity.Instance.finish();
            }
            if (ProfilePage1Activity.Instance != null) {
                ProfilePage1Activity.Instance.finish();
            }
            Pointer.finish();
            Pointer.startActivity(new Intent(Pointer, ViewProfileActivity.class));
        } else {
            if (Pointer._nextActivityClass == MyFertilityActivity.class) {
                if (MyFertilityActivity.Instance != null) {
                    MyFertilityActivity.Instance.finish();
                }
            }
            Pointer.startActivity(new Intent(ProfilePage2Activity.this, Pointer._nextActivityClass));
        }
    }
    private ProfilePage2Data getInputs() {
        ProfilePage2Data data = new ProfilePage2Data();
        try {
            data.Height = Common.replaceNull(Pointer._txt_height.getText().toString());
            if(TextUtils.isEmpty(data.Height)) {
                data.Height = "0";
            }
            data.Weight = Common.replaceNull(Pointer._txt_weight.getText().toString());
            if(TextUtils.isEmpty(data.Weight)) {
                data.Weight = "0";
            }
            data.Thyroid = Common.replaceNull(SelectBox.getData(Pointer._sel_thyroid).Text);
            data.Diabetes = Common.replaceNull(SelectBox.getData(Pointer._sel_diabetes).Text);
            data.TobaccoUse = Common.replaceNull(SelectBox.getSelectBoxText(Pointer._sel_tobacco_use, ", "));
            data.RecreationalDrugUse = Common.replaceNull(SelectBox.getSelectBoxText(Pointer._sel_recreational_drugs, ", "));
            data.STD = Common.replaceNull(SelectBox.getSelectBoxText(Pointer._sel_std, ", "));
            data.PreviousPregnancy = Common.replaceNull(SelectBox.getSelectBoxText(Pointer._sel_prev_pregnancy, ", "));
            data.ListMedications = Common.replaceNull(Pointer._txt_medications.getText().toString());
        }
        catch(Exception ex) {
        }
        return data;
    }
    private void loadProfileData() {
        if(ProfilePage1Activity.SavedProfileData != null) {
            if (TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.Height) == false &&
                    (ProfilePage1Activity.SavedProfileData.Height.trim().compareTo("0") == 0 ||
                            ProfilePage1Activity.SavedProfileData.Height.trim().replace("0", "").compareTo(".") == 0)) {
                ProfilePage1Activity.SavedProfileData.Height = "";
            }
            if (TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.Weight) == false &&
                    (ProfilePage1Activity.SavedProfileData.Weight.trim().compareTo("0") == 0 ||
                            ProfilePage1Activity.SavedProfileData.Weight.trim().replace("0", "").compareTo(".") == 0)) {
                ProfilePage1Activity.SavedProfileData.Weight = "";
            }
            Pointer._txt_height.setText(ProfilePage1Activity.SavedProfileData.Height);
            Pointer._txt_weight.setText(ProfilePage1Activity.SavedProfileData.Weight);
            if (TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.Thyroid) == false) {
                SelectBox.setData(Pointer._sel_thyroid, new SelectDialogItem(ProfilePage1Activity.SavedProfileData.Thyroid));
            }
            if (TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.Diabetes) == false) {
                SelectBox.setData(Pointer._sel_diabetes, new SelectDialogItem(ProfilePage1Activity.SavedProfileData.Diabetes));
            }
            if (TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.TobaccoUse) == false) {
                SelectBox.setDataList(Pointer._sel_tobacco_use, ProfilePage1Activity.SavedProfileData.TobaccoUse.split(","));
            }
            if (TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.RecreationalDrugUse) == false) {
                SelectBox.setDataList(Pointer._sel_recreational_drugs, ProfilePage1Activity.SavedProfileData.RecreationalDrugUse.split(","));
            }
            if (TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.STD) == false) {
                SelectBox.setDataList(Pointer._sel_std, ProfilePage1Activity.SavedProfileData.STD.split(","));
            }
            if (TextUtils.isEmpty(ProfilePage1Activity.SavedProfileData.PreviousPregnancy) == false) {
                SelectBox.setDataList(Pointer._sel_prev_pregnancy, ProfilePage1Activity.SavedProfileData.PreviousPregnancy.split(","));
            }
            Pointer._txt_medications.setText(ProfilePage1Activity.SavedProfileData.ListMedications);
        }
    }
    private boolean isValidInputs(ProfilePage2Data data) {
        boolean isValid = true;
        try {
            Gson gson = new Gson();
            String actualJson = gson.toJson(data);
            String emptyJson = gson.toJson(new ProfilePage2Data());
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
