package com.win.winfertility;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;

import com.win.winfertility.tools.WINFertilityActivity;
import com.win.winfertility.utils.Shared;

public class LandingActivity extends WINFertilityActivity {
    private LandingActivity Pointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_landing);
        this.Pointer = this;
        this.init();
    }

    private void init() {
        if(TextUtils.isEmpty(Shared.getString(Pointer, Shared.KEY_EMAIL_ID)) == true) {
            View btn_sign_in = this.findViewById(R.id.btn_sign_in);
            if(btn_sign_in != null) {
                btn_sign_in.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(LandingActivity.this, LoginActivity.class));
                    }
                });
            }

            View btn_create_account = this.findViewById(R.id.btn_create_account);
            if(btn_create_account != null) {
                btn_create_account.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(LandingActivity.this, AccountOptionActivity.class));
                    }
                });
            }
        }
        else {
            this.startActivity(new Intent(LandingActivity.this, HomeActivity.class));
            this.handleFCMData();
        }
    }
    public void handleFCMData() {
        try {
            String date = "";
            System.out.println("BROADLANDINF date "+"handleFCMData "+getIntent().getExtras());

            if (getIntent().getExtras() != null) {
                for (String key : getIntent().getExtras().keySet()) {
                    if(key.compareToIgnoreCase(Shared.FCM_PAYLOAD_DATA_KEY) == 0) {
                        date = getIntent().getExtras().get(key).toString();
                        break;
                    }
                }
            }
            String notificationBody = "";
            if (getIntent().getExtras() != null) {
                for (String key : getIntent().getExtras().keySet()) {
                    if(key.compareToIgnoreCase(Shared.FCM_PAYLOAD_DATA_KEY_body) == 0) {
                        notificationBody = getIntent().getExtras().get(key).toString();
                        break;
                    }
                }
            }
            System.out.println("BROADLANDINF date "+notificationBody);
            if(TextUtils.isEmpty(date) == false ) {
                final Intent intent = new Intent(Shared.ACTION_FCM_INDIRECT_MSG);
                intent.putExtra(Shared.EXTRA_NOTIFICATION_DATE, date);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LocalBroadcastManager.getInstance(LandingActivity.this).sendBroadcast(intent);
                    }
                }, 500);
            } else if(TextUtils.isEmpty(notificationBody) == false) {
                    final Intent intent = new Intent(Shared.ACTION_FCM_INDIRECT_MSG_2);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LocalBroadcastManager.getInstance(LandingActivity.this).sendBroadcast(intent);
                        }
                    }, 500);
                }

        } catch (Exception ex) {

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

}
