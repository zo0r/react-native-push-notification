package com.dieam.reactnativepushnotification.modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DeleteNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(RNPushNotificationHelper.DELETE_MESSAGE, false))
        {
            Bundle bundle = intent.getBundleExtra(RNPushNotificationHelper.NOTIFICATION_BUNDLE);
            if (bundle != null)
            {
                String dialog_id = bundle.getString("dialog_id");
                String message_id = bundle.getString("message_id");
                RNPushNotificationHelper.deleteMessage(dialog_id, message_id);
            }
        }
    }
}
