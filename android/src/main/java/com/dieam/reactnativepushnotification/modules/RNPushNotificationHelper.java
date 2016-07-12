package com.dieam.reactnativepushnotification.modules;


import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class RNPushNotificationHelper {
    private static final long DEFAULT_VIBRATION = 1000L;
    private static final String TAG = RNPushNotificationHelper.class.getSimpleName();

    private Context mContext;

    public RNPushNotificationHelper(Application context) {
        mContext = context;
    }

    public Class getMainActivityClass() {
      String packageName = mContext.getPackageName();
      Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
      String className = launchIntent.getComponent().getClassName();
      try {
          return Class.forName(className);
      } catch (ClassNotFoundException e) {
          e.printStackTrace();
          return null;
      }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getScheduleNotificationIntent(Bundle bundle) {
        int notificationID;
        String notificationIDString = bundle.getString("id");

        if ( notificationIDString != null ) {
            notificationID = Integer.parseInt(notificationIDString);
        } else {
            notificationID = (int) System.currentTimeMillis();
        }

        Intent notificationIntent = new Intent(mContext, RNPushNotificationPublisher.class);
        notificationIntent.putExtra(RNPushNotificationPublisher.NOTIFICATION_ID, notificationID);
        notificationIntent.putExtras(bundle);

        return PendingIntent.getBroadcast(mContext, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void sendNotificationScheduled(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        Double fireDateDouble = bundle.getDouble("fireDate", 0);
        if (fireDateDouble == 0) {
            return;
        }

        long fireDate = Math.round(fireDateDouble);
        long currentTime = System.currentTimeMillis();

        Log.i("ReactSystemNotification", "fireDate: " + fireDate + ", Now Time: " + currentTime);
        PendingIntent pendingIntent = getScheduleNotificationIntent(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        } else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }
    }

    public void sendNotification(Bundle bundle) {
        try {// intentionally not formatted correctly to make the merge easier to understand

        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        if (bundle.getString("message") == null) {
            return;
        }

        Resources res = mContext.getResources();
        String packageName = mContext.getPackageName();

        String title = bundle.getString("title");
        if (title == null) {
            ApplicationInfo appInfo = mContext.getApplicationInfo();
            title = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setTicker(bundle.getString("ticker"))
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(bundle.getBoolean("autoCancel", true));

        String group = bundle.getString("group");
        if (group != null) {
            notification.setGroup(group);
        }

        notification.setContentText(bundle.getString("message"));

        String largeIcon = bundle.getString("largeIcon");

        String subText = bundle.getString("subText");

        if ( subText != null ) {
            notification.setSubText(subText);
        }

        if (bundle.containsKey("number")) {
            String number = bundle.getString("number");

            if (number != null) {
                Log.w(TAG, "'number' field set as a string instead of an int");
                notification.setNumber(Integer.parseInt(number));
            }

            notification.setNumber((int) bundle.getDouble("number"));
        }

        int smallIconResId;
        int largeIconResId;

        String smallIcon = bundle.getString("smallIcon");

        if ( smallIcon != null ) {
            smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
        } else {
            smallIconResId = res.getIdentifier("ic_notification", "mipmap", packageName);
        }

        if ( smallIconResId == 0 ) {
            smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);

            if ( smallIconResId == 0 ) {
                smallIconResId  = android.R.drawable.ic_dialog_info;
            }
        }

        if ( largeIcon != null ) {
            largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
        } else {
            largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);
        }

        Bitmap largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);

        if ( largeIconResId != 0 && ( largeIcon != null || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ) ) {
            notification.setLargeIcon(largeIconBitmap);
        }

        notification.setSmallIcon(smallIconResId);
        String bigText = bundle.getString("bigText");

        if (bigText == null ) {
            bigText = bundle.getString("message");
        }

        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));

        Intent intent = new Intent(mContext, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notification", bundle);

        if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notification.setSound(defaultSoundUri);
        }

        if ( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            notification.setCategory(NotificationCompat.CATEGORY_CALL);

            String color = bundle.getString("color");
            if (color != null) {
                notification.setColor(Color.parseColor(color));
            }
        }

        int notificationID = (int) System.currentTimeMillis();
        if (bundle.containsKey("id")) {
            String notificationIDString = bundle.getString("id");
            if (notificationIDString != null) {
                Log.w(TAG, "'id' field set as a string instead of an int");

                try {
                    notificationID = Integer.parseInt(notificationIDString);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "'id' field could not be converted to an int, ignoring it", e);
                }
            } else {
                notificationID = (int) bundle.getDouble("id");
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notification.setContentIntent(pendingIntent);

        if (!bundle.containsKey("vibrate") || bundle.getBoolean("vibrate")) {
            long vibration = bundle.containsKey("vibration") ? (long) bundle.getDouble("vibration") : DEFAULT_VIBRATION;
            if (vibration == 0)
                vibration = DEFAULT_VIBRATION;
            notification.setVibrate(new long[]{0, vibration});
        }

        Notification info = notification.build();
        info.defaults |= Notification.DEFAULT_LIGHTS;

        if (bundle.containsKey("tag")) {
            String tag = bundle.getString("tag");
            notificationManager.notify(tag, notificationID, info);
        } else {
            notificationManager.notify(notificationID, info);
        }

        } catch (Exception e) {
            Log.e(TAG, "failed to send push notification", e);
        }
    }

    public void cancelAll() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        Bundle b = new Bundle();
        b.putString("id", "0");
        getAlarmManager().cancel(getScheduleNotificationIntent(b));
    }
}
