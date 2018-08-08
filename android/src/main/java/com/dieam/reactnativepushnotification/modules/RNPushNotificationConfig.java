package com.dieam.reactnativepushnotification.modules;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

class RNPushNotificationConfig {
    private static final String KEY_CHANNEL_NAME = "com.dieam.reactnativepushnotification.notification_channel_name";
    private static final String KEY_CHANNEL_DESCRIPTION = "com.dieam.reactnativepushnotification.notification_channel_description";

    private Bundle metadata;

    public RNPushNotificationConfig(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            metadata = applicationInfo.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(RNPushNotification.LOG_TAG, "Error reading application meta, falling back to defaults");
            metadata = new Bundle();
        }
    }

    public String getChannelName() {
        try {
            return metadata.getString(KEY_CHANNEL_NAME);
        } catch (Exception e) {
            Log.e(RNPushNotification.LOG_TAG, "Unable to find " + KEY_CHANNEL_NAME + " in manifest. Falling back to default");
        }
        // Default
        return "rn-push-notification-channel";
    }
    public String getChannelDescription() {
        try {
            return metadata.getString(KEY_CHANNEL_DESCRIPTION);
        } catch (Exception e) {
            Log.e(RNPushNotification.LOG_TAG, "Unable to find " + KEY_CHANNEL_DESCRIPTION + " in manifest. Falling back to default");
        }
        // Default
        return "";
    }
}
