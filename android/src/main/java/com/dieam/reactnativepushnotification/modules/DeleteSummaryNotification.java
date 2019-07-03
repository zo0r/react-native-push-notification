package com.dieam.reactnativepushnotification.modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeleteSummaryNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        RNPushNotificationHelper.clearMessage();
        System.out.println("[Delete]");
    }
}
