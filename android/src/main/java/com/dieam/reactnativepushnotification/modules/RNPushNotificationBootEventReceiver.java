package com.dieam.reactnativepushnotification.modules;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Set;

import android.os.Bundle;
import android.util.Log;

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
            RNPushNotificationHelper rnPushNotificationHelper = new RNPushNotificationHelper((Application)context.getApplicationContext(), context);

            for (String id: ids) {
                try {
                    String bundleJson = sharedPreferences.getString(id, null);
                    if(bundleJson != null) {
                        Bundle bundle = RNPushNotification.converJSONToBundle(bundleJson);
                        rnPushNotificationHelper.sendNotificationScheduled(bundle);
                        Log.i("RNPushNotification", "RNPushNotificationBootEventReceiver: Alarm set for: " + bundle.getString("id"));
                    }
                } catch (Exception e) {
                    Log.e("ReactSystemNotification", "SystemBootEventReceiver: onReceive Error: " + e.getMessage());
                }
            }
        }
    }
}
