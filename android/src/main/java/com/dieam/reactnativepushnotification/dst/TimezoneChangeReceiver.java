package com.dieam.reactnativepushnotification.dst;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dieam.reactnativepushnotification.modules.RNPushNotificationHelper;

public class TimezoneChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(Intent.ACTION_TIME_CHANGED)) {
            Log.e("TIMEZONE CHANGED", "########## ACTION_TIMEZONE_CHANGED ###############");
            //resetting the broadcast if a timezone change occurred.
            RNPushNotificationHelper helper = new RNPushNotificationHelper(context);
            helper.cancelNotification(helper.getCurrentNotificationId());
            new DSTManager().scheduleDSTTransitionAlarms(context);
        }
    }
}