package com.example.teju.biker;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.teju.biker.Utils.ConnectivityReceiver;

public class SplashScreen extends AppCompatActivity {
    private SharedPreferences.Editor editor;
    private SharedPreferences prefrence;
    static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    ConnectivityReceiver Conn=new ConnectivityReceiver();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        IntentFilter filter = new IntentFilter(ACTION);
        this.registerReceiver(Conn, filter);
        CountDownTimer c = new CountDownTimer(2000, 1000) {
            public void onFinish() {
                //Display activity_no internet xml
                if(prefrence.getString("isLoggedIn", "").equals("true")) {
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(SplashScreen.this, Login.class);
                    startActivity(i);
                    finish();
                }
            }
            public void onTick(long millisUntilFinished) {
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(Conn);
    }
}
