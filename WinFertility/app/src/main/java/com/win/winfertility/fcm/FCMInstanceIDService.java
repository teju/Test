package com.win.winfertility.fcm;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.win.winfertility.utils.Shared;

public class FCMInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FCMInstanceIDService";

    @Override
    public void onTokenRefresh() {
        String newTokenID = FirebaseInstanceId.getInstance().getToken();
        System.out.println("MyFirebaseIIDService1234566 "+newTokenID);
        this.updateFCMTokenToServer(newTokenID);
    }

    private void updateFCMTokenToServer(String token) {
        try {
            Shared.setString(this, Shared.KEY_TOKEN_ID, token);
            Intent intent = new Intent(Shared.ACTION_FCM_TOKEN_ID);
            intent.putExtra(Shared.EXTRA_TOKEN_ID, token);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception ex) {
        }
    }
}
