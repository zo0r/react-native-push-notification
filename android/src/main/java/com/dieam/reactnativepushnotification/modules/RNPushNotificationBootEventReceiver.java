package com.dieam.reactnativepushnotification.modules;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Set;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

/**
 * Set alarms for scheduled notification after system reboot.
 */
public class RNPushNotificationBootEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("RNPushNotification", "RNPushNotificationBootEventReceiver: Setting system alarms");

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(RNPushNotificationHelper.PREFERENCES_KEY, Context.MODE_PRIVATE);
            Set<String> ids = sharedPreferences.getAll().keySet();
            RNPushNotificationHelper rnPushNotificationHelper = new RNPushNotificationHelper((Application)context.getApplicationContext());

            for (String id: ids) {
                try {
                    String notificationAttributesJson = sharedPreferences.getString(id, null);
                    if(notificationAttributesJson != null) {
                        RNPushNotificationAttributes notificationAttributes = new RNPushNotificationAttributes();
                        notificationAttributes.fromJson(new JSONObject(notificationAttributesJson));

                        if(notificationAttributes.getFireDate() < System.currentTimeMillis()) {
                            Log.i("RNPushNotification", "RNPushNotificationBootEventReceiver: Showing notification for " +
                                    notificationAttributes.getId());
                            rnPushNotificationHelper.sendNotification(notificationAttributes.toBundle());
                        } else {
                            Log.i("RNPushNotification", "RNPushNotificationBootEventReceiver: Scheduling notification for " +
                                    notificationAttributes.getId());
                            rnPushNotificationHelper.sendNotificationScheduledCore(notificationAttributes.toBundle());
                        }
                    }
                } catch (Exception e) {
                    Log.e("RNPushNotification", "SystemBootEventReceiver: onReceive Error: " + e.getMessage());
                }
            }
        }
    }
}
