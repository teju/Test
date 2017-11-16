package com.win.winfertility.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.win.winfertility.R;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.SendGraphArgs;
import com.win.winfertility.dto.ApiResult;
import com.win.winfertility.dto.SelectDialogItem;

import java.util.ArrayList;
import java.util.List;

public class GraphManager {
    private Activity _activity;
    private GraphManager Pointer;

    private EditText _txt_email;
    private TextView _vw_data_to_include;
    private View _vw_graph_dialog;
    private View linear_list;

    public GraphManager(Activity activity) {
        this.Pointer = this;
        this._activity = activity;
    }

    public void init(final View view) {
        /*----- Graph Dialog Controls -----*/
        linear_list = view;
        Pointer._txt_email = (EditText) Pointer._activity.findViewById(R.id.txt_email);
       // linear_list = (ScrollView) Pointer._activity.findViewById(R.id.linear_list);

        Pointer._vw_graph_dialog = Pointer._activity.findViewById(R.id.vw_graph_dialog);
        if(Pointer._vw_graph_dialog != null) {
            Common.hideKeyboard(_activity);
            Pointer._vw_graph_dialog.setVisibility(View.GONE);
            if(linear_list !=null) {
                linear_list.setVisibility(View.VISIBLE);
            }
        }

        Pointer._vw_data_to_include = (TextView) Pointer._activity.findViewById(R.id.vw_data_to_include);
        if(Pointer._vw_data_to_include != null) {
            Pointer.initDataToIncludeControl(Pointer._vw_data_to_include);
            SelectBox.setData(Pointer._vw_data_to_include, new SelectDialogItem("6", "Last six months"));
        }

        View btn_email_me = Pointer._activity.findViewById(R.id.btn_email_me);
        if(btn_email_me != null) {
            btn_email_me.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pointer.sendEmailWithGraph();
                }
            });
        }

        View btn_close = Pointer._activity.findViewById(R.id.btn_close);
        if(btn_close != null) {
            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Pointer._vw_graph_dialog != null) {
                        Common.hideKeyboard(_activity);
                        Pointer._vw_graph_dialog.setVisibility(View.GONE);
                        if(linear_list !=null) {
                            linear_list.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }
    public void showDialog() {
        if(Pointer._vw_graph_dialog != null) {
            Pointer._vw_graph_dialog.setVisibility(View.VISIBLE);
            if(linear_list !=null) {
                linear_list.setVisibility(View.GONE);
            }
            if(Pointer._txt_email != null) {
                Pointer._txt_email.setText(Shared.getString(Pointer._activity, Shared.KEY_EMAIL_ID));
                Pointer._txt_email.requestFocus();
            }
        }
    }

    private void initDataToIncludeControl(TextView view) {
        try {
            List<SelectDialogItem> items = new ArrayList<>();
            items.add(new SelectDialogItem("1", "Last month only"));
            items.add(new SelectDialogItem("3", "Last three months"));
            items.add(new SelectDialogItem("6", "Last six months"));
            items.add(new SelectDialogItem("12", "Last year"));
            items.add(new SelectDialogItem("0", "All calendar activity"));
            SelectBox.config(view, items, "Duration");
        }
        catch(Exception ex) {
        }
    }
    private void sendEmailWithGraph() {
        if(Pointer._txt_email != null) {
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Pointer._vw_graph_dialog != null) {
                        Pointer._vw_graph_dialog.setVisibility(View.VISIBLE);
                        if(linear_list !=null) {
                            linear_list.setVisibility(View.GONE);
                        }
                    }
                    Pointer._txt_email.requestFocus();
                }
            };

            if (Pointer._vw_graph_dialog != null) {
                Common.hideKeyboard(_activity);
                Pointer._vw_graph_dialog.setVisibility(View.GONE);
                if(linear_list !=null) {
                    linear_list.setVisibility(View.VISIBLE);
                }
            }

            String email = Pointer._txt_email.getText().toString();
            if(TextUtils.isEmpty(email)) {
                Notify.show(Pointer._activity, "Please enter your email address.", onClickListener);
                return;
            }
            else if(Common.isValidEmail(email) == false) {
                Notify.show(Pointer._activity, "Please enter valid email address.", onClickListener);
                return;
            }

            Loader.show(Pointer._activity);
            SendGraphArgs args = new SendGraphArgs();
            args.EmailID = Shared.getString(Pointer._activity, Shared.KEY_EMAIL_ID);
            args.ToEmailID = email;
            args.Duration = SelectBox.getData(Pointer._vw_data_to_include).ID;
            Common.invokeAPI(Pointer._activity, ServiceMethods.SendMyGraphs, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String error = null;
                    try {
                        if(msg.obj != null && msg.obj instanceof ApiResult) {
                            ApiResult response = (ApiResult) msg.obj;
                            if(TextUtils.isEmpty(response.Json) == false) {
                                ApiReqResult data = new Gson().fromJson(Common.suppressJsonArray(response.Json), ApiReqResult.class);
                                if(data != null && data.Result == 1) {
                                    Notify.show(Pointer._activity, "We have received your request. You will shortly get a copy of the data to the email address provided to us.");
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
                            if (Pointer._vw_graph_dialog != null) {
                                Pointer._vw_graph_dialog.setVisibility(View.VISIBLE);
                                if(linear_list !=null) {
                                    linear_list.setVisibility(View.GONE);
                                }
                            }
                            Pointer._txt_email.requestFocus();
                        }
                    });
                    return true;
                }
            }));
        }
    }
}
