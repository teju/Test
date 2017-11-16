package com.win.winfertility.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import com.google.gson.Gson;
import com.win.winfertility.Notifications;
import com.win.winfertility.R;
import com.win.winfertility.SettingsActivity;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.NotificationFeedbackArgs;
import com.win.winfertility.dto.ApiResult;

public class NotificationManager {
    private Activity _activity;
    private NotificationManager Pointer;

    private EditText _txt_turnoff_notification_text;
    private RadioButton _rb_turn_off_notify_recommend_yes;
    private View _vw_turnoff_notification_dialog;
    ListView linear_list;

    public NotificationManager(Activity activity) {
        this.Pointer = this;
        this._activity = activity;
    }

    public void init() {
        /*----- Turnoff Notification Dialog Controls -----*/
        Pointer._txt_turnoff_notification_text = (EditText) Pointer._activity.findViewById(R.id.txt_turnoff_notification_text);
        Pointer._rb_turn_off_notify_recommend_yes = (RadioButton) Pointer._activity.findViewById(R.id.rb_turn_off_notify_recommend_yes);
        linear_list = (ListView) Pointer._activity.findViewById(R.id.notification_list);

        Pointer._vw_turnoff_notification_dialog = Pointer._activity.findViewById(R.id.vw_turnoff_notification_dialog);
        if(Pointer._vw_turnoff_notification_dialog != null) {
            Common.hideKeyboard(_activity);
            Pointer._vw_turnoff_notification_dialog.setVisibility(View.GONE);
            if(linear_list !=null) {
                linear_list.setVisibility(View.VISIBLE);
            }
        }

        View btn_turnoff_notification_proceed = Pointer._activity.findViewById(R.id.btn_turnoff_notification_proceed);
        if(btn_turnoff_notification_proceed != null) {
            btn_turnoff_notification_proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.sendTurnoffNotificationData();
                }
            });
        }

        View btn_turnoff_notification_close = Pointer._activity.findViewById(R.id.btn_turnoff_notification_close);
        if(btn_turnoff_notification_close != null) {
            btn_turnoff_notification_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Pointer._vw_turnoff_notification_dialog != null) {
                        Common.hideKeyboard(_activity);
                        Pointer._vw_turnoff_notification_dialog.setVisibility(View.GONE);
                        if(linear_list !=null) {
                            linear_list.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }

    public void showDialog() {

        if(Pointer._vw_turnoff_notification_dialog != null) {
            System.out.println("sendTurnoffNotificationData showDialog ");

            Pointer._vw_turnoff_notification_dialog.setVisibility(View.VISIBLE);

            if(linear_list !=null) {
                linear_list.setVisibility(View.GONE);
            }
            if(Pointer._txt_turnoff_notification_text != null) {
                Pointer._txt_turnoff_notification_text.requestFocus();
            }
        }
    }

    private void sendTurnoffNotificationData() {
        if(Pointer._txt_turnoff_notification_text != null) {
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Pointer._vw_turnoff_notification_dialog != null) {
                        Pointer._vw_turnoff_notification_dialog.setVisibility(View.VISIBLE);
                        if(linear_list !=null) {
                            linear_list.setVisibility(View.GONE);
                        }
                    }
                    Pointer._txt_turnoff_notification_text.requestFocus();
                }
            };

            if (Pointer._vw_turnoff_notification_dialog != null) {
                Common.hideKeyboard(_activity);
                Pointer._vw_turnoff_notification_dialog.setVisibility(View.GONE);
                if(linear_list !=null) {
                    linear_list.setVisibility(View.VISIBLE);
                }
            }

            Loader.show(Pointer._activity);
            NotificationFeedbackArgs args = new NotificationFeedbackArgs();
            args.EmailID = Shared.getString(Pointer._activity, Shared.KEY_EMAIL_ID);
            args.RecommendToAFriend = Pointer._rb_turn_off_notify_recommend_yes.isChecked() ? "Yes" : "No";
            args.Feedback = Pointer._txt_turnoff_notification_text.getText().toString();

            Common.invokeAPI(Pointer._activity, ServiceMethods.SaveNotificationOffFeedback, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if(msg.obj != null && msg.obj instanceof ApiResult) {
                            System.out.println("sendTurnoffNotificationData "+msg);

                            ApiResult response = (ApiResult) msg.obj;
                            if(TextUtils.isEmpty(response.Json) == false) {
                                ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                if(data != null && data.Result == 1) {
                                    if(Pointer._activity != null && Pointer._activity instanceof SettingsActivity) {
                                        ((Notifications) Pointer._activity).refreshNotificationStatus(false);
                                    }
                                    if(Pointer._vw_turnoff_notification_dialog != null) {
                                        Common.hideKeyboard(_activity);
                                        Pointer._vw_turnoff_notification_dialog.setVisibility(View.GONE);
                                        if(linear_list !=null) {
                                            linear_list.setVisibility(View.VISIBLE);
                                        }
                                    }
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
                            if (Pointer._vw_turnoff_notification_dialog != null) {
                                Pointer._vw_turnoff_notification_dialog.setVisibility(View.VISIBLE);
                                if(linear_list !=null) {
                                    linear_list.setVisibility(View.GONE);
                                }
                            }
                            Pointer._txt_turnoff_notification_text.requestFocus();
                        }
                    });
                    return true;
                }
            }));
        }
    }
}
