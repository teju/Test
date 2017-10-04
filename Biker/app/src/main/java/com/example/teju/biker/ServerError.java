package com.example.teju.biker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.teju.biker.Utils.CustomToast;

/**
 * Created by nz160 on 03-10-2017.
 */

public class ServerError extends AppCompatActivity{
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_error);
        rootView=findViewById(android.R.id.content);
    }

    public void retry(View view){
        int status = NetworkUtil.getConnectivityStatusString(this);
        if(status == 1) {
            super.onBackPressed();
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "Service Not Available");
        }
    }

}
