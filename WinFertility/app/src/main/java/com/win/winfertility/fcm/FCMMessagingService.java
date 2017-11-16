package com.win.winfertility.fcm;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.win.winfertility.utils.Shared;

public class FCMMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        System.out.println("FCMMessagingService12344 body : "
             +remoteMessage.getNotification().getBody()
                +" title : "+remoteMessage.getNotification().getTitle()
                +" icon : "+remoteMessage.getNotification().getIcon()+" "
                +"click action : "+remoteMessage.getNotification().getClickAction());
          if(remoteMessage != null) {
            String date = "";
            java.util.Map<java.lang.String,java.lang.String> values = remoteMessage.getData();
            for(String key : values.keySet()) {
                if(key.compareToIgnoreCase(Shared.FCM_PAYLOAD_DATA_KEY) == 0) {
                    date = values.get(key).toString();
                    System.out.println("FCMMessagingService12344 date :" +date);
                    break;
                }
            }
            if(TextUtils.isEmpty(date) == false) {
                Intent intent = new Intent(Shared.ACTION_FCM_DIRECT_MSG);
                intent.putExtra(Shared.EXTRA_NOTIFICATION_DATE, date);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else {
                Intent intent = new Intent(Shared.ACTION_FCM_DIRECT_MSG_2);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }

    }

}
