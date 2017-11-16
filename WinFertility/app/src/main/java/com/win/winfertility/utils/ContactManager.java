package com.win.winfertility.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.win.winfertility.EmailActivity;
import com.win.winfertility.R;

public class ContactManager {
    public static void Init(final Activity activity) {
        if(activity != null) {
            final RichTextInputManager dialog = new RichTextInputManager(activity);
            dialog.init(null);
            View.OnTouchListener menuOnTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        view.setBackgroundColor(Color.parseColor("#3080A6DE"));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        view.setBackgroundColor(Color.parseColor("#00000000"));
                    }
                    return false;
                }
            };

            View vw_call_contact = activity.findViewById(R.id.vw_call_contact);
            if(vw_call_contact != null) {
                vw_call_contact.setOnTouchListener(menuOnTouchListener);
                vw_call_contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phone = Shared.getString(activity, Shared.KEY_PHONE);
                        if(TextUtils.isEmpty(phone) == false) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + phone));
                            activity.startActivity(intent);
                        }
                        else {
                            Notify.show(activity, "Sorry, Phone number not available.");
                        }
                    }
                });
            }
            View vw_email_contact = activity.findViewById(R.id.vw_email_contact);
            if(vw_email_contact != null) {
                vw_email_contact.setOnTouchListener(menuOnTouchListener);
                vw_email_contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.startActivity(new Intent(activity, EmailActivity.class));
                    }
                });
            }
            View vw_text_contact = activity.findViewById(R.id.vw_text_contact);
            if(vw_text_contact != null) {
                vw_text_contact.setOnTouchListener(menuOnTouchListener);
                vw_text_contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.setTitle("Text Message");
                        dialog.showDialog(false);
                    }
                });
            }
        }
    }
}
