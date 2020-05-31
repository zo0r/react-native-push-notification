package com.dieam.reactnativepushnotification.modules;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.core.content.res.ResourcesCompat;
import android.os.Bundle;
import android.util.Log;

class RNPushNotificationConfig {
    private static final String KEY_CHANNEL_CREATE_DEFAULT = "com.dieam.reactnativepushnotification.channel_create_default";
    private static final String KEY_CHANNEL_NAME = "com.dieam.reactnativepushnotification.notification_channel_name";
    private static final String KEY_CHANNEL_DESCRIPTION = "com.dieam.reactnativepushnotification.notification_channel_description";
    private static final String KEY_NOTIFICATION_FOREGROUND = "com.dieam.reactnativepushnotification.notification_foreground";
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

    private String getStringValue(String key, String defaultValue) {
        try {
            final String value = metadata.getString(key);

            if (value != null && value.length() > 0) {
                return value;
            }
        } catch (Exception e) {
            Log.w(RNPushNotification.LOG_TAG, "Unable to find " + key + " in manifest. Falling back to default");
        }

        // Default
        return defaultValue;
    }

    public String getChannelName(String channel_id) {
        String overrided = this.getStringValue(KEY_CHANNEL_NAME, "rn-push-notification-channel");  

        return this.getStringValue(KEY_CHANNEL_NAME + "." + channel_id, overrided);
    }
    
    public String getChannelDescription(String channel_id) {
        String overrided = this.getStringValue(KEY_CHANNEL_DESCRIPTION, "");  
        
        return this.getStringValue(KEY_CHANNEL_DESCRIPTION + "." + channel_id, overrided);
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

    public boolean getNotificationForeground() {
        try {
            return metadata.getBoolean(KEY_NOTIFICATION_FOREGROUND, false);
        } catch (Exception e) {
            Log.w(RNPushNotification.LOG_TAG, "Unable to find " + KEY_NOTIFICATION_FOREGROUND + " in manifest. Falling back to default");
        }
        // Default
        return false;
    }

    public boolean getChannelCreateDefault() {
        try {
            return metadata.getBoolean(KEY_CHANNEL_CREATE_DEFAULT, true);
        } catch (Exception e) {
            Log.w(RNPushNotification.LOG_TAG, "Unable to find " + KEY_CHANNEL_CREATE_DEFAULT + " in manifest. Falling back to default");
        }
        // Default
        return true;
    }
}
