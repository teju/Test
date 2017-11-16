package com.win.winfertility;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.PasswordChangeArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends WINFertilityActivity {
    private ChangePasswordActivity Pointer;

    private EditText _txt_pwd;
    private EditText _txt_new_pwd;
    private EditText _txt_confirm_new_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_change_password);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        Pointer._txt_pwd = (EditText) this.findViewById(R.id.txt_pwd);
        if(Pointer._txt_pwd != null) {
            Pointer._txt_pwd.setTypeface(Typeface.DEFAULT);
        }
        Pointer._txt_new_pwd = (EditText) this.findViewById(R.id.txt_new_pwd);
        if(Pointer._txt_new_pwd != null) {
            Pointer._txt_new_pwd.setTypeface(Typeface.DEFAULT);
        }
        Pointer._txt_confirm_new_pwd = (EditText) this.findViewById(R.id.txt_confirm_new_pwd);
        if(Pointer._txt_confirm_new_pwd != null) {
            Pointer._txt_confirm_new_pwd.setTypeface(Typeface.DEFAULT);
        }

        View btn_submit = this.findViewById(R.id.btn_submit);
        if(btn_submit != null) {
            btn_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.handleSubmit();
                }
            });
        }
    }
    private void handleSubmit() {
        PasswordChangeArgs args = new PasswordChangeArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        args.OldPassword = Pointer._txt_pwd.getText().toString();
        args.NewPassword = Pointer._txt_new_pwd.getText().toString();
        args.ConfirmNewPassword = Pointer._txt_confirm_new_pwd.getText().toString();

        if(Pointer.isValidInputs(args)) {
            Loader.show(Pointer);
            Common.invokeAPI(Pointer, ServiceMethods.ChangePassword, args, new Handler(new Handler.Callback() {
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
                                        if(TextUtils.isEmpty(error) == false) {
                                            if(error.toLowerCase().contains("fail")) {
                                                Notify.show(Pointer, "Invalid password.", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Pointer._txt_pwd.requestFocus();
                                                    }
                                                });
                                                return true;
                                            }
                                        }
                                    }
                                    else {
                                        Notify.show(Pointer, "Your password has been changed successfully! Thank you.", new DialogInterface.OnClickListener() {
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
                    Notify.show(Pointer, error);
                    return true;
                }
            }));
        }
    }
    public boolean isValidInputs(PasswordChangeArgs args) {
        if(TextUtils.isEmpty(args.OldPassword)) {
            Notify.show(Pointer, "Please enter your password.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_pwd.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.NewPassword)) {
            Notify.show(Pointer, "Please enter your new password.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_new_pwd.requestFocus();
                }
            });
            return false;
        }
        else if(args.NewPassword.length() < 6) {
            Notify.show(Pointer, "OldPassword should be minimum 6 characters.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_new_pwd.requestFocus();
                }
            });
            return false;
        } else if(args.NewPassword == null && !isValidPassword(args.NewPassword)) {
            Notify.show(Pointer, "Password must contain a number,a special character,an upper case" +
                            " alphabet, a lower case alphabet and has to be 8 characters in length\"",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Pointer._txt_new_pwd.requestFocus();
                        }
                    });
            return false;
        }
        else if(TextUtils.isEmpty(args.ConfirmNewPassword)) {
            Notify.show(Pointer, "Please confirm your new password.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_confirm_new_pwd.requestFocus();
                }
            });
            return false;
        }
        else if(args.NewPassword.compareTo(args.ConfirmNewPassword) != 0) {
            Notify.show(Pointer, "Your password and confirmation password do not match.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_confirm_new_pwd.requestFocus();
                }
            });
            return false;
        }

        return true;
    }
    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

}
