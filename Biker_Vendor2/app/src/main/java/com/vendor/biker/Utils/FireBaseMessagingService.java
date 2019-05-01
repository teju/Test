package com.vendor.biker.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vendor.biker.R;


/**
 * Created by nz160 on 08-06-2017.
 */

public class FireBaseMessagingService extends FirebaseMessagingService {

    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        editor.putString("isNotiReceived", "true");

        PrintClass.printValue("FireBaseMessagingService RESPONSE ",""+ remoteMessage.getNotification().getSound());
        Intent i=new Intent(remoteMessage.getNotification().getClickAction());
        i.putExtra("booking_id", remoteMessage.getData().get("booking_id"));

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_ONE_SHOT);
        Uri alarmSound = Uri.parse("android.resource://com.vendor.biker/raw/notification");

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setFullScreenIntent(pendingIntent, true);
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        builder.setPriority(NotificationCompat.PRIORITY_LOW); // Set priority to PRIORITY_LOW to mute notification sound
        builder.setSound(alarmSound);
        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setStyle((new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody())));
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.bierlogo);
        builder.setLargeIcon(largeIcon);
        builder.setSmallIcon(R.drawable.bierlogo);

        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());

       playNotificationSound();
    }

    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse("android.resource://com.vendor.biker/raw/notification");
            Ringtone r = RingtoneManager.getRingtone(this, alarmSound);
            r.play();
        } catch (Exception e) {
            System.out.println("playNotificationSound1212 "+e.toString());
            e.printStackTrace();
        }
    }

}
