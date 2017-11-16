package com.win.winfertility.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.win.winfertility.R;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.EmployerContactArgs;
import com.win.winfertility.dto.ApiResult;

public class EmployerContactManager {
    private Activity _activity;
    private EmployerContactManager Pointer;

    private EditText _txt_contact_emp_employer;
    private EditText _txt_contact_emp_no_of_employees;
    private EditText _txt_contact_emp_hr;
    private View _vw_contact_employer_dialog;
    View linear_list;
    public EmployerContactManager(Activity activity) {
        this.Pointer = this;
        this._activity = activity;
    }

    public void init(View view) {
        /*----- Contact Employer Dialog Controls -----*/
        Pointer._txt_contact_emp_employer = (EditText) Pointer._activity.findViewById(R.id.txt_contact_emp_employer);
        Pointer._txt_contact_emp_hr = (EditText) Pointer._activity.findViewById(R.id.txt_contact_emp_hr);
        Pointer._txt_contact_emp_no_of_employees = (EditText) Pointer._activity.findViewById(R.id.txt_contact_emp_no_of_employees);
        linear_list = view;

        Pointer._vw_contact_employer_dialog = Pointer._activity.findViewById(R.id.vw_contact_employer_dialog);
        if(Pointer._vw_contact_employer_dialog != null) {
            Common.hideKeyboard(_activity);
            Pointer._vw_contact_employer_dialog.setVisibility(View.GONE);
            if(linear_list !=null) {
                linear_list.setVisibility(View.VISIBLE);
            }
        }

        View btn_contact_emp_proceed = Pointer._activity.findViewById(R.id.btn_contact_emp_proceed);
        if(btn_contact_emp_proceed != null) {
            btn_contact_emp_proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.sendContactEmployerRequest();
                }
            });
        }

        View btn_contact_emp_close = Pointer._activity.findViewById(R.id.btn_contact_emp_close);
        if(btn_contact_emp_close != null) {
            btn_contact_emp_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Pointer._vw_contact_employer_dialog != null) {
                        Common.hideKeyboard(_activity);
                        Pointer._vw_contact_employer_dialog.setVisibility(View.GONE);
                        if(linear_list !=null) {
                            linear_list.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }
    public void showDialog() {
        if(Pointer._vw_contact_employer_dialog != null) {
            Pointer._vw_contact_employer_dialog.setVisibility(View.VISIBLE);
            if(linear_list !=null) {
                linear_list.setVisibility(View.GONE);
            }
            if(Pointer._txt_contact_emp_employer != null) {
                Pointer._txt_contact_emp_employer.requestFocus();
            }
        }
    }

    private void sendContactEmployerRequest() {
        if(Pointer._txt_contact_emp_employer != null) {
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Pointer._vw_contact_employer_dialog != null) {
                        Pointer._vw_contact_employer_dialog.setVisibility(View.VISIBLE);
                        if(linear_list !=null) {
                            linear_list.setVisibility(View.GONE);
                        }
                    }
                    Pointer._txt_contact_emp_employer.requestFocus();
                }
            };

            if (Pointer._vw_contact_employer_dialog != null) {
                Common.hideKeyboard(_activity);
                Pointer._vw_contact_employer_dialog.setVisibility(View.GONE);
                if(linear_list !=null) {
                    linear_list.setVisibility(View.VISIBLE);
                }
            }

            String employer = Pointer._txt_contact_emp_employer.getText().toString();
            if(TextUtils.isEmpty(employer)) {
                Notify.show(Pointer._activity, "Please enter your employer name.", onClickListener);
                return;
            }

            Loader.show(Pointer._activity);
            EmployerContactArgs args = new EmployerContactArgs();
            args.EmailID = Shared.getString(Pointer._activity, Shared.KEY_EMAIL_ID);
            args.EmployerName = Pointer._txt_contact_emp_employer.getText().toString();
            args.HRName = Pointer._txt_contact_emp_hr.getText().toString();
            args.NumberOfEmployees = Pointer._txt_contact_emp_no_of_employees.getText().toString();

            Common.invokeAPI(Pointer._activity, ServiceMethods.HelpUsContactEmployer, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if(msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if(TextUtils.isEmpty(response.Json) == false) {
                                ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                if(data != null && data.Result == 1) {
                                    Notify.show(Pointer._activity, "Thank you for submitting your employer information. \nWe are constantly updating our network and will reach out to your employer to see if they want to participate. \nIn the meantime you may find this information useful http://go.winfertility.com/benefits");
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
                    Notify.show(Pointer._activity, error, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Pointer._vw_contact_employer_dialog != null) {
                                Pointer._vw_contact_employer_dialog.setVisibility(View.VISIBLE);
                                if(linear_list !=null) {
                                    linear_list.setVisibility(View.GONE);
                                }
                            }
                            Pointer._txt_contact_emp_employer.requestFocus();
                        }
                    });
                    return true;
                }
            }));
        }
    }
}
