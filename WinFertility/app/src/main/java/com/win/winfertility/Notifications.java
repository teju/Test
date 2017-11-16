package com.win.winfertility;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.BaseUserReqArgs;
import com.win.winfertility.dataobjects.NotificationRequiredStatus;
import com.win.winfertility.dataobjects.NotificationStatusArgs;
import com.win.winfertility.dataobjects.NotificationsResult;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.AppMsg;
import com.win.winfertility.utils.Common;
import com.win.winfertility.utils.EmployerContactManager;
import com.win.winfertility.utils.GraphManager;
import com.win.winfertility.utils.Loader;
import com.win.winfertility.utils.NotificationManager;
import com.win.winfertility.utils.Notify;
import com.win.winfertility.utils.RichTextInputManager;
import com.win.winfertility.utils.ServiceMethods;
import com.win.winfertility.utils.Shared;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Notifications extends WINFertilityActivity implements View.OnClickListener{
    public static Notifications Instance;

    private Notifications Pointer;
    private ToggleButton _tb_notification;
    private NotificationManager _notificationManager;
    private boolean status=true;
    private ListView notification_list;
    private Button dismiss;
    private GraphManager _graphManager;
    private RichTextInputManager _feedbackManager;
    private EmployerContactManager _employerContactManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Notifications.Instance = this;

        setContentView(R.layout.activity_notifications);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        Pointer._tb_notification = (ToggleButton) this.findViewById(R.id.tb_notification);
        Pointer.notification_list = (ListView) this.findViewById(R.id.notification_list);
        Pointer.dismiss = (Button) this.findViewById(R.id.dismiss);

        Pointer._graphManager = new GraphManager(this);
        Pointer._graphManager.init(notification_list);

        Pointer._feedbackManager = new RichTextInputManager(this);
        Pointer._feedbackManager.init(notification_list);

        Pointer._employerContactManager = new EmployerContactManager(this);
        Pointer._employerContactManager.init(notification_list);

        getNotification();

        if(Pointer._tb_notification != null) {
            String status = Common.replaceNull(Shared.getString(Pointer, Shared.KEY_NOTIFICATION_ENABLED));
            Pointer._tb_notification.setChecked(status.compareToIgnoreCase("Yes") == 0);
            System.out.println("NotificationManager1233 init "+status);
        }
        if(Pointer.dismiss != null) {
          Pointer.dismiss.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Notifications.super.onBackPressed();
              }
          });
        }
        Pointer._notificationManager = new NotificationManager(this);
        Pointer._notificationManager.init();
        Pointer._tb_notification.setOnClickListener(this);
    }

    private void handleNotificationStatusChange() {
        String status_val = Common.replaceNull(Shared.getString(Pointer, Shared.KEY_NOTIFICATION_ENABLED));
        if(status_val.equalsIgnoreCase("Yes")) {
            status =true;
        } else {
            status =false;
        }
        System.out.println("NotificationManager1233 handleNotificationStatusChange "+status);

        Notify.show(Pointer, "Do you want to " + (status ? "Turn Off" : "Turn On") + " notifications?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE) {
                    if (status) {
                        Pointer.handleNotificationOffMethod();
                    } else {
                        Pointer.changeNotificationStatus(true);
                    }
                }
            }
        }, (status ? "Turn Off" : "Turn On"), "No Thanks");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void handleNotificationOffMethod() {
        Pointer.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Loader.show(Pointer);
                BaseUserReqArgs args = new BaseUserReqArgs();
                args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
                Common.invokeAPI(Pointer, ServiceMethods.NotificationDialogStatus, args, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        String error = "";
                        try {
                            if (msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                                ApiResult result = (ApiResult) msg.obj;
                                if (result != null && TextUtils.isEmpty(result.Json) == false) {
                                    NotificationRequiredStatus data = new Gson().fromJson(result.Json, NotificationRequiredStatus.class);
                                    if (data != null) {
                                        System.out.println("NotificationManager1233 handleNotificationOffMethod "+data.AllowFeedback);

                                        if (TextUtils.isEmpty(data.AllowFeedback) == false &&
                                                data.AllowFeedback.compareToIgnoreCase("Yes") == 0) {
                                            Pointer._notificationManager.init();
                                            Pointer._notificationManager.showDialog();
                                            //Pointer.changeNotificationStatus(false);
                                        } else {
                                            System.out.println("NotificationManager1233 handleNotificationOffMethod ");
                                            Pointer.changeNotificationStatus(false);
                                        }
                                        return true;
                                    }
                                    error = result.Error;
                                }
                            }
                        } catch (Exception ex) {
                        }
                        finally {
                            Loader.hide();
                        }
                        if(TextUtils.isEmpty(error)) {
                            error = AppMsg.MSG_FAILED;
                        }
                        Notify.show(Pointer, error);
                        return false;
                    }
                }));
            }
        });
    }

    public void refreshNotificationStatus(final boolean status) {
        Pointer.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Shared.setString(Pointer, Shared.KEY_NOTIFICATION_ENABLED, (status ? "Yes" : "No"));
                System.out.println("refreshNotificationStatus ststus "+status);
                Pointer._tb_notification.setChecked(status);
            }
        });
    }

    private void changeNotificationStatus(final boolean status) {
        Pointer.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NotificationStatusArgs args = new NotificationStatusArgs();
                        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
                        System.out.println("NotificationManager1233 changeNotificationStatus "+status);

                        args.NotificationStatus = (status ? "Yes" : "No");
                        Loader.show(Pointer);
                        Common.invokeAPI(Pointer, ServiceMethods.SaveNotificationStatus, args, new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                String error = "";
                                try {
                                    if(msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                                        ApiResult result = (ApiResult) msg.obj;
                                        if(result != null && TextUtils.isEmpty(result.Json) == false) {
                                            ApiReqResult data = new Gson().fromJson(result.Json, ApiReqResult.class);
                                            if(data != null && data.Result == 1) {
                                                Pointer.refreshNotificationStatus(status);
                                                return true;
                                            }
                                        }
                                    }
                                }
                                catch(Exception ex) {
                                }
                                finally {
                                    Loader.hide();
                                }
                                if(TextUtils.isEmpty(error)) {
                                    error = AppMsg.MSG_FAILED;
                                }
                                Notify.show(Pointer, error);
                                return false;
                            }
                        }));
                    }
                }, 100);
            }
        });
    }

    private void getNotification() {
        Pointer.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NotificationStatusArgs args = new NotificationStatusArgs();
                        args.EmailID = Shared.getString(Pointer, Shared.KEY_EMAIL_ID);
                        System.out.println("NotificationManager1233 changeNotificationStatus "+status);

                        Loader.show(Pointer);
                        Common.invokeAPI(Pointer, ServiceMethods.LoadNotificationsList, args, new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                String error = "";
                                try {
                                    if(msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                                        ApiResult result = (ApiResult) msg.obj;
                                        if(result != null && TextUtils.isEmpty(result.Json) == false) {
                                            System.out.println("NOTIFICATIONSRESPONSE "+result.Json);
                                            ResponseOfNotifications(result.Json);
                                           // ApiReqResult data = new Gson().fromJson(result.Json, ApiReqResult.class);
                                            /*if(data != null && data.Result == 1) {
                                                Pointer.refreshNotificationStatus(status);
                                                return true;
                                            }*/
                                        }
                                    }
                                }
                                catch(Exception ex) {
                                }
                                finally {
                                    Loader.hide();
                                }
                                System.out.println("NOTIFICATIONSRESPONSE isEmpty error "+error);

                                if(TextUtils.isEmpty(error)) {
                                    error = AppMsg.MSG_FAILED;
                                }
                               // Notify.show(Pointer, "Y");
                                return false;
                            }
                        }));
                    }
                }, 100);
            }
        });
    }

    public  void ResponseOfNotifications(String result){
        List<NotificationsResult> notification=new ArrayList<>();
        try {
            JSONArray json=new JSONArray(result);
            for(int i=0;i<json.length();i++) {
                JSONObject jObj=json.getJSONObject(i);
                NotificationsResult noti = new NotificationsResult();
                noti.setNotificationHeader(jObj.getString("NotificationHeader"));
                noti.setNotificationBody(jObj.getString("NotificationBody"));
                noti.setNotificationDate(jObj.getString("NotificationDate"));
                noti.setRedirectScreen(jObj.getString("RedirectScreen"));
                noti.setRedirect(jObj.getString("AndroidRedirect"));
                noti.setRedirect_screen(jObj.getString("AndroidBoard"));
                notification.add(noti);
            }
            System.out.println("NOTIFICATIONSRESPONSE ResponseOfNotifications "+notification.size());
            if(notification_list != null) {
                notification_list.setAdapter(new NotificationsAdapter(this,notification));
            }
        } catch (Exception e){
            System.out.println("NOTIFICATIONSRESPONSE Exception "+e.toString());
        }

    }
    @Override
    public void onClick(View view) {
        Pointer.handleNotificationStatusChange();
    }

    public class NotificationsAdapter extends BaseAdapter {

        private final Context context;
        private List<NotificationsResult> notification=new ArrayList<>();
        private LayoutInflater _inflater;

        public NotificationsAdapter(Context context,List<NotificationsResult> notification) {
            this.context=context;
            this.notification=notification;

        }
        @Override
        public int getCount() {
            return notification.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                if (this._inflater == null) {
                    this._inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                }
                view = this._inflater.inflate(R.layout.notification_items, null);
            }
            TextView noti_desc = (TextView) view.findViewById(R.id.noti_desc);
            TextView learn_more = (TextView) view.findViewById(R.id.learn_more);
            if(notification.get(i).getRedirectScreen().length() != 0) {
                learn_more.setText("Go To " + notification.get(i).getRedirectScreen());
            }

            noti_desc.setText(notification.get(i).getNotificationBody());
            String clsName = "com.win.winfertility." + notification.get(i).getRedirect();

            // use fully qualified name
            Class cls = null;
            try {
                cls = Class.forName(clsName);
            } catch (ClassNotFoundException e) {
                System.out.println("ClassNotFoundExceptionException " + e.toString());
                e.printStackTrace();
            }

            final Class finalCls = cls;
            learn_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(notification.get(i).getRedirect().equals("MyFertilityGraph")) {
                        Pointer._graphManager.showDialog();
                    } else if(notification.get(i).getRedirect().equals("ContactHelp")) {
                        Pointer._employerContactManager.showDialog();
                    } else if(notification.get(i).getRedirect().equals("ProvideFeedback")) {
                        Pointer._feedbackManager.showDialog(true);
                    } else if(notification.get(i).getRedirect().equals("MenstrualInfoActivity")) {
                        Intent intent = new Intent(Pointer, MenstrualInfoActivity.class);
                        intent.putExtra(Shared.EXTRA_PARENT_CLASS, MyFertilityActivity.class.getName());
                        Pointer.startActivity(intent);
                    } else if(finalCls != null) {
                        Intent intent = new Intent(context, finalCls);
                        if (notification.get(i).getRedirect().equals("BrowserActivity")) {
                            if (notification.get(i).getRedirect_screen().equals("FertilityEducation")) {
                                intent.putExtra("URL", Shared.KEY_FERTILITY_EDU_URL);
                            } else {
                                intent.putExtra("URL", Shared.KEY_BENEFITS_OVERVIEW_URL);
                            }
                        } else if (notification.get(i).getRedirect().equals("MenstrualInfoActivity")) {
                            intent.putExtra(Shared.EXTRA_PARENT_CLASS, SettingsActivity.class.getName());
                        }
                        startActivity(intent);
                    }

                }
            });
            return view;
        }
    }
}
