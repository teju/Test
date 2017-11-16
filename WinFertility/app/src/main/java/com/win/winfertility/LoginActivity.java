package com.win.winfertility;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.AccountDataResult;
import com.win.winfertility.dataobjects.LoginArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;


public class LoginActivity extends WINFertilityActivity {
    private EditText _txt_email;
    private EditText _txt_password;
    private LoginActivity Pointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        Pointer._txt_email = (EditText) this.findViewById(R.id.txt_email);
        Pointer._txt_password = (EditText) this.findViewById(R.id.txt_password);
        if(Pointer._txt_password != null) {
            Pointer._txt_password.setTypeface(Typeface.DEFAULT);
        }
        View btn_login = this.findViewById(R.id.btn_login);
        if(btn_login != null) {
            btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.handleLogin();
                }
            });
        }
        View btn_forgot_password = this.findViewById(R.id.btn_forgot_password);
        if(btn_forgot_password != null) {
            btn_forgot_password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.handleForgotPassword();
                }
            });
        }
    }

    private void handleLogin() {
        final LoginArgs args = new LoginArgs();
        args.EncryptPassword = "";
        args.EmailID = Pointer._txt_email.getText().toString();
        args.Password = Pointer._txt_password.getText().toString();

        if(Pointer.isValidInputs(args)) {
            Loader.show(Pointer);
            Common.invokeAPI(Pointer, ServiceMethods.Login, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if(msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if(TextUtils.isEmpty(response.Json) == false) {
                                AccountDataResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), AccountDataResult.class);
                                if(data != null) {
                                    if(data.Result != 2) {
                                        switch (data.Result) {
                                            case 0: error = "This email address is not registered."; break;
                                            case 1: error = "Invalid password."; break;
                                        }
                                    }
                                    else {
                                        if(TextUtils.isEmpty(data.EmailID) == false) {
                                            data.Password=args.Password;
                                            System.out.println("LOGINACTIVITYJAVA "+data.Password);
                                            Pointer.saveAccountDetails(data);
                                            Pointer.startActivity(new Intent(Pointer, HomeActivity.class));
                                            return true;
                                        }
                                    }
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
                    Notify.show(Pointer, error, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Pointer._txt_email.requestFocus();
                        }
                    });
                    return true;
                }
            }));
        }
    }
    private void saveAccountDetails(AccountDataResult result) {
        String fcmTokenID = Common.replaceNull(Shared.getString(Pointer, Shared.KEY_TOKEN_ID));
        String createdEmailID = Common.replaceNull(Shared.getString(Pointer, Shared.KEY_CREATED_EMAIL_ID));
        Shared.clear(Pointer);
        Shared.setString(Pointer, Shared.KEY_CREATED_EMAIL_ID, createdEmailID);
        Shared.setString(Pointer, Shared.KEY_ENROLLED, Common.replaceNull(result.Enrolled));
        Shared.setString(Pointer, Shared.KEY_PROFILE_NAME, result.ProfileName);
        Shared.setString(Pointer, Shared.KEY_PASSWORD, result.Password);
        Shared.setString(Pointer, Shared.KEY_NOTIFICATION_ENABLED, result.NotificationStatus);
        Shared.setString(Pointer, Shared.KEY_EMAIL_ID, (result.EmailID != null ? result.EmailID : ""));
        Shared.setString(Pointer, Shared.KEY_PHONE, (result.PhoneNumber != null ? result.PhoneNumber : ""));
        Shared.setString(Pointer, Shared.KEY_FERTILITY_EDU_URL, (result.FertilityEducationLink != null ? result.FertilityEducationLink : ""));
        Shared.setString(Pointer, Shared.KEY_PROVIDER_SEARCH_URL, (result.ProviderSearchLink != null ? result.ProviderSearchLink : ""));
        Shared.setString(Pointer, Shared.KEY_BENEFITS_OVERVIEW_URL, (result.BenefitsOverview != null ? result.BenefitsOverview : ""));
        Shared.setString(Pointer, Shared.KEY_TOKEN_ID, fcmTokenID);

        Shared.setInt(Pointer, Shared.KEY_MENSTRUAL_INFO_SAVED, 1);
        Shared.setInt(Pointer, Shared.KEY_PROFILE_INFO_SAVED, 1);

        /*----- Saving employer logo -----*/
        Common.removeEmployerLogo(Pointer);
        Common.setEmployerLogo(Pointer, result.Logo);
        System.out.println("setEmployerLogo1234 "+result.Logo);
        Common.setProfileImage(Pointer, result.ProfilePhoto);
    }
    private void handleForgotPassword() {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }
    private boolean isValidInputs(LoginArgs args) {
        if(TextUtils.isEmpty(args.EmailID)) {
            Notify.show(Pointer, "Please enter your email address.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_email.requestFocus();
                }
            });
            return false;
        }
        else if(Common.isValidEmail(args.EmailID) == false) {
            Notify.show(Pointer, "Please enter valid email address.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_email.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.Password)) {
            Notify.show(Pointer, "Please enter your password.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_password.requestFocus();
                }
            });
            return false;
        }

        return true;
    }
}
