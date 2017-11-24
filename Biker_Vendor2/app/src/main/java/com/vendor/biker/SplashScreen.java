package com.vendor.biker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;



public class SplashScreen extends AppCompatActivity {
    private SharedPreferences.Editor editor;
    private SharedPreferences prefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();

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
    }
}
