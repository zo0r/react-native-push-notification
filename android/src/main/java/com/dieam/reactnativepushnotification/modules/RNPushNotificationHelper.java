package com.dieam.reactnativepushnotification.modules;


import android.app.*;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;

public class RNPushNotificationHelper {
    public static final String PREFERENCES_KEY = "RNPushNotification";
    private static final long DEFAULT_VIBRATION = 1000L;
    private static final String TAG = RNPushNotificationHelper.class.getSimpleName();

    private Context mContext;
    private final SharedPreferences mSharedPreferences;

    public RNPushNotificationHelper(Application context) {
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
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getScheduleNotificationIntent(Bundle bundle) {
        int notificationID = Integer.parseInt(bundle.getString("id"));

        Intent notificationIntent = new Intent(mContext, RNPushNotificationPublisher.class);
        notificationIntent.putExtra(RNPushNotificationPublisher.NOTIFICATION_ID, notificationID);
        notificationIntent.putExtras(bundle);

        return PendingIntent.getBroadcast(mContext, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void sendNotificationScheduled(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            Log.e("RNPushNotification", "No activity class found for the notification");
            return;
        }

        if (bundle.getString("message") == null) {
            Log.e("RNPushNotification", "No message specified for the notification");
            return;
        }

        if(bundle.getString("id") == null) {
            Log.e("RNPushNotification", "No notification ID specified for the notification");
            return;
        }

        double fireDate = bundle.getDouble("fireDate");
        if (fireDate == 0) {
            Log.e("RNPushNotification", "No date specified for the scheduled notification");
            return;
        }

        storeNotificationToPreferences(bundle);

        sendNotificationScheduledCore(bundle);
    }

    public void sendNotificationScheduledCore(Bundle bundle) {
        long fireDate = (long)bundle.getDouble("fireDate");
        
        // If the fireDate is in past, this will fire immediately and show the
        // notification to the user
        PendingIntent pendingIntent = getScheduleNotificationIntent(bundle);

        Log.d("RNPushNotification", String.format("Setting a notification with id %s at time %s",
            bundle.getString("id"), Long.toString(fireDate)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        } else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }
    }

    private void storeNotificationToPreferences(Bundle bundle) {
        RNPushNotificationAttributes notificationAttributes = new RNPushNotificationAttributes();
        notificationAttributes.fromBundle(bundle);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(notificationAttributes.getId(), notificationAttributes.toJson().toString());
        commitPreferences(editor);
    }

    private void commitPreferences(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT < 9) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public void sendNotification(Bundle bundle) {
        try {
            Class intentClass = getMainActivityClass();
            if (intentClass == null) {
                Log.e("RNPushNotification", "No activity class found for the notification");
                return;
            }

            if (bundle.getString("message") == null) {
                Log.e("RNPushNotification", "No message specified for the notification");
                return;
            }

            if(bundle.getString("id") == null) {
                Log.e("RNPushNotification", "No notification ID specified for the notification");
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

            if (subText != null) {
                notification.setSubText(subText);
            }

            String numberString = bundle.getString("number");
            if ( numberString != null ) {
                notification.setNumber(Integer.parseInt(numberString));
            }

            int smallIconResId;
            int largeIconResId;

            String smallIcon = bundle.getString("smallIcon");

            if (smallIcon != null) {
                smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
            } else {
                smallIconResId = res.getIdentifier("ic_notification", "mipmap", packageName);
            }

            if (smallIconResId == 0) {
                smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);

                if (smallIconResId == 0) {
                    smallIconResId = android.R.drawable.ic_dialog_info;
                }
            }

            if (largeIcon != null) {
                largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
            } else {
                largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);
            }

            Bitmap largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);

            if (largeIconResId != 0 && (largeIcon != null || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)) {
                notification.setLargeIcon(largeIconBitmap);
            }

            notification.setSmallIcon(smallIconResId);
            String bigText = bundle.getString("bigText");

            if (bigText == null) {
                bigText = bundle.getString("message");
            }

            notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));

            Intent intent = new Intent(mContext, intentClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            bundle.putBoolean("userInteraction", true);
            intent.putExtra("notification", bundle);

            if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                String soundName = bundle.getString("sound");
                if(soundName != null) {
                    if(!"default".equalsIgnoreCase(soundName)) {
                        soundUri = Uri.parse(soundName);
                    }
                }
                notification.setSound(soundUri);
            }

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.setCategory(NotificationCompat.CATEGORY_CALL);

                String color = bundle.getString("color");
                if (color != null) {
                    notification.setColor(Color.parseColor(color));
                }
            }

            int notificationID = Integer.parseInt(bundle.getString("id"));

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

            JSONArray actionsArray = null;
            try {
                actionsArray = new JSONArray(bundle.getString("actions"));
            } catch (JSONException e) {
                Log.e("RNPushNotification", "Exception while converting actions to JSON object.", e);
            }

            if (actionsArray != null) {
                // No icon for now. The icon value of 0 shows no icon.
                int icon = 0;

                // Add button for each actions.
                for (int i = 0; i < actionsArray.length(); i++) {
                    String action = null;
                    try {
                        action = actionsArray.getString(i);
                    } catch (JSONException e) {
                        Log.e("RNPushNotification", "Exception while getting action from actionsArray.", e);
                        continue;
                    }

                    Intent actionIntent = new Intent();
                    actionIntent.setAction(action);
                    // Add "action" for later identifying which button gets pressed.
                    bundle.putString("action", action);
                    actionIntent.putExtra("notification", bundle);
                    PendingIntent pendingActionIntent = PendingIntent.getBroadcast(mContext, notificationID, actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    notification.addAction(icon, action, pendingActionIntent);
                }
            }

            Notification info = notification.build();
            info.defaults |= Notification.DEFAULT_LIGHTS;

            if(mSharedPreferences.getString(Integer.toString(notificationID), null) != null) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.remove(Integer.toString(notificationID));
                commitPreferences(editor);
            }

            if (bundle.containsKey("tag")) {
                String tag = bundle.getString("tag");
                notificationManager.notify(tag, notificationID, info);
            } else {
                notificationManager.notify(notificationID, info);
            }

            // Can't use setRepeating for recurring notifications because setRepeating
            // is inexact by default starting API 19 and the notifications are not fired
            // at the exact time. During testing, it was found that notifications could
            // late by many minutes.
            this.scheduleNextNotificationIfRepeating(bundle);
        } catch (Exception e) {
            Log.e(TAG, "failed to send push notification", e);
        }
    }

    private void scheduleNextNotificationIfRepeating(Bundle bundle) {
        String repeatType = bundle.getString("repeatType");
        long repeatTime = (long)bundle.getDouble("repeatTime");

        if(repeatType != null) {
            long fireDate = (long)bundle.getDouble("fireDate");
            int msecInAMinute = 60000;

            boolean validRepeatType = Arrays.asList("time", "week", "day", "hour", "minute").contains(repeatType);

            // Sanity checks
            if (!validRepeatType) {
                Log.w("RNPushNotification", String.format("Invalid repeatType specified as %s", repeatType));
                return;
            }

            if (repeatType == "time" && repeatTime <= 0) {
                Log.w("RNPushNotification", "repeatType specified as time but no repeatTime " +
                        "has been mentioned");
                return;
            }

            long newFireDate = 0;

            switch (repeatType) {
                case "time" :
                    newFireDate = fireDate + repeatTime;
                    break;
                case "week":
                    newFireDate = fireDate + 7 * 24 * 60 * msecInAMinute;
                    break;
                case "day":
                    newFireDate = fireDate + 24 * 60 * msecInAMinute;
                    break;
                case "hour":
                    newFireDate = fireDate + 60 * msecInAMinute;
                    break;
                case "minute":
                    newFireDate = fireDate + msecInAMinute;
                    break;
            }

            // Sanity check, should never happen
            if(newFireDate != 0) {
                Log.d("RNPushNotification", String.format("Repeating notification with id %s at time %s",
                        bundle.getString("id"), Long.toString(newFireDate)));
                bundle.putDouble("fireDate", newFireDate);
                this.sendNotificationScheduled(bundle);
            }
        }
    }

    public void cancelAll() {
        Set<String> ids = mSharedPreferences.getAll().keySet();

        for (String id: ids) {
            this.cancelNotification(id);
        }
    }

    public void cancelNotification(String notificationIDString) {
        // Sanity check. Only delete if it has been scheduled
        if (mSharedPreferences.contains(notificationIDString)) {
            Log.d("RNPushNotification", "Cancelling notification with ID " + notificationIDString);

            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancel(Integer.parseInt(notificationIDString));

            Bundle b = new Bundle();
            b.putString("id", notificationIDString);
            getAlarmManager().cancel(getScheduleNotificationIntent(b));

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(notificationIDString);
            commitPreferences(editor);
        } else {
            Log.d("RNPushNotification", "Didn't find a notification with " + notificationIDString +
                " while cancelling a local notification");
        }
    }
}
