package com.win.winfertility;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.BaseUserReqArgs;
import com.win.winfertility.dataobjects.EmailArgs;
import com.win.winfertility.dataobjects.ProfileDisplayData;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EmailActivity extends WINFertilityActivity {
    private EmailActivity Pointer;

    private EditText _txt_first_name;
    private EditText _txt_last_name;
    private EditText _txt_email;
    private EditText _txt_phone;
    private TextView _txt_dob;
    private TextView _vw_time_to_call;
    private TextView _vw_discuss_about;
    private EditText _txt_comments;
    private View _btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_email);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        Pointer._txt_first_name = (EditText) this.findViewById(R.id.txt_first_name);
        Pointer._txt_last_name = (EditText) this.findViewById(R.id.txt_last_name);
        Pointer._txt_email = (EditText) this.findViewById(R.id.txt_email);
        Pointer._txt_phone = (EditText) this.findViewById(R.id.txt_phone);
        Pointer._txt_dob = (TextView) this.findViewById(R.id.txt_dob);
        Pointer._vw_time_to_call = (TextView) this.findViewById(R.id.vw_time_to_call);
        Pointer._vw_discuss_about = (TextView) this.findViewById(R.id.vw_discuss_about);
        Pointer._txt_comments = (EditText) this.findViewById(R.id.txt_comments);
        Pointer._btn_submit = this.findViewById(R.id.btn_submit);

        if(Pointer._txt_dob != null) {
            Pointer._txt_dob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar calendar = Calendar.getInstance();
                    if(Pointer._txt_dob.getText().toString().trim().length() > 0) {
                        try {
                            calendar.setTime(Common.WinAppDateFormat.parse(Pointer._txt_dob.getText().toString()));
                        } catch (Exception e) {
                        }
                    }
                    DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                            calendar.set(year, monthOfYear, dayOfMonth);
                            Pointer._txt_dob.setText(Common.WinAppDateFormat.format(calendar.getTime()));
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.setAccentColor("#FF4081");
                    datePickerDialog.setVersion(DatePickerDialog.Version.VERSION_1);
                    datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
                }
            });
        }

        if(Pointer._vw_time_to_call != null) {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("Mornings"));
            items.add(new SelectDialogItem("Afternoons"));
            items.add(new SelectDialogItem("Evenings"));
            SelectBox.config(Pointer._vw_time_to_call, items, "Time To Call");
        }

        if(Pointer._vw_discuss_about != null) {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("I have been trying to get pregnant for several months and have questions"));
            items.add(new SelectDialogItem("Need help selecting a provider"));
            items.add(new SelectDialogItem("Would like to speak with a nurse about my current treatment"));
            items.add(new SelectDialogItem("Have questions regarding my benefits"));
            items.add(new SelectDialogItem("I would like info on genetic testing"));
            items.add(new SelectDialogItem("I would like info on elective egg freezing"));
            items.add(new SelectDialogItem("It is something else"));
            SelectBox.config(Pointer._vw_discuss_about, items, "Discuss About", true);
        }

        if(Pointer._btn_submit != null) {
            Pointer._btn_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.handleSubmit();
                }
            });
        }

        Pointer.loadProfileData();
    }
    private void loadProfileData() {
        Loader.show(Pointer);
        final BaseUserReqArgs args = new BaseUserReqArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        Common.invokeAPI(Pointer, ServiceMethods.GetProfileDisplayData, args, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String error = null;
                try {
                    if (msg.obj != null && msg.obj instanceof ApiResult) {
                        ApiResult response = (ApiResult) msg.obj;
                        if (TextUtils.isEmpty(response.Json) == false) {
                            ProfileDisplayData data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ProfileDisplayData.class);
                            if (data != null && TextUtils.isEmpty(data.ProfileName) == false) {
                                Pointer._txt_first_name.setText(Common.replaceNull(data.FirstName));
                                Pointer._txt_last_name.setText(Common.replaceNull(data.LastName));
                                Pointer._txt_email.setText(args.EmailID);
                                Pointer._txt_phone.setText(Common.replaceNull(data.PhoneNumber));
                                Pointer._txt_dob.setText(Common.replaceNull(data.DateOfBirth));
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
    private void handleSubmit() {
        EmailArgs args = new EmailArgs();
        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
        args.FirstName = Pointer._txt_first_name.getText().toString();
        args.LastName = Pointer._txt_last_name.getText().toString();
        args.EmailAddress = Pointer._txt_email.getText().toString();
        args.PhoneNumber = Pointer._txt_phone.getText().toString();
        args.DateOfBirth = Pointer._txt_dob.getText().toString();
        args.BestTimeToCall = SelectBox.getData(Pointer._vw_time_to_call).ID;
        args.Subject = Common.replaceNull(SelectBox.getSelectBoxText(Pointer._vw_discuss_about, ", "));
        args.Notes = Pointer._txt_comments.getText().toString();

        if(Pointer.isValidInputs(args)) {
            Loader.show(Pointer);
            Common.invokeAPI(Pointer, ServiceMethods.SendEmail, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if(msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if(TextUtils.isEmpty(response.Json) == false) {
                                ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                if (data != null && data.Result == 1) {
                                    Notify.show(Pointer, "Thank you for sending your request to WINFertility. We will contact you at the time you indicated on the form.", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Pointer.finish();
                                        }
                                    });
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
    }
    private boolean isValidInputs(EmailArgs args) {
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
        else if(TextUtils.isEmpty(args.EmailAddress)) {
            Notify.show(Pointer, "Please enter your email address.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_email.requestFocus();
                }
            });
            return false;
        }
        else if(Common.isValidEmail(args.EmailAddress) == false) {
            Notify.show(Pointer, "Please enter valid email address.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_email.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.PhoneNumber)) {
            Notify.show(Pointer, "Please enter your phone number.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_phone.requestFocus();
                }
            });
            return false;
        }
        else if(Common.isValidNumber(args.PhoneNumber) == false || args.PhoneNumber.length() != 10) {
            Notify.show(Pointer, "Please enter valid phone number.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_phone.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.DateOfBirth)) {
            Notify.show(Pointer, "Please enter your date of birth.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_dob.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.BestTimeToCall)) {
            Notify.show(Pointer, "Please select best time to call.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._vw_time_to_call.requestFocus();
                }
            });
            return false;
        }

        return true;
    }
}
