package com.example.teju.biker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.teju.biker.R;
import com.example.teju.biker.Utils.Constants;

/**
 * Created by nz160 on 20-09-2017.
 */

public class BookingSuccessful extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_success);
      //  Constants.statusColor(this);

    }

    public void home(View view){
        Intent i =new Intent(this,MainActivity.class);
        startActivity(i);
    }
}
