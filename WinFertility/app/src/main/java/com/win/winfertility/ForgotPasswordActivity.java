package com.win.winfertility;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.ForgotPasswordArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.ServiceMethods;

public class ForgotPasswordActivity extends WINFertilityActivity {
    private ForgotPasswordActivity Pointer;
    private EditText _txt_first_name;
    private EditText _txt_last_name;
    private EditText _txt_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_forgot_password);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        Pointer._txt_first_name = (EditText) this.findViewById(R.id.txt_first_name);
        Pointer._txt_last_name = (EditText) this.findViewById(R.id.txt_last_name);
        Pointer._txt_email = (EditText) this.findViewById(R.id.txt_email);

        View btn_recover = this.findViewById(R.id.btn_recover);
        if(btn_recover != null) {
            btn_recover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.handleForgotPassword();
                }
            });
        }
    }

    private void handleForgotPassword() {
        ForgotPasswordArgs args = new ForgotPasswordArgs();
        args.FirstName = Pointer._txt_first_name.getText().toString();
        args.LastName = Pointer._txt_last_name.getText().toString();
        args.EmailID = Pointer._txt_email.getText().toString();

        if(Pointer.isValidInputs(args)) {
            Loader.show(Pointer);
            Common.invokeAPI(Pointer, ServiceMethods.ForgotPassword, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if(msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if(TextUtils.isEmpty(response.Json) == false) {
                                ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                if(data != null) {
                                    if(data.Result != 1) {
                                        error = data.Message;
                                    }
                                    else {
                                        Notify.show(Pointer, data.Message, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Pointer.finish();
                                            }
                                        });
                                        return true;
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
                            Pointer._txt_first_name.requestFocus();
                        }
                    });
                    return true;
                }
            }));
        }
    }
    private boolean isValidInputs(ForgotPasswordArgs args) {
        if(TextUtils.isEmpty(args.FirstName)) {
            Notify.show(Pointer, "Please enter your first name.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_first_name.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.LastName)) {
            Notify.show(Pointer, "Please enter your last name.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_last_name.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.EmailID)) {
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

        return true;
    }
}
