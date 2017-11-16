package com.win.winfertility.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.win.winfertility.MyFertilityActivity;
import com.win.winfertility.Notifications;
import com.win.winfertility.R;
import com.win.winfertility.SettingsActivity;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.BaseUserReqArgs;
import com.win.winfertility.dataobjects.PregnancyNotificationResult;
import com.win.winfertility.dataobjects.PregnancyStatusArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

import java.util.Calendar;

public class WINFertilityActivity extends AppCompatActivity {
    private WINFertilityActivity Pointer;
    private BroadcastReceiver FCMBroadcastReceiver = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, final Intent intent) {
            System.out.println("WINFertilityActivity1234 0 "+intent.getStringExtra(Shared.EXTRA_NOTIFICATION_DATE));

            if(intent.getAction().compareToIgnoreCase(Shared.ACTION_FCM_TOKEN_ID) == 0) {
                System.out.println("WINFertilityActivity1234 1");
                String token = intent.getStringExtra(Shared.EXTRA_TOKEN_ID);
                if (TextUtils.isEmpty(token) == false) {
                    Common.saveFCMTokenID(Pointer, token);
                }
            }
            else {
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String date = intent.getStringExtra(Shared.EXTRA_NOTIFICATION_DATE);
                        if (TextUtils.isEmpty(date) == false) {
                            System.out.println("WINFertilityActivity1234 2 "+date);
                            if (Pointer instanceof MyFertilityActivity) {
                                ((MyFertilityActivity) Pointer).showPopupByDate(date);
                            } else {
                                if (MyFertilityActivity.Instance != null) {
                                    MyFertilityActivity.Instance.finish();
                                }
                                Intent new_intent = new Intent(WINFertilityActivity.this, MyFertilityActivity.class);
                                new_intent.putExtra(Shared.EXTRA_NOTIFICATION_DATE, date);
                                startActivity(new_intent);
                            }
                        } else {
                            System.out.println("WINFertilityActivity1234 4");
                            Intent new_intent = new Intent(WINFertilityActivity.this, Notifications.class);
                            startActivity(new_intent);
                            //finish();
                        }
                    }
                };if(intent.getAction().compareToIgnoreCase(Shared.ACTION_FCM_INDIRECT_MSG) == 0) {
                    runnable.run();
                } else if (intent.getAction().compareToIgnoreCase(Shared.ACTION_FCM_DIRECT_MSG) == 0) {
                    System.out.println("WINFertilityActivity1234 ACTION_FCM_DIRECT_MSG 3");
                    Notify.show(Pointer, "WINFertility Reminder Alert. Click OK to view.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                runnable.run();
                            }
                        }
                    }, "OK", "CANCEL");
                } else if(intent.getAction().compareToIgnoreCase(Shared.ACTION_FCM_INDIRECT_MSG_2) == 0) {
                    runnable.run();
                }else {
                    System.out.println("WINFertilityActivity1234 3");

                    Notify.show(Pointer, "WINFertility Reminder Alert. Click OK to view.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                runnable.run();
                            }
                        }
                    }, "OK", "CANCEL");
                }
            }
        }
    };

    public WINFertilityActivity() {
        this.Pointer = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*----- Starting data sync service -----*/
        WINFertilityService.start(this);
        this.handlePregnancyNotification();
        /*----- Registering FCM Data Broadcast Receiver -----*/
        IntentFilter filter = new IntentFilter();
        filter.addAction(Shared.ACTION_FCM_DIRECT_MSG);
        filter.addAction(Shared.ACTION_FCM_DIRECT_MSG_2);
        filter.addAction(Shared.ACTION_FCM_INDIRECT_MSG);
        filter.addAction(Shared.ACTION_FCM_INDIRECT_MSG_2);
        filter.addAction(Shared.ACTION_FCM_TOKEN_ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(this.FCMBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.FCMBroadcastReceiver);
    }

    private void handlePregnancyNotification() {
        final String today = Common.replaceNull(Common.WinAppDateFormat.format(Calendar.getInstance().getTime()), "");
        String date = Common.replaceNull(Shared.getString(this, Shared.KEY_PREGNANCY_NOTIFICATION_DATE), "");
        if (today.compareToIgnoreCase(date) != 0) {
            BaseUserReqArgs args = new BaseUserReqArgs();
            args.EmailID = Shared.getString(this, Shared.KEY_EMAIL_ID);
            Common.invokeAPI(this, ServiceMethods.GetPregnantNotifications, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                        ApiResult result = (ApiResult) msg.obj;
                        if (result != null && TextUtils.isEmpty(result.Json) == false) {
                            PregnancyNotificationResult data = new Gson().fromJson(result.Json, PregnancyNotificationResult.class);
                            if (data != null && TextUtils.isEmpty(data.AllowNotification) == false && data.AllowNotification.compareToIgnoreCase("Yes") == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showPregnancyNotification();
                                    }
                                });
                            }
                        }
                    }
                    return false;
                }
            }));
        }
    }

    private void showPregnancyNotification() {
        Notify.show(this, "Are you pregnant?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    handleYesButtonClick();
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    handleNoButtonClick();
                }
            }
        }, "Yes", "No");
    }

    private void handleYesButtonClick() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Notify.show(WINFertilityActivity.this, "Do you wish to turn off all further notifications from the App?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveNotificationStatus("Yes", (which == DialogInterface.BUTTON_POSITIVE ? "No" : ""));
                            }
                        }, "Yes", "No");
                    }
                }, 100);
            }
        });
    }

    private void handleNoButtonClick() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveNotificationStatus("No", "");
                    }
                }, 100);
            }
        });
    }

    private void handleServiceResult(final String pregnancyStatus, final String notificationStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String today = Common.replaceNull(Common.WinAppDateFormat.format(Calendar.getInstance().getTime()), "");
                Shared.setString(WINFertilityActivity.this, Shared.KEY_PREGNANCY_NOTIFICATION_DATE, today);
                if (pregnancyStatus.compareToIgnoreCase("No") == 0) {
                    Notify.show(WINFertilityActivity.this, "If you would like to speak with a WINFertility nurse, then please call WINFertility at:" +
                            Shared.getString(WINFertilityActivity.this, Shared.KEY_PHONE));
                }
                if (TextUtils.isEmpty(notificationStatus) == false) {
                    Shared.setString(WINFertilityActivity.this, Shared.KEY_NOTIFICATION_ENABLED, notificationStatus);
                    if (WINFertilityActivity.this instanceof SettingsActivity) {
                        ToggleButton tb_notification = (ToggleButton) WINFertilityActivity.this.findViewById(R.id.tb_notification);
                        if (tb_notification != null) {
                            tb_notification.setChecked(false);
                        }
                    }
                }
            }
        });
    }

    private void saveNotificationStatus(final String pregnancyStatus, final String notificationStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PregnancyStatusArgs args = new PregnancyStatusArgs();
                args.EmailID = Shared.getString(WINFertilityActivity.this, Shared.KEY_EMAIL_ID);
                args.NotificationStatus = notificationStatus;
                args.PregnancyStatus = pregnancyStatus;
                Loader.show(WINFertilityActivity.this);
                Common.invokeAPI(WINFertilityActivity.this, ServiceMethods.SavePregnantNotificationStatus, args, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        String error = null;
                        try {
                            if (msg.obj != null && msg.obj instanceof ApiResult) {
                                ApiResult response = (ApiResult) msg.obj;
                                if (TextUtils.isEmpty(response.Json) == false) {
                                    ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                    if (data != null && data.Result == 1) {
                                        handleServiceResult(pregnancyStatus, notificationStatus);
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
                        Notify.show(WINFertilityActivity.this, error);
                        return true;
                    }
                }));
            }
        });
    }
}
