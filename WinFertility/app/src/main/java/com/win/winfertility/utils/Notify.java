package com.win.winfertility.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.win.winfertility.R;

public class Notify {
    private static Dialog AlertDialog;
    public static void show(final Context context, final String message, final DialogInterface.OnClickListener callback, final String positive, final String negative) {
        Notify.show(context, message, callback, positive, negative, false);
    }
    public static void show(final Context context, final String message, final DialogInterface.OnClickListener
            callback, final String positive, final String negative, final boolean showExitIcon) {
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(Notify.AlertDialog != null) {
                    Notify.AlertDialog.dismiss();
                    Notify.AlertDialog = null;
        }

                Notify.AlertDialog = new Dialog(context);
                Notify.AlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                Notify.AlertDialog.setCancelable(false);
                Notify.AlertDialog.setContentView(R.layout.view_alert_dialog);

                TextView vw_text = (TextView) Notify.AlertDialog.findViewById(R.id.vw_text);
                if(vw_text != null) {
                    vw_text.setText(message);
                }

                Button btn_positive = (Button) Notify.AlertDialog.findViewById(R.id.btn_positive);
                if(btn_positive != null) {
                    btn_positive.setText((positive != null && positive.trim().length() > 0) ? positive : "Ok");
                    btn_positive.setVisibility(View.VISIBLE);
                    btn_positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Notify.hide();
                            if(callback != null) {
                                callback.onClick(Notify.AlertDialog, DialogInterface.BUTTON_POSITIVE);
                            }
                        }
                    });
                }

                Button btn_negative = (Button) Notify.AlertDialog.findViewById(R.id.btn_negative);
                if(btn_negative != null) {
                    if(negative != null && negative.trim().length() > 0) {
                        btn_negative.setText(negative);
                        btn_negative.setVisibility(View.VISIBLE);
                        btn_negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Notify.hide();
                                if(callback != null) {
                                    callback.onClick(Notify.AlertDialog, DialogInterface.BUTTON_NEGATIVE);
                                }
                            }
                        });
                    }
                    else {
                        btn_negative.setVisibility(View.GONE);
                    }
                }

                View btn_close = Notify.AlertDialog.findViewById(R.id.btn_close);
                if(btn_close != null) {
                    btn_close.setVisibility(showExitIcon ? View.VISIBLE : View.GONE);
                    if(showExitIcon) {
                        btn_close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Notify.hide();
                                if(callback != null) {
                                    callback.onClick(Notify.AlertDialog, DialogInterface.BUTTON_NEUTRAL);
                                }
                            }
                        });
                    }
                }

                Notify.AlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                Notify.AlertDialog.show();
                Notify.AlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });
    }
    public static void show(Context context, String message, DialogInterface.OnClickListener callback, String positive) {
        Notify.show(context, message, callback, positive, null, false);
    }
    public static void show(Context context, String message, DialogInterface.OnClickListener callback) {
        Notify.show(context, message, callback, null, null, false);
    }
    public static void show(Context context, String message) {
        Notify.show(context, message, null, null, null, false);
    }
    public static void hide() {
        if(Notify.AlertDialog != null) {
            new Handler(Notify.AlertDialog.getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Notify.AlertDialog.dismiss();
                    Notify.AlertDialog = null;
                }
            });
        }
    }
}
