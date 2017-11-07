package com.vendor.biker.Utils;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vendor.biker.JobList;
import com.vendor.biker.R;


/**
 * Created by nz160 on 08-06-2017.
 */

public class FireBaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        PrintClass.printValue("FireBaseMessagingService RESPONSE ",""+remoteMessage.getData().get("action"));
        Intent i=new Intent(remoteMessage.getNotification().getClickAction());
        i.putExtra("booking_id", remoteMessage.getData().get("booking_id"));

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setFullScreenIntent(pendingIntent, true);

        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setStyle((new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody())));
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.bierlogo);
        builder.setLargeIcon(largeIcon);
        builder.setSmallIcon(R.drawable.bierlogo);

        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());

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
