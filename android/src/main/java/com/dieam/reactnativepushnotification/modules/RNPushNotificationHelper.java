package com.dieam.reactnativepushnotification.modules;


import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class RNPushNotificationHelper {
    public static final String PREFERENCES_KEY = "RNPushNotification";
    private Application mApplication;
    private Context mContext;
    private final SharedPreferences mSharedPreferences;
    public RNPushNotificationHelper(Application application, Context context) {
        mApplication = application;
        mContext = context;
        mSharedPreferences = (SharedPreferences)context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
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
        return (AlarmManager) mApplication.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getScheduleNotificationIntent(Bundle bundle) {
        int notificationID;
        String notificationIDString = bundle.getString("id");

        if ( notificationIDString != null ) {
            notificationID = Integer.parseInt(notificationIDString);
        } else {
            notificationID = (int) System.currentTimeMillis();
            bundle.putString("id", Integer.toString(notificationID));
        }

        Intent notificationIntent = new Intent(mApplication, RNPushNotificationPublisher.class);
        notificationIntent.putExtra(RNPushNotificationPublisher.NOTIFICATION_ID, notificationID);
        notificationIntent.putExtras(bundle);

        return PendingIntent.getBroadcast(mApplication, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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

        String notificationId = bundle.getString("id");
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String bundleJson = RNPushNotification.convertBundleToJSON(bundle);
        editor.putString(notificationId, bundleJson);

        if (Build.VERSION.SDK_INT < 9) {
            editor.commit();
        } else {
            editor.apply();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        } else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }
    }

    public void sendNotification(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        if (bundle.getString("message") == null) {
            return;
        }

        Resources res = mApplication.getResources();
        String packageName = mApplication.getPackageName();

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

        notification.setContentText(bundle.getString("message"));

        String largeIcon = bundle.getString("largeIcon");

        String subText = bundle.getString("subText");

        if ( subText != null ) {
            notification.setSubText(subText);
        }

        String number = bundle.getString("number");

        if ( number != null ) {
            notification.setNumber(Integer.parseInt(number));
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

        String soundName = bundle.getString("sound");
        if(soundName != null) {
            Uri soundUri = null;
            
            if("default".equalsIgnoreCase(soundName)) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            } else {
                soundUri = Uri.parse(soundName);
            }
            notification.setSound(soundUri);
        }

        if ( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            notification.setCategory(NotificationCompat.CATEGORY_CALL);

            String color = bundle.getString("color");
            if (color != null) {
                notification.setColor(Color.parseColor(color));
            }
        }

        int notificationID;
        String notificationIDString = bundle.getString("id");

        if ( notificationIDString != null ) {
            notificationID = Integer.parseInt(notificationIDString);
        } else {
            notificationID = (int) System.currentTimeMillis();
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notification.setContentIntent(pendingIntent);

        Notification info = notification.build();
        info.defaults |= Notification.DEFAULT_VIBRATE;
        info.defaults |= Notification.DEFAULT_LIGHTS;

        if(mSharedPreferences.getString(Integer.toString(notificationID), null) != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(Integer.toString(notificationID));

            if (Build.VERSION.SDK_INT < 9) {
                editor.commit();
            } else {
                editor.apply();
            }
        }

        notificationManager.notify(notificationID, info);
    }

    public void cancelAll() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        Bundle b = new Bundle();
        b.putString("id", "0");
        getAlarmManager().cancel(getScheduleNotificationIntent(b));
    }

    public void cancelNotification(String notificationIDString) {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(Integer.parseInt(notificationIDString));

        Bundle b = new Bundle();
        b.putString("id", notificationIDString);
        getAlarmManager().cancel(getScheduleNotificationIntent(b));
    }
}
