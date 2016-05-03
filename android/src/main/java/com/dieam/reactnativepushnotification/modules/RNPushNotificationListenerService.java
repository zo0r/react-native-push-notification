package com.dieam.reactnativepushnotification.modules;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.BroadcastReceiver;

import java.util.List;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

public class RNPushNotificationListenerService extends GcmListenerService {

    private static final String ReceiveNotificationExtra  = "receiveNotifExtra";
    private static Boolean autoRestartReactActivity = false;

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
        intent.putExtra("notification", bundle);

        autoRestartReactActivity = Boolean.parseBoolean(bundle.getString("autoRestartReactActivity"));

        sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle result = getResultExtras(true);
                String status = result.getString(ReceiveNotificationExtra, "fail");
                if (status.equals("fail") && autoRestartReactActivity) {
                    restartReactActivity(intent);
                }
            }
        }, null, Activity.RESULT_OK, null, null);

        if (!isRunning) {
            new RNPushNotificationHelper(getApplication(), this).sendNotification(bundle);
        }
    }

    private boolean isApplicationRunning() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.processName.equals(getApplication().getPackageName())) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String d: processInfo.pkgList) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void restartReactActivity(Intent receiveBroadcastIntent) {
        Class mActivityClass = new RNPushNotificationHelper(getApplication(), this).getMainActivityClass();
        Intent intent = new Intent(this, mActivityClass);
        intent.putExtra("notification", receiveBroadcastIntent.getBundleExtra("notification"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
