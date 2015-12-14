package com.dieam.reactnativepushnotification.modules;

import android.content.Intent;
import android.os.Bundle;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

import java.util.List;

import com.google.android.gms.gcm.GcmListenerService;

public class RNPushNotificationListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        sendNotification(bundle);
    }

    private void sendNotification(Bundle bundle) {

        if (isApplicationRunning()) {
            Intent intent = new Intent("RNPushNotificationReceiveNotification");
            bundle.putBoolean("foreground", true);
            intent.putExtra("notification", bundle);
            sendBroadcast(intent);
            return;
        }

        new RNPushNotificationHelper(getApplication(), this).sendNotification(bundle);
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
}