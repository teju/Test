package com.win.winfertility;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.EmployerInfoArgs;
import com.win.winfertility.dataobjects.EmployerInfoResult;
import com.win.winfertility.dataobjects.ProfileDisplayData;
import com.win.winfertility.dataobjects.RegisterArgs;
import com.win.winfertility.dataobjects.AccountDataResult;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.dto.SelectDialogItem;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;
import com.win.winfertility.utils.SelectBox;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountInfoActivity extends WINFertilityActivity {
    public static AccountInfoActivity Instance;

    private AccountInfoActivity Pointer;
    private EditText _txt_first_name;
    private EditText _txt_last_name;
    private EditText _txt_email;
    private EditText _txt_phone;
    private EditText _txt_password;
    private EditText _confirm_txt_password;
    private TextView _txt_dob;
    private EditText _txt_company;
    private EditText _txt_promo_code;
    private RadioButton _rb_provide_yes;
    private RadioButton _rb_provide_no;
    private TextView _txt_gender;
    private Button _btn_next;

    private ProfileDisplayData _data;
    private boolean _isEdit;
    private String _employerDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccountInfoActivity.Instance = this;
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_account_info);
        this.Pointer = this;
        this.init();
        this.handleIntentData();
    }

    private void init() {
        Pointer._txt_first_name = (EditText) this.findViewById(R.id.txt_first_name);
        Pointer._txt_last_name = (EditText) this.findViewById(R.id.txt_last_name);
        Pointer._txt_email = (EditText) this.findViewById(R.id.txt_email);
        Pointer._txt_phone = (EditText) this.findViewById(R.id.txt_phone);
        Pointer._txt_password = (EditText) this.findViewById(R.id.txt_password);
        Pointer._confirm_txt_password = (EditText) this.findViewById(R.id.confirm_txt_password);
        Pointer._txt_dob = (TextView) this.findViewById(R.id.txt_dob);
        Pointer._txt_company = (EditText) this.findViewById(R.id.txt_company);
        Pointer._txt_promo_code = (EditText) this.findViewById(R.id.txt_promo_code);
        Pointer._txt_gender = (TextView) this.findViewById(R.id.txt_gender);
        Pointer._rb_provide_yes = (RadioButton) this.findViewById(R.id.rb_provide_yes);
        Pointer._rb_provide_no = (RadioButton) this.findViewById(R.id.rb_provide_no);

        if(Pointer._txt_password != null) {
            Pointer._txt_password.setTypeface(Typeface.DEFAULT);
        }
        if(Pointer._confirm_txt_password != null) {
            Pointer._confirm_txt_password.setTypeface(Typeface.DEFAULT);
        }
        if(Pointer._txt_dob != null) {
            new WinDatePickerDialog(Pointer._txt_dob).init();
        }

        if(Pointer._txt_gender != null) {
            List<SelectDialogItem> genders = new ArrayList<>();
            genders.add(new SelectDialogItem("Female"));
            genders.add(new SelectDialogItem("Male"));
            genders.add(new SelectDialogItem("Not Specified"));
            genders.add(new SelectDialogItem("Prefer not to say"));
            SelectBox.config(Pointer._txt_gender, genders, "Gender");
        }

        Pointer._btn_next = (Button) this.findViewById(R.id.btn_next);
        if(Pointer._btn_next != null) {
            Pointer._btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.handleNext();
                }
            });
        }
    }

    private void handleIntentData() {
        Intent intent = this.getIntent();
        if(intent != null) {
            if(intent.hasExtra("DATA")) {
                String json = intent.getStringExtra("DATA");
                if(TextUtils.isEmpty(json) == false) {
                    Pointer._data = new Gson().fromJson(json, ProfileDisplayData.class);
                    if(Pointer._data != null) {
                        Pointer._isEdit = true;
                        Pointer._txt_first_name.setText(Common.replaceNull(Pointer._data.FirstName));
                        Pointer._txt_last_name.setText(Common.replaceNull(Pointer._data.LastName));
                        Pointer._txt_email.setText(Shared.getString(Pointer, Shared.KEY_EMAIL_ID));
                        Pointer._txt_email.setEnabled(false);
                        Pointer._txt_phone.setText(Common.replaceNull(Pointer._data.PhoneNumber));
                        Pointer._txt_dob.setText(Common.replaceNull(Pointer._data.DateOfBirth));
                        Pointer._txt_password.setVisibility(View.GONE);
                        Pointer._confirm_txt_password.setVisibility(View.GONE);
                        Pointer.findViewById(R.id.vw_pwd_divider).setVisibility(View.GONE);
                        SelectBox.setData(Pointer._txt_gender, new SelectDialogItem(Common.replaceNull(Pointer._data.Gender)));
                        Pointer._txt_company.setText(Common.replaceNull(Pointer._data.Company));
                        Pointer._txt_promo_code.setText(Common.replaceNull(Pointer._data.Promocode));
                        System.out.println("WINBenefits233232 "+Pointer._data.WINBenefits);
                        Pointer._employerDetails = Common.replaceNull(Pointer._data.Company).trim().toUpperCase() + "|" + Common.replaceNull(Pointer._data.Promocode).trim().toUpperCase();

                        Pointer._rb_provide_yes.setChecked(Common.replaceNull(Pointer._data.WINBenefits).compareToIgnoreCase("Y") == 0);
                        Pointer._rb_provide_no.setChecked(Common.replaceNull(Pointer._data.WINBenefits).compareToIgnoreCase("N") == 0);
                    }
                }
            }
        }
    }

    private void handleNext() {
        RegisterArgs args = new RegisterArgs();
        args.FirstName = Pointer._txt_first_name.getText().toString();
        args.LastName = Pointer._txt_last_name.getText().toString();
        args.EmailID = Pointer._txt_email.getText().toString();
        args.PhoneNo = Pointer._txt_phone.getText().toString();
        if(Pointer._txt_password.getVisibility() == View.VISIBLE) {
            args.PWD = Pointer._txt_password.getText().toString();
        }
        else {
            args.PWD = "";
        }
        args.DOB = Pointer._txt_dob.getText().toString();
        args.confirm_password = Pointer._confirm_txt_password.getText().toString();
        args.Company = Pointer._txt_company.getText().toString();
        args.Promocode = Pointer._txt_promo_code.getText().toString();
        args.Gender = SelectBox.getData(Pointer._txt_gender).ID;
        args.UpdateFlag = (Pointer._isEdit ? "Y" : "");
        args.WINBenefits = (Pointer._rb_provide_yes.isChecked() ? "Y" : (Pointer._rb_provide_no.isChecked() ? "N" : ""));

        /*----- Checking the employer details and creating new account -----*/
        if(Pointer.isValidInputs(args)) {
            Pointer.checkEmployerInfoAndProceed(args);
        }
    }

    private void checkEmployerInfoAndProceed(final RegisterArgs args) {
        if(TextUtils.isEmpty(args.Company) && TextUtils.isEmpty(args.Promocode)) {
            Notify.show(Pointer, "You did not enter any employer information.\n\nWe will not be able to provide you with any plan information.\nDo you still want to proceed?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == DialogInterface.BUTTON_POSITIVE) {
                        Pointer.createAccount(args);
                    }
                }
            }, "Proceed", null, true);
        }
        else {
            String employerDetails = Pointer._txt_company.getText().toString().trim().toUpperCase() + "|" + Pointer._txt_promo_code.getText().toString().trim().toUpperCase();
            boolean isEmployerChanged = (Common.replaceNull(Pointer._employerDetails).compareToIgnoreCase(employerDetails) != 0);
            if(Pointer._isEdit == true && isEmployerChanged == false) {
                Pointer.createAccount(args);
            }
            else {
                EmployerInfoArgs employerArgs = new EmployerInfoArgs();
                employerArgs.Company = args.Company;
                employerArgs.Promocode = args.Promocode;
                employerArgs.WINBenefits = args.WINBenefits;

                Loader.show(Pointer);
                Common.invokeAPI(Pointer, ServiceMethods.GetEmployerDetails, employerArgs, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        String error = null;
                        try {
                            if (msg.obj != null && msg.obj instanceof ApiResult) {
                                ApiResult response = (ApiResult) msg.obj;
                                System.out.println("SYSTEMPRINT1 invokeAPI "+response.Json);

                                if (TextUtils.isEmpty(response.Json) == false) {
                                    EmployerInfoResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json),
                                            EmployerInfoResult.class);
                                    if (data != null ) {
                                        /*Notify.show(Pointer, data.EmployerMessage, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                                    Pointer.createAccount(args);
                                                }
                                            }
                                        }, "Proceed", null, true);*/
                                        Pointer.createAccount(args);

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
                        Notify.show(Pointer, error);
                        return true;
                    }
                }));
            }
        }
    }

    private void createAccount(final RegisterArgs args) {
        /*----- Setting goal from previous screen selection -----*/
        Intent intent = Pointer.getIntent();
        if(intent != null) {
            if(intent.hasExtra(Shared.EXTRA_GOAL)) {
                args.Goal = intent.getStringExtra(Shared.EXTRA_GOAL);
            }
        }
        System.out.println("PASSWORDISONCREATE "+args.PWD);
        Loader.show(Pointer);
        Common.invokeAPI(Pointer, ServiceMethods.CreateAccount, args, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String error = null;
                try {
                    if(msg.obj != null && msg.obj instanceof ApiResult) {
                        ApiResult response = (ApiResult) msg.obj;
                        if(TextUtils.isEmpty(response.Json) == false) {
                            AccountDataResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), AccountDataResult.class);
                            if(data != null) {
                                if(data.Result != 1) {
                                    error = data.Message;
                                }
                                else {
                                    data.EmailID = args.EmailID;
                                    Pointer.saveAccountDetails(data,args.PWD);
                                    /*----- Close previous account screens in edit mode -----*/
                                    if(Pointer._isEdit) {
                                        if(AccountOptionActivity.Instance != null) {
                                            AccountOptionActivity.Instance.finish();
                                        }
                                        Pointer.finish();
                                    }

                                    Intent intent = new Intent(Pointer, ProfilePage1Activity.class);
                                    intent.putExtra(Shared.EXTRA_IS_EDIT, Pointer._isEdit);
                                    Pointer.startActivity(intent);
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
                        Pointer._txt_email.requestFocus();
                    }
                });
                return true;
            }
        }));
    }
    private void saveAccountDetails(AccountDataResult result,String password) {
        String fcmTokenID = Common.replaceNull(Shared.getString(Pointer, Shared.KEY_TOKEN_ID));
        /*----- Clear the app memory only for account creation (Not for account update) -----*/
        if(Pointer._data == null) {
            Shared.clear(Pointer);
        }
        /*----- Saving created profile details -----*/
        Shared.setString(Pointer, Shared.KEY_CREATED_EMAIL_ID, (result.EmailID != null ? result.EmailID : ""));
        Shared.setString(Pointer, Shared.KEY_ENROLLED, Common.replaceNull(result.Enrolled));
        Shared.setString(Pointer, Shared.KEY_EMAIL_ID, (result.EmailID != null ? result.EmailID : ""));
        if(password.length() != 0) {
            Shared.setString(Pointer, Shared.KEY_PASSWORD, password);
        }
        Shared.setString(Pointer, Shared.KEY_PROFILE_NAME, result.ProfileName);
        Shared.setString(Pointer, Shared.KEY_NOTIFICATION_ENABLED, result.NotificationStatus);
        Shared.setString(Pointer, Shared.KEY_PHONE, (result.PhoneNumber != null ? result.PhoneNumber : ""));
        Shared.setString(Pointer, Shared.KEY_FERTILITY_EDU_URL, (result.FertilityEducationLink != null ? result.FertilityEducationLink : ""));
        Shared.setString(Pointer, Shared.KEY_PROVIDER_SEARCH_URL, (result.ProviderSearchLink != null ? result.ProviderSearchLink : ""));
        Shared.setString(Pointer, Shared.KEY_BENEFITS_OVERVIEW_URL, (result.BenefitsOverview != null ? result.BenefitsOverview : ""));
        Shared.setString(Pointer, Shared.KEY_TOKEN_ID, fcmTokenID);
        /*----- Saving employer logo -----*/
        Common.removeEmployerLogo(Pointer);
        Common.setEmployerLogo(Pointer, result.Logo);
        Common.setProfileImage(Pointer, result.ProfilePhoto);
    }
    private boolean isValidInputs(RegisterArgs args) {
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
        else if(TextUtils.isEmpty(args.PhoneNo)) {
            Notify.show(Pointer, "Please enter your phone number.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_phone.requestFocus();
                }
            });
            return false;
        }
        else if(Common.isValidNumber(args.PhoneNo) == false || args.PhoneNo.length() != 10) {
            Notify.show(Pointer, "Please enter valid phone number.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_phone.requestFocus();
                }
            });
            return false;
        }
        else if(Common.isValidEmail(args.EmailID) == false) {
            Notify.show(Pointer, "The email address you entered does not appear to be a valid email address.  Please confirm you have entered the email address accurately; or please supply a valid email address.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_email.requestFocus();
                }
            });
            return false;
        }
        else if(Pointer._data == null && TextUtils.isEmpty(args.PWD)) {
            Notify.show(Pointer, "Please enter your password.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_password.requestFocus();
                }
            });
            return false;
        }
        else if(Pointer._data == null && args.PWD.length() < 6) {
            Notify.show(Pointer, "OldPassword should be minimum 6 characters.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_password.requestFocus();
                }
            });
            return false;
        } else if(Pointer._data == null && !isValidPassword(args.PWD)) {
            Notify.show(Pointer, "Password must contain a number,a special character,an upper case" +
                            " alphabet, a lower case alphabet and has to be 8 characters in length\"",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Pointer._txt_password.requestFocus();
                        }
                    });
            return false;
        } else if(Pointer._data == null && args.confirm_password.length() == 0) {
            Notify.show(Pointer, "Please confirm your password.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._confirm_txt_password.requestFocus();
                }
            });
            return false;
        } else if(Pointer._data == null && !args.PWD.equals(args.confirm_password)) {
            Notify.show(Pointer, "Your password and confirmation password do not match .", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._confirm_txt_password.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.DOB)) {
            Notify.show(Pointer, "Please enter your date of birth.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_dob.requestFocus();
                }
            });
            return false;
        }
        else if(isValidDOB(args.DOB) == false) {
            Notify.show(Pointer, "Minimum age limit to register is 15 years old.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Pointer._txt_dob.requestFocus();
                }
            });
            return false;
        }
        else if(TextUtils.isEmpty(args.Gender)) {
            Notify.show(Pointer, "Please select your gender.");
            return false;
        }
        else if(TextUtils.isEmpty(args.WINBenefits)) {
            Notify.show(Pointer, "Please select \"Fertility Benefits\" status.");
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

    private boolean isValidDOB(String dob) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date date = formatter.parse(dob);
            if(formatter.format(date).compareToIgnoreCase(dob) != 0){
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int currentYear = calendar.get(Calendar.YEAR);
            calendar.setTime(date);
            int selectedYear = calendar.get(Calendar.YEAR);
            if((currentYear - 15) < selectedYear) {
                return false;
            }
        }
        catch(Exception ex) {
            return false;
        }
        return true;
    }

    public class WinDatePickerDialog {
        private WinDatePickerDialog Pointer;
        private TextView _view;
        private DatePickerDialog.OnDateSetListener _onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(Pointer._view != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, dayOfMonth);
                    Pointer._view.setText(Common.WinAppDateFormat.format(calendar.getTime()));
                }
            }
        };
        public WinDatePickerDialog(TextView view) {
            this.Pointer = this;
            Pointer._view = view;
        }
        public void init() {
            if(Pointer._view != null) {
                Pointer._view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int currentYear = calendar.get(Calendar.YEAR);
                        if(Pointer._view.getText().toString().trim().length() > 0) {
                            try {
                                calendar.setTime(Common.WinAppDateFormat.parse(Pointer._view.getText().toString()));
                            } catch (Exception e) {
                            }
                        }
                        int year = calendar.get(Calendar.YEAR);
                        if(year == currentYear) {
                            year = year - 18;
                            calendar.set(Calendar.YEAR, year);
                        }
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog dialog = new DatePickerDialog(Pointer._view.getContext(), android.R.style.Theme_Holo_Light_Dialog, Pointer._onDateSetListener, year, month, day);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        DatePicker datepicker = dialog.getDatePicker();
                        List<NumberPicker> pickers = Common.getChildrenByType(NumberPicker.class, (ViewGroup) datepicker.getRootView());
                        if(pickers != null && pickers.size() > 0) {
                            for(NumberPicker picker : pickers) {
                                setDividerColor(picker);
                            }
                        }
                        dialog.show();
                    }
                });
            }
        }
        private void setDividerColor(NumberPicker picker) {
            try {
                if(picker != null) {
                    Field dividerField = picker.getClass().getDeclaredField("mSelectionDivider");
                    dividerField.setAccessible(true);
                    dividerField.set(picker, new ColorDrawable(Color.parseColor("#FB838D")));
                    picker.invalidate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
