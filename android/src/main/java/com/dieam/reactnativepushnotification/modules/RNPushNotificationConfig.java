package com.dieam.reactnativepushnotification.modules;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.core.content.res.ResourcesCompat;
import android.os.Bundle;
import android.util.Log;

class RNPushNotificationConfig {
    private static final String KEY_CHANNEL_NAME = "com.dieam.reactnativepushnotification.notification_channel_name";
    private static final String KEY_CHANNEL_DESCRIPTION = "com.dieam.reactnativepushnotification.notification_channel_description";
    private static final String KEY_NOTIFICATION_COLOR = "com.dieam.reactnativepushnotification.notification_color";

    private static Bundle metadata;
    private Context context;

    public RNPushNotificationConfig(Context context) {
        this.context = context;
        if (metadata == null) {
            try {
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                metadata = applicationInfo.metaData;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.e(RNPushNotification.LOG_TAG, "Error reading application meta, falling back to defaults");
                metadata = new Bundle();
            }
        }
    }

    public String getChannelName() {
        try {
            return metadata.getString(KEY_CHANNEL_NAME);
        } catch (Exception e) {
            Log.w(RNPushNotification.LOG_TAG, "Unable to find " + KEY_CHANNEL_NAME + " in manifest. Falling back to default");
        }
        // Default
        return "rn-push-notification-channel";
    }
    public String getChannelDescription() {
        try {
            return metadata.getString(KEY_CHANNEL_DESCRIPTION);
        } catch (Exception e) {
            Log.w(RNPushNotification.LOG_TAG, "Unable to find " + KEY_CHANNEL_DESCRIPTION + " in manifest. Falling back to default");
        }
        // Default
        return "";
    }
    public int getNotificationColor() {
        try {
            int resourceId = metadata.getInt(KEY_NOTIFICATION_COLOR);
            return ResourcesCompat.getColor(context.getResources(), resourceId, null);
        } catch (Exception e) {
            Log.w(RNPushNotification.LOG_TAG, "Unable to find " + KEY_NOTIFICATION_COLOR + " in manifest. Falling back to default");
        }
        // Default
        return -1;
    }
}
