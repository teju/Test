package com.bikerservice.biker.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bikerservice.biker.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by nz160 on 08-06-2017.
 */

public class    FCMInstancdIdService  extends FirebaseInstanceIdService{
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String resent_token= FirebaseInstanceId.getInstance().getToken();
        PrintClass.printValue("FCMInstancdIdService ","resent_token "+resent_token);
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences
                (getString(R.string.fcm_pref), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(getString(R.string.fcm_token),resent_token);
        editor.commit();
    }

}
