package com.dieam.reactnativepushnotification.modules;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RNPushNotificationPublisher extends BroadcastReceiver {
    final static String NOTIFICATION_ID = "notificationId";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        long currentTime = System.currentTimeMillis();
        Log.i("ReactSystemNotification", "NotificationPublisher: Prepare To Publish: " + id + ", Now Time: " + currentTime);
        new RNPushNotificationHelper((Application) context.getApplicationContext()).sendNotification(intent.getExtras());
    }
}
