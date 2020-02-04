package com.dieam.reactnativepushnotification.modules;


import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;

import com.facebook.react.bridge.ReadableMap;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;
import static com.dieam.reactnativepushnotification.modules.RNPushNotificationAttributes.fromJson;

public class RNPushNotificationHelper {
    public static final String PREFERENCES_KEY = "rn_push_notification";
    private static final long DEFAULT_VIBRATION = 300L;
    private static final int GROUP_MESSAGE_ID = 0;
    private static final String NOTIFICATION_CHANNEL_ID = "rn-push-notification-channel-id";
    private static final int RB_GROUP_MSG_TYPE = 3;

    private Context context;
    private RNPushNotificationConfig config;
    private final SharedPreferences scheduledNotificationsPersistence;
    private static final int ONE_MINUTE = 60 * 1000;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;

    public RNPushNotificationHelper(Application context) {
        this.context = context;
        this.config = new RNPushNotificationConfig(context);
        this.scheduledNotificationsPersistence = context.getSharedPreferences(RNPushNotificationHelper.PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public Class getMainActivityClass() {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent toScheduleNotificationIntent(Bundle bundle) {
        int notificationID = Integer.parseInt(bundle.getString("id"));

        Intent notificationIntent = new Intent(context, RNPushNotificationPublisher.class);
        notificationIntent.putExtra(RNPushNotificationPublisher.NOTIFICATION_ID, notificationID);
        notificationIntent.putExtras(bundle);

        return PendingIntent.getBroadcast(context, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void sendNotificationScheduled(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            Log.e(LOG_TAG, "No activity class found for the scheduled notification");
            return;
        }

        if (bundle.getString("message") == null) {
            Log.e(LOG_TAG, "No message specified for the scheduled notification");
            return;
        }

        if (bundle.getString("id") == null) {
            Log.e(LOG_TAG, "No notification ID specified for the scheduled notification");
            return;
        }

        double fireDate = bundle.getDouble("fireDate");
        if (fireDate == 0) {
            Log.e(LOG_TAG, "No date specified for the scheduled notification");
            return;
        }

        RNPushNotificationAttributes notificationAttributes = new RNPushNotificationAttributes(bundle);
        String id = notificationAttributes.getId();

        Log.d(LOG_TAG, "Storing push notification with id " + id);

        SharedPreferences.Editor editor = scheduledNotificationsPersistence.edit();
        editor.putString(id, notificationAttributes.toJson().toString());
        commit(editor);

        boolean isSaved = scheduledNotificationsPersistence.contains(id);
        if (!isSaved) {
            Log.e(LOG_TAG, "Failed to save " + id);
        }

        sendNotificationScheduledCore(bundle);
    }

    public void sendNotificationScheduledCore(Bundle bundle) {
        long fireDate = (long) bundle.getDouble("fireDate");

        // If the fireDate is in past, this will fire immediately and show the
        // notification to the user
        PendingIntent pendingIntent = toScheduleNotificationIntent(bundle);

        Log.d(LOG_TAG, String.format("Setting a notification with id %s at time %s",
                bundle.getString("id"), Long.toString(fireDate)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        } else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }
    }

    private int getIconResourceId(Bundle bundle){
        String smallIcon = bundle.getString("smallIcon");
        int smallIconResId;
        Resources res = context.getResources();
        String packageName = context.getPackageName();

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
        return smallIconResId;
    }

    public void sendToNotificationCentre(Bundle bundle) {
        try {
            Class intentClass = getMainActivityClass();

            int smallIconResId = this.getIconResourceId(bundle);
            String notificationIdString = bundle.getString("id");
            int notificationID = Integer.parseInt(notificationIdString);

            String title = bundle.getString("title");
            String notificationType = bundle.getString("notification_type");
            String message = bundle.getString("message");
            String bundleTitle = bundle.getString("bundle_title");
            String bundleId = bundle.getString("bundle_id");

            int notificationTypeInt = Integer.parseInt(notificationType);

            NotificationManager notificationManager = notificationManager();
            checkOrCreateChannel(notificationManager);

            Intent intent = new Intent(context, intentClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            bundle.putBoolean("userInteraction", true);
            intent.putExtra("notification", bundle);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationID, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context,
                    NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(smallIconResId)
                    .setStyle(new NotificationCompat.InboxStyle().setSummaryText("RivalBet"))
                    .setGroup("com.apthletic.rivalbet").setGroupSummary(true)
                    .setAutoCancel(bundle.getBoolean("autoCancel", true));


            if (bundleTitle != null && bundleId != null && notificationTypeInt == RB_GROUP_MSG_TYPE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String sender = bundle.getString("sender");
                String chatMessage = bundle.getString("chat_message");
                String chatTimestamp = bundle.getString("chat_timestamp");

                // LP: is supposed to be grouped message
                int bundleIdInt = Integer.parseInt(bundleId);

                Bundle extras = new Bundle();

                for (StatusBarNotification notif : notificationManager.getActiveNotifications()) {
                    if (notif.getId() == bundleIdInt ) {
                        extras = notif.getNotification().extras;
                    }
                }

                ArrayList<String> existingUsernames = extras.getStringArrayList("existingUsernames");
                if (existingUsernames == null) {
                    existingUsernames = new ArrayList<>();
                }
                existingUsernames.add(sender);
                extras.putStringArrayList("existingUsernames", existingUsernames);

                ArrayList<String> existingTimestamps = extras.getStringArrayList("existingTimestamps");
                if (existingTimestamps == null) {
                    existingTimestamps = new ArrayList<>();
                }
                existingTimestamps.add(chatTimestamp);
                extras.putStringArrayList("existingTimestamps", existingTimestamps);

                ArrayList<String> existingMessages = extras.getStringArrayList("existingMessages");
                if (existingMessages == null) {
                    existingMessages = new ArrayList<>();
                }
                existingMessages.add(chatMessage);
                extras.putStringArrayList("existingMessages", existingMessages);

                NotificationCompat.MessagingStyle notifStyle = new NotificationCompat.MessagingStyle("Me")
                        .setConversationTitle(bundleTitle);
                int index = 0;
                for (String m : existingMessages) {
                    String timestampString = existingTimestamps.get(index);
                    long timestampLong = Long.parseLong(timestampString);
                    notifStyle.addMessage(m, timestampLong, existingUsernames.get(index));
                    index++;
                }
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(smallIconResId)
                        .setGroup("com.apthletic.rivalbet")
                        .setAutoCancel(bundle.getBoolean("autoCancel", true))
                        .setExtras(extras)
                        .setStyle(notifStyle);
                notificationBuilder.setContentIntent(pendingIntent);

                notificationManager.notify(bundleIdInt, notificationBuilder.build());
            } else {

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                    NOTIFICATION_CHANNEL_ID)
                    .setGroup("com.apthletic.rivalbet")
                    .setSmallIcon(smallIconResId)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(bundle.getBoolean("autoCancel", true));
                notificationBuilder.setContentIntent(pendingIntent);

                // LP: is a single message
                notificationManager.notify(notificationID, notificationBuilder.build());
            }

            notificationManager.notify(0, summaryBuilder.build());

        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to send push notification", e);
        }
    }

    private void scheduleNextNotificationIfRepeating(Bundle bundle) {
        String repeatType = bundle.getString("repeatType");
        long repeatTime = (long) bundle.getDouble("repeatTime");

        if (repeatType != null) {
            long fireDate = (long) bundle.getDouble("fireDate");

            boolean validRepeatType = Arrays.asList("time", "month", "week", "day", "hour", "minute").contains(repeatType);

            // Sanity checks
            if (!validRepeatType) {
                Log.w(LOG_TAG, String.format("Invalid repeatType specified as %s", repeatType));
                return;
            }

            if ("time".equals(repeatType) && repeatTime <= 0) {
                Log.w(LOG_TAG, "repeatType specified as time but no repeatTime " +
                        "has been mentioned");
                return;
            }

            long newFireDate = 0;

            switch (repeatType) {
                case "time":
                    newFireDate = fireDate + repeatTime;
                    break;
                case "month":
                    final Calendar fireDateCalendar = new GregorianCalendar();
                    fireDateCalendar.setTime(new Date(fireDate));
                    final int fireDay = fireDateCalendar.get(Calendar.DAY_OF_MONTH);
                    final int fireMinute = fireDateCalendar.get(Calendar.MINUTE);
                    final int fireHour = fireDateCalendar.get(Calendar.HOUR_OF_DAY);

                    final Calendar nextEvent = new GregorianCalendar();
                    nextEvent.setTime(new Date());
                    final int currentMonth = nextEvent.get(Calendar.MONTH);
                    int nextMonth = currentMonth < 11 ? (currentMonth + 1) : 0;
                    nextEvent.set(Calendar.YEAR, nextEvent.get(Calendar.YEAR) + (nextMonth == 0 ? 1 : 0));
                    nextEvent.set(Calendar.MONTH, nextMonth);
                    final int maxDay = nextEvent.getActualMaximum(Calendar.DAY_OF_MONTH);
                    nextEvent.set(Calendar.DAY_OF_MONTH, fireDay <= maxDay ? fireDay : maxDay);
                    nextEvent.set(Calendar.HOUR_OF_DAY, fireHour);
                    nextEvent.set(Calendar.MINUTE, fireMinute);
                    nextEvent.set(Calendar.SECOND, 0);
                    newFireDate = nextEvent.getTimeInMillis();
                    break;
                case "week":
                    newFireDate = fireDate + 7 * ONE_DAY;
                    break;
                case "day":
                    newFireDate = fireDate + ONE_DAY;
                    break;
                case "hour":
                    newFireDate = fireDate + ONE_HOUR;
                    break;
                case "minute":
                    newFireDate = fireDate + ONE_MINUTE;
                    break;
            }

            // Sanity check, should never happen
            if (newFireDate != 0) {
                Log.d(LOG_TAG, String.format("Repeating notification with id %s at time %s",
                        bundle.getString("id"), Long.toString(newFireDate)));
                bundle.putDouble("fireDate", newFireDate);
                this.sendNotificationScheduled(bundle);
            }
        }
    }

    public void clearNotifications() {
        Log.i(LOG_TAG, "Clearing alerts from the notification centre");

        NotificationManager notificationManager = notificationManager();
        notificationManager.cancelAll();
    }

    public void clearNotification(int notificationID) {
        Log.i(LOG_TAG, "Clearing notification: " + notificationID);

        NotificationManager notificationManager = notificationManager();
        notificationManager.cancel(notificationID);
    }

    public void cancelAllScheduledNotifications() {
        Log.i(LOG_TAG, "Cancelling all notifications");

        for (String id : scheduledNotificationsPersistence.getAll().keySet()) {
            cancelScheduledNotification(id);
        }
    }

    public void cancelScheduledNotification(ReadableMap userInfo) {
        for (String id : scheduledNotificationsPersistence.getAll().keySet()) {
            try {
                String notificationAttributesJson = scheduledNotificationsPersistence.getString(id, null);
                if (notificationAttributesJson != null) {
                    RNPushNotificationAttributes notificationAttributes = fromJson(notificationAttributesJson);
                    if (notificationAttributes.matches(userInfo)) {
                        cancelScheduledNotification(id);
                    }
                }
            } catch (JSONException e) {
                Log.w(LOG_TAG, "Problem dealing with scheduled notification " + id, e);
            }
        }
    }

    private void cancelScheduledNotification(String notificationIDString) {
        Log.i(LOG_TAG, "Cancelling notification: " + notificationIDString);

        // remove it from the alarm manger schedule
        Bundle b = new Bundle();
        b.putString("id", notificationIDString);
        getAlarmManager().cancel(toScheduleNotificationIntent(b));

        if (scheduledNotificationsPersistence.contains(notificationIDString)) {
            // remove it from local storage
            SharedPreferences.Editor editor = scheduledNotificationsPersistence.edit();
            editor.remove(notificationIDString);
            commit(editor);
        } else {
            Log.w(LOG_TAG, "Unable to find notification " + notificationIDString);
        }

        // removed it from the notification center
        NotificationManager notificationManager = notificationManager();

        notificationManager.cancel(Integer.parseInt(notificationIDString));
    }

    private NotificationManager notificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private static void commit(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT < 9) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    private static boolean channelCreated = false;
    private void checkOrCreateChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        if (channelCreated)
            return;
        if (manager == null)
            return;

        Bundle bundle = new Bundle();

        int importance = NotificationManager.IMPORTANCE_HIGH;
        final String importanceString = bundle.getString("importance");

        if (importanceString != null) {
            switch(importanceString.toLowerCase()) {
                case "default":
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    break;
                case "max":
                    importance = NotificationManager.IMPORTANCE_MAX;
                    break;
                case "high":
                    importance = NotificationManager.IMPORTANCE_HIGH;
                    break;
                case "low":
                    importance = NotificationManager.IMPORTANCE_LOW;
                    break;
                case "min":
                    importance = NotificationManager.IMPORTANCE_MIN;
                    break;
                case "none":
                    importance = NotificationManager.IMPORTANCE_NONE;
                    break;
                case "unspecified":
                    importance = NotificationManager.IMPORTANCE_UNSPECIFIED;
                    break;
                default:
                    importance = NotificationManager.IMPORTANCE_HIGH;
            }
        }

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, this.config.getChannelName() != null ? this.config.getChannelName() : "rn-push-notification-channel", importance);

        channel.setDescription(this.config.getChannelDescription());
        channel.enableLights(true);
        channel.enableVibration(true);

        manager.createNotificationChannel(channel);
        channelCreated = true;
    }
}
