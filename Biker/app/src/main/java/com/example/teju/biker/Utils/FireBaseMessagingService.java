package com.example.teju.biker.Utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


/**
 * Created by nz160 on 08-06-2017.
 */

public class FireBaseMessagingService extends FirebaseMessagingService {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        PrintClass.printValue("FireBaseMessagingService RESPONSE ",""+remoteMessage.getNotification().getClickAction());
       /* String title=remoteMessage.getNotification().getTitle();
        String message=remoteMessage.getNotification().getClickAction();
        String body=(remoteMessage.getNotification().getBody());
        Intent i=new Intent(message);
        if(message.equals("com.fipl.netzealous_News_TARGET_NOTIFICATION")){
            i.putExtra("isfrom","news");
        }
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setContentTitle(title);
        builder.setStyle((new NotificationCompat.BigTextStyle().bigText(body)));
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_logo);
        builder.setLargeIcon(largeIcon);
        builder.setSmallIcon(R.drawable.navigation_logo);

        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());*/
    }
}
