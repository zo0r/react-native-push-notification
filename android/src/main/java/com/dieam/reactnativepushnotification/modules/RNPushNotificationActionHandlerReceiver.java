package com.dieam.reactnativepushnotification.modules;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.HeadlessJsTaskService;

import java.util.List;

public class RNPushNotificationActionHandlerReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    
    if (intent == null || intent.getExtras() == null) {
      return;
    }
    Intent serviceIntent = new Intent(context, RNPushNotificationActionService.class);
    serviceIntent.putExtras(intent.getExtras());
    context.startService(serviceIntent);

     // Dismiss the notification popup.
    Bundle bundle = intent.getBundleExtra("notification");
    NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
    int notificationID = (int) Long.parseLong(bundle.getString("id"));
    manager.cancel(notificationID);

     if (!isAppOnForeground((context))) {
      HeadlessJsTaskService.acquireWakeLockNow(context);
    }
  }

  private boolean isAppOnForeground(Context context) {
    /**
     We need to check if app is in foreground otherwise the app will crash.
     http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
     **/
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcesses =
        activityManager.getRunningAppProcesses();
    if (appProcesses == null) {
      return false;
    }
    final String packageName = context.getPackageName();
    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
      if (appProcess.importance ==
          ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
          appProcess.processName.equals(packageName)) {
        return true;
      }
    }
    return false;
  }
}