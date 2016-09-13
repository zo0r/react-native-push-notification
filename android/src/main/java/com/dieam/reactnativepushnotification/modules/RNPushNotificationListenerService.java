package com.dieam.reactnativepushnotification.modules;

import android.content.Intent;
import android.os.Bundle;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.util.Log;

import java.util.List;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

public class RNPushNotificationListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        Log.d("RN", "onMessageReceived");

        JSONObject data = getPushData(bundle.getString("data"));
        if (data != null) {
            if (!bundle.containsKey("message")) {
                bundle.putString("message", data.optString("alert", "Notification received"));
            }
            if (!bundle.containsKey("title")) {
                bundle.putString("title", data.optString("title", null));
            }
        }

        sendNotification(bundle);
    }

    private JSONObject getPushData(String dataString) {
        try {
            return new JSONObject(dataString);
        } catch (Exception e) {
            return null;
        }
    }

    private void sendNotification(Bundle bundle) {

        Boolean isRunning = isApplicationRunning();

        Intent intent = new Intent("RNPushNotificationReceiveNotification");
        bundle.putBoolean("foreground", isRunning);
        bundle.putBoolean("userInteraction", false);
        intent.putExtra("notification", bundle);

        if (!RNPushNotificationQueue.getInstance().isLoaded()) {
            Log.d("RN", "Pushing new notification");
        //    RNPushNotificationQueue.getInstance().push(intent);
        } else {
            Log.d("RN", "sendBroadcast" + intent.toString());
            sendBroadcast(intent);
        }

        if (!isRunning) {
            new RNPushNotificationHelper(getApplication()).sendNotification(bundle);
        }
    }

    private boolean isApplicationRunning() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.processName.equals(getApplication().getPackageName())) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String d : processInfo.pkgList) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
