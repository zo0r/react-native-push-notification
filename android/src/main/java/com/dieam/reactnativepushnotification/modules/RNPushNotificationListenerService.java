package com.dieam.reactnativepushnotification.modules;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dieam.reactnativepushnotification.helpers.ApplicationBadgeHelper;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import java.util.List;
import java.util.Random;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        JSONObject data = getPushData(bundle.getString("data"));
        if (data != null) {
            if (!bundle.containsKey("message")) {
                bundle.putString("message", data.optString("alert", "Notification received"));
            }
            if (!bundle.containsKey("title")) {
                bundle.putString("title", data.optString("title", null));
            }

            final int badge = data.optInt("badge", -1);
            if (badge >= 0) {
                ApplicationBadgeHelper.INSTANCE.setApplicationIconBadgeNumber(this, badge);
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

        // If notification ID is not provided by the user for push notification, generate one at random
        if (bundle.getString("id") == null) {
            Random randomNumberGenerator = new Random(System.currentTimeMillis());
            bundle.putString("id", String.valueOf(randomNumberGenerator.nextInt()));
        }

        Boolean isRunning = isApplicationRunning();

        Intent intent = new Intent(this.getPackageName() + ".RNPushNotificationReceiveNotification");
        bundle.putBoolean("foreground", isRunning);
        bundle.putBoolean("userInteraction", false);
        intent.putExtra("notification", bundle);
        sendBroadcast(intent);

        // If contentAvailable is set to true, then send out a remote fetch event
        if (bundle.getString("contentAvailable", "false").equalsIgnoreCase("true")) {
            Log.d(LOG_TAG, "Received a notification with remote fetch enabled");
            Intent remoteFetchIntent = new Intent(this.getPackageName() + ".RNPushNotificationRemoteFetch");
            remoteFetchIntent.putExtra("notification", bundle);
            sendBroadcast(remoteFetchIntent);
        }

        if (!isRunning) {
            new RNPushNotificationHelper(getApplication()).sendNotification(bundle);
        }
    }

    private boolean isApplicationRunning() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.processName.equals(getApplication().getPackageName())) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String d : processInfo.pkgList) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
