package com.bikerservice.biker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bikerservice.biker.R;


public class SplashScreen extends AppCompatActivity {
    private SharedPreferences.Editor editor;
    private SharedPreferences prefrence;
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startActivity(getIntent());

    }
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
                    i.putExtra("reached_dest","false");
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