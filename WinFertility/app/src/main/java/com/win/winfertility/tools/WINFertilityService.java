package com.win.winfertility.tools;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.MenstrualArgs;
import com.win.winfertility.dataobjects.ProfileDataArgs;
import com.win.winfertility.dataobjects.ProfilePage1Data;
import com.win.winfertility.dataobjects.ProfilePage2Data;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

public class WINFertilityService extends IntentService {
    public WINFertilityService() {
        super("WINFertilityService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String email = Common.replaceNull(Shared.getString(this, Shared.KEY_EMAIL_ID));
            if(TextUtils.isEmpty(email) == false) {
                /*----- Saving Profile Data -----*/
                this.syncProfileDataToServer(email);
                /*----- Saving Menstrual Data -----*/
                this.syncMenstrualDataToServer(email);
            }
        } catch (Exception ex) {
        }
    }
    public static void start(Context context) {
        try {
            Intent intent = new Intent(context, WINFertilityService.class);
            context.startService(intent);
        }
        catch(Exception ex) {
        }
    }

    private void syncMenstrualDataToServer(String email) {
        if (Shared.getInt(this, Shared.KEY_MENSTRUAL_INFO_SAVED) == 0) {
            String menstrualData = Shared.getString(this, Shared.KEY_MENSTRUAL_INFO);
            Gson gson = null;

            if (TextUtils.isEmpty(menstrualData) == false) {
                gson = new Gson();
                MenstrualArgs data = gson.fromJson(menstrualData, MenstrualArgs.class);
                if(data != null) {
                    data.EmailID = email;

                    ApiResult response = Common.invokeAPIEx(this, ServiceMethods.SaveCycle, data);
                    if (response != null && TextUtils.isEmpty(response.Json) == false) {
                        ApiReqResult result = gson.fromJson(response.Json, ApiReqResult.class);
                        if (result != null && result.Result == 1) {
                            Shared.setInt(this, Shared.KEY_MENSTRUAL_INFO_SAVED, 1);
                        }
                    }
                }
            }
        }
    }
    private void syncProfileDataToServer(String email) {
        if (Shared.getInt(this, Shared.KEY_PROFILE_INFO_SAVED) == 0) {
            String profilePage1Data = Shared.getString(this, Shared.KEY_PROF_PAGE_1_INFO);
            String profilePage2Data = Shared.getString(this, Shared.KEY_PROF_PAGE_2_INFO);
            ProfileDataArgs data = null;
            Gson gson = null;

            /*----- Handling profile page-1 data -----*/
            if (TextUtils.isEmpty(profilePage1Data) == false) {
                if (gson == null) {
                    gson = new Gson();
                }
                ProfilePage1Data pageData = gson.fromJson(profilePage1Data, ProfilePage1Data.class);
                if (pageData != null) {
                    if (data == null) {
                        data = new ProfileDataArgs();
                        data.EmailID = email;
                    }

                    data.RelationshipStatus = pageData.RelationshipStatus;
                    data.SexualOrientation = pageData.SexualOrientation;
                    data.NumberOfHoursOfSleepPerNight = pageData.NumberOfHoursOfSleepPerNight;
                    data.Exercise = pageData.Exercise;
                    data.GeneralStressLevel = pageData.GeneralStressLevel;
                }
            }

            /*----- Handling profile page-2 data -----*/
            if (TextUtils.isEmpty(profilePage2Data) == false) {
                if (gson == null) {
                    gson = new Gson();
                }
                ProfilePage2Data pageData = gson.fromJson(profilePage2Data, ProfilePage2Data.class);
                if (pageData != null) {
                    if (data == null) {
                        data = new ProfileDataArgs();
                        data.EmailID = email;
                    }

                    data.Height = pageData.Height;
                    data.Weight = pageData.Weight;
                    data.Thyroid = pageData.Thyroid;
                    data.Diabetes = pageData.Diabetes;
                    data.TobaccoUse = pageData.TobaccoUse;
                    data.RecreationalDrugUse = pageData.RecreationalDrugUse;
                    data.STD = pageData.STD;
                    data.PreviousPregnancy = pageData.PreviousPregnancy;
                    data.ListMedications = pageData.ListMedications;
                }
            }

            if (data != null) {
                ApiResult response = Common.invokeAPIEx(this, ServiceMethods.SaveProfile, data);
                if (response != null && TextUtils.isEmpty(response.Json) == false) {
                    ApiReqResult result = gson.fromJson(response.Json, ApiReqResult.class);
                    if (result != null && result.Result == 1) {
                        Shared.setInt(this, Shared.KEY_PROFILE_INFO_SAVED, 1);
                    }
                }
            }
        }
    }
}
