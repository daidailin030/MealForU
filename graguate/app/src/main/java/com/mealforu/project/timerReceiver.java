package com.mealforu.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

public class timerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("sender_name");
        Toast.makeText(context,"時間到",Toast.LENGTH_SHORT).show();
//        playDefaultNotification(context);

    }
//    public static void playDefaultNotification(Context context) {
//        Uri defaultNotificationURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//        if(defaultNotificationURI == null){
//            System.out.println("nooooooooooooooo");
//        }
//        Ringtone soundOfDefaultNotification = RingtoneManager.getRingtone(context, defaultNotificationURI);
//        soundOfDefaultNotification.play();
//    }
}
