package com.dieam.reactnativepushnotification.modules;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
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
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;
import static com.dieam.reactnativepushnotification.modules.RNPushNotificationAttributes.fromJson;

public class RNPushNotificationHelper {
    public static final String PREFERENCES_KEY = "rn_push_notification";
    private static final long DEFAULT_VIBRATION = 300L;
    private static final String NOTIFICATION_CHANNEL_ID = "rn-push-notification-channel-id";

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

    public void invokeApp(Bundle bundle) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();

        try {
            Class<?> activityClass = Class.forName(className);
            Intent activityIntent = new Intent(context, activityClass);

            if(bundle != null) {
                activityIntent.putExtra("notification", bundle);
            }

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(activityIntent);
        } catch(Exception e) {
            Log.e(LOG_TAG, "Class not found", e);
            return;
        }
    }

    private PendingIntent toScheduleNotificationIntent(Bundle bundle) {
        try {
            int notificationID = Integer.parseInt(bundle.getString("id"));

            Intent notificationIntent = new Intent(context, RNPushNotificationPublisher.class);
            notificationIntent.putExtra(RNPushNotificationPublisher.NOTIFICATION_ID, notificationID);
            notificationIntent.putExtras(bundle);

            return PendingIntent.getBroadcast(context, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to parse Notification ID", e);
        }

        return null;
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
        editor.apply();

        boolean isSaved = scheduledNotificationsPersistence.contains(id);
        if (!isSaved) {
            Log.e(LOG_TAG, "Failed to save " + id);
        }

        sendNotificationScheduledCore(bundle);
    }

    public void sendNotificationScheduledCore(Bundle bundle) {
        long fireDate = (long) bundle.getDouble("fireDate");
        boolean allowWhileIdle = bundle.getBoolean("allowWhileIdle");

        // If the fireDate is in past, this will fire immediately and show the
        // notification to the user
        PendingIntent pendingIntent = toScheduleNotificationIntent(bundle);

        if (pendingIntent == null) {
            return;
        }

        Log.d(LOG_TAG, String.format("Setting a notification with id %s at time %s",
                bundle.getString("id"), Long.toString(fireDate)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (allowWhileIdle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getAlarmManager().setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
            } else {
                getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
            }
        } else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
        }
    }


    public void sendToNotificationCentre(final Bundle bundle) {
      RNPushNotificationPicturesAggregator aggregator = new RNPushNotificationPicturesAggregator(new RNPushNotificationPicturesAggregator.Callback() {
        public void call(Bitmap largeIconImage, Bitmap bigPictureImage) {
          sendToNotificationCentreWithPicture(bundle, largeIconImage, bigPictureImage);
        }
      });

      aggregator.setLargeIconUrl(context, bundle.getString("largeIconUrl"));
      aggregator.setBigPictureUrl(context, bundle.getString("bigPictureUrl"));
    }

    public void sendToNotificationCentreWithPicture(Bundle bundle, Bitmap largeIconBitmap, Bitmap bigPictureBitmap) {
        try {
            Class intentClass = getMainActivityClass();
            if (intentClass == null) {
                Log.e(LOG_TAG, "No activity class found for the notification");
                return;
            }

            if (bundle.getString("message") == null) {
                // this happens when a 'data' notification is received - we do not synthesize a local notification in this case
                Log.d(LOG_TAG, "Cannot send to notification centre because there is no 'message' field in: " + bundle);
                return;
            }

            String notificationIdString = bundle.getString("id");
            if (notificationIdString == null) {
                Log.e(LOG_TAG, "No notification ID specified for the notification");
                return;
            }

            Resources res = context.getResources();
            String packageName = context.getPackageName();

            String channel_id = NOTIFICATION_CHANNEL_ID;

            String title = bundle.getString("title");
            if (title == null) {
                ApplicationInfo appInfo = context.getApplicationInfo();
                title = context.getPackageManager().getApplicationLabel(appInfo).toString();
            }

            int priority = NotificationCompat.PRIORITY_HIGH;
            final String priorityString = bundle.getString("priority");

            if (priorityString != null) {
                switch (priorityString.toLowerCase()) {
                    case "max":
                        priority = NotificationCompat.PRIORITY_MAX;
                        break;
                    case "high":
                        priority = NotificationCompat.PRIORITY_HIGH;
                        break;
                    case "low":
                        priority = NotificationCompat.PRIORITY_LOW;
                        break;
                    case "min":
                        priority = NotificationCompat.PRIORITY_MIN;
                        break;
                    case "default":
                        priority = NotificationCompat.PRIORITY_DEFAULT;
                        break;
                    default:
                        priority = NotificationCompat.PRIORITY_HIGH;
                }
            }

            int importance = 4; // Same as HIGH for lower version

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                importance = NotificationManager.IMPORTANCE_HIGH;

                final String importanceString = bundle.getString("importance");

                if (importanceString != null) {
                    switch (importanceString.toLowerCase()) {
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
            }

            channel_id = channel_id + "-" + importance;

            int visibility = NotificationCompat.VISIBILITY_PRIVATE;
            final String visibilityString = bundle.getString("visibility");

            if (visibilityString != null) {
                switch (visibilityString.toLowerCase()) {
                    case "private":
                        visibility = NotificationCompat.VISIBILITY_PRIVATE;
                        break;
                    case "public":
                        visibility = NotificationCompat.VISIBILITY_PUBLIC;
                        break;
                    case "secret":
                        visibility = NotificationCompat.VISIBILITY_SECRET;
                        break;
                    default:
                        visibility = NotificationCompat.VISIBILITY_PRIVATE;
                }
            }

            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channel_id)
                    .setContentTitle(title)
                    .setTicker(bundle.getString("ticker"))
                    .setVisibility(visibility)
                    .setPriority(priority)
                    .setAutoCancel(bundle.getBoolean("autoCancel", true));
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // API 24 and higher
                // Restore showing timestamp on Android 7+
                // Source: https://developer.android.com/reference/android/app/Notification.Builder.html#setShowWhen(boolean)
                boolean showWhen = bundle.getBoolean("showWhen", true);

                notification.setShowWhen(showWhen);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26 and higher
                // Changing Default mode of notification
                notification.setDefaults(Notification.DEFAULT_LIGHTS);
            }

            Spanned styledText = HtmlCompat.fromHtml(bundle.getString("message"), HtmlCompat.FROM_HTML_MODE_LEGACY);

            notification.setContentText(styledText);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) { // API 20 and higher
              String group = bundle.getString("group");

              if (group != null) {
                  notification.setGroup(group);
              }

              if (bundle.containsKey("groupSummary") || bundle.getBoolean("groupSummary")) {
                  notification.setGroupSummary(bundle.getBoolean("groupSummary"));
              }
            }

            String numberString = bundle.getString("number");

            if (numberString != null) {
                notification.setNumber(Integer.parseInt(numberString));
            }

            // Small icon
            int smallIconResId;

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

            notification.setSmallIcon(smallIconResId);

            // Large icon
            if(largeIconBitmap == null) {
                int largeIconResId;

                String largeIcon = bundle.getString("largeIcon");

                if (largeIcon != null) {
                    largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
                } else {
                    largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);
                }

                // Before Lolipop there was no large icon for notifications.
                if (largeIconResId != 0 && (largeIcon != null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                    largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);
                }
            }
            
            if (largeIconBitmap != null){
              notification.setLargeIcon(largeIconBitmap);
            }

            notification.setSmallIcon(smallIconResId);

            Spanned bigText = null;
            String bigTextString = bundle.getString("bigText");

            if(bigTextString == null) {
                bigText = styledText;
            } else {
                bigText = HtmlCompat.fromHtml(bigTextString, HtmlCompat.FROM_HTML_MODE_LEGACY);
            }

            String message = null;
            String messageString = bundle.getString("message");

            if(messageString == null) {
                message = styledText;
            } else {
                message = HtmlCompat.fromHtml(messageString, HtmlCompat.FROM_HTML_MODE_LEGACY);
            }

            notification.setContentText(message);

            String subText = bundle.getString("subText");

            if (subText != null) {
                notification.setSubText(subText);
            }

            if (bigText == null) {
                bigText = message;
            }

            NotificationCompat.Style style;

            if(bigPictureBitmap != null) {
              style = new NotificationCompat.BigPictureStyle()
                      .bigPicture(bigPictureBitmap)
                      .setBigContentTitle(title)
                      .setSummaryText(message);
            }
            else {
              style = new NotificationCompat.BigTextStyle().bigText(bigText);
            }

            notification.setStyle(style);

            Intent intent = new Intent(context, intentClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            bundle.putBoolean("userInteraction", true);
            intent.putExtra("notification", bundle);

            Uri soundUri = null;

            if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                String soundName = bundle.getString("soundName");

                if (soundName != null) {
                    if (!"default".equalsIgnoreCase(soundName)) {

                        // sound name can be full filename, or just the resource name.
                        // So the strings 'my_sound.mp3' AND 'my_sound' are accepted
                        // The reason is to make the iOS and android javascript interfaces compatible

                        int resId;
                        if (context.getResources().getIdentifier(soundName, "raw", context.getPackageName()) != 0) {
                            resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName());
                        } else {
                            soundName = soundName.substring(0, soundName.lastIndexOf('.'));
                            resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName());
                        }

                        soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
                    }
                } else {
                    soundName = "default";
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26 and higher
                    channel_id = channel_id + "-" + soundName;
                }

                notification.setSound(soundUri);
            }

            if (soundUri == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification.setSound(null);
            }

            if (bundle.containsKey("ongoing") || bundle.getBoolean("ongoing")) {
                notification.setOngoing(bundle.getBoolean("ongoing"));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.setCategory(NotificationCompat.CATEGORY_CALL);

                String color = bundle.getString("color");
                int defaultColor = this.config.getNotificationColor();
                if (color != null) {
                    notification.setColor(Color.parseColor(color));
                } else if (defaultColor != -1) {
                    notification.setColor(defaultColor);
                }
            }

            int notificationID = Integer.parseInt(notificationIdString);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationID, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = notificationManager();

            long[] vibratePattern = new long[]{0};

            if (!bundle.containsKey("vibrate") || bundle.getBoolean("vibrate")) {
                long vibration = bundle.containsKey("vibration") ? (long) bundle.getDouble("vibration") : DEFAULT_VIBRATION;
                if (vibration == 0)
                    vibration = DEFAULT_VIBRATION;

                channel_id = channel_id + "-" + vibration;

                vibratePattern = new long[]{0, vibration};

                notification.setVibrate(vibratePattern); 
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { 
              // Define the shortcutId
              String shortcutId = bundle.getString("shortcutId");
              
              if (shortcutId != null) {
                notification.setShortcutId(shortcutId);
              }
            }

            // Override channel_id if there is one provided
            String customChannelId = bundle.getString("channelId");
            
            if (customChannelId != null) {
              channel_id = customChannelId;
            }

            // Override channel_name, channel_description if there is one provided
            String channel_name = bundle.getString("channelName");
            String channel_description = bundle.getString("channelDescription");
            
            checkOrCreateChannel(notificationManager, channel_id, channel_name, channel_description, soundUri, importance, vibratePattern);

            notification.setChannelId(channel_id);
            notification.setContentIntent(pendingIntent);

            JSONArray actionsArray = null;
            try {
                actionsArray = bundle.getString("actions") != null ? new JSONArray(bundle.getString("actions")) : null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Exception while converting actions to JSON object.", e);
            }

            if (actionsArray != null) {
                // No icon for now. The icon value of 0 shows no icon.
                int icon = 0;

                // Add button for each actions.
                for (int i = 0; i < actionsArray.length(); i++) {
                    String action;
                    try {
                        action = actionsArray.getString(i);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Exception while getting action from actionsArray.", e);
                        continue;
                    }


                    Intent actionIntent = new Intent(context, RNPushNotificationActions.class);
                    actionIntent.setAction(packageName + ".ACTION_" + i);

                    actionIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    // Add "action" for later identifying which button gets pressed.
                    bundle.putString("action", action);
                    actionIntent.putExtra("notification", bundle);
                    actionIntent.setPackage(packageName);

                    PendingIntent pendingActionIntent = PendingIntent.getBroadcast(context, notificationID, actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                      notification.addAction(new NotificationCompat.Action.Builder(icon, action, pendingActionIntent).build());
                    } else {
                      notification.addAction(icon, action, pendingActionIntent);
                    }
                }
            }

            // Remove the notification from the shared preferences once it has been shown
            // to avoid showing the notification again when the phone is rebooted. If the
            // notification is not removed, then every time the phone is rebooted, we will
            // try to reschedule all the notifications stored in shared preferences and since
            // these notifications will be in the past time, they will be shown immediately
            // to the user which we shouldn't do. So, remove the notification from the shared
            // preferences once it has been shown to the user. If it is a repeating notification
            // it will be scheduled again.
            if (scheduledNotificationsPersistence.getString(notificationIdString, null) != null) {
                SharedPreferences.Editor editor = scheduledNotificationsPersistence.edit();
                editor.remove(notificationIdString);
                editor.apply();
            }

            if (!(this.isApplicationInForeground(context) && bundle.getBoolean("ignoreInForeground"))) {
                Notification info = notification.build();
                info.defaults |= Notification.DEFAULT_LIGHTS;

                if (bundle.containsKey("tag")) {
                    String tag = bundle.getString("tag");
                    notificationManager.notify(tag, notificationID, info);
                } else {
                    notificationManager.notify(notificationID, info);
                }
            }

            // Can't use setRepeating for recurring notifications because setRepeating
            // is inexact by default starting API 19 and the notifications are not fired
            // at the exact time. During testing, it was found that notifications could
            // late by many minutes.
            this.scheduleNextNotificationIfRepeating(bundle);
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
                    nextEvent.set(Calendar.DAY_OF_MONTH, Math.min(fireDay, maxDay));
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

    public void clearNotification(String tag, int notificationID) {
        Log.i(LOG_TAG, "Clearing notification: " + notificationID);

        NotificationManager notificationManager = notificationManager();
        if(tag != null) {
          notificationManager.cancel(tag, notificationID);
        } else {
          notificationManager.cancel(notificationID);
        }
    }

    public void clearDeliveredNotifications(ReadableArray identifiers) {
      NotificationManager notificationManager = notificationManager();
      for (int index = 0; index < identifiers.size(); index++) {
        String id = identifiers.getString(index);
        Log.i(LOG_TAG, "Removing notification with id " + id);
        notificationManager.cancel(Integer.parseInt(id));
      }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public WritableArray getDeliveredNotifications() {
      NotificationManager notificationManager = notificationManager();
      StatusBarNotification delivered[] = notificationManager.getActiveNotifications();
      Log.i(LOG_TAG, "Found " + delivered.length + " delivered notifications");
      WritableArray result = Arguments.createArray();
      /*
        * stay consistent to the return structure in
        * https://facebook.github.io/react-native/docs/pushnotificationios.html#getdeliverednotifications
        * but there is no such thing as a 'userInfo'
        */
      for (StatusBarNotification notification : delivered) {
        Notification original = notification.getNotification();
        Bundle extras = original.extras;
        WritableMap notif = Arguments.createMap();
        notif.putString("identifier", "" + notification.getId());
        notif.putString("title", extras.getString(Notification.EXTRA_TITLE));
        notif.putString("body", extras.getString(Notification.EXTRA_TEXT));
        notif.putString("tag", notification.getTag());
        notif.putString("group", original.getGroup());
        result.pushMap(notif);
      }

      return result;

    }

    public WritableArray getScheduledLocalNotifications() {
        WritableArray scheduled = Arguments.createArray();

        Map<String, ?> scheduledNotifications = scheduledNotificationsPersistence.getAll();

        for (Map.Entry<String, ?> entry : scheduledNotifications.entrySet()) {
            try {
                RNPushNotificationAttributes notification = fromJson(entry.getValue().toString());
                WritableMap notificationMap = Arguments.createMap();

                notificationMap.putString("title", notification.getTitle());
                notificationMap.putString("message", notification.getMessage());
                notificationMap.putString("number", notification.getNumber());
                notificationMap.putDouble("date", notification.getFireDate());
                notificationMap.putString("id", notification.getId());
                notificationMap.putString("repeatInterval", notification.getRepeatType());
                notificationMap.putString("soundName", notification.getSound());

                scheduled.pushMap(notificationMap);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }

        return scheduled;
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
        PendingIntent pendingIntent = toScheduleNotificationIntent(b);

        if (pendingIntent != null) {
            getAlarmManager().cancel(pendingIntent);
        }

        if (scheduledNotificationsPersistence.contains(notificationIDString)) {
            // remove it from local storage
            SharedPreferences.Editor editor = scheduledNotificationsPersistence.edit();
            editor.remove(notificationIDString);
            editor.apply();
        } else {
            Log.w(LOG_TAG, "Unable to find notification " + notificationIDString);
        }

        // removed it from the notification center
        NotificationManager notificationManager = notificationManager();

        try {
            notificationManager.cancel(Integer.parseInt(notificationIDString));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to parse Notification ID " + notificationIDString, e);
        }
    }

    private NotificationManager notificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void checkOrCreateDefaultChannel() {
      if(!this.config.getChannelCreateDefault()) {
        return;
      }

      NotificationManager manager = notificationManager();

      int importance = 4; // Default value of HIGH for lower version

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
          importance = NotificationManager.IMPORTANCE_HIGH;
      }

      Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      
      // Instanciate a default channel with default sound.
      String channel_id_sound = NOTIFICATION_CHANNEL_ID + "-" + importance + "-default-" + DEFAULT_VIBRATION;
      checkOrCreateChannel(manager, channel_id_sound, null, null, soundUri, importance, new long[] {0, DEFAULT_VIBRATION});
    }

    public List<String> listChannels() {
      List<String> channels = new ArrayList<>();
      
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
          return channels;
      
      NotificationManager manager = notificationManager();

      if (manager == null)
        return channels;

      List<NotificationChannel> listChannels = manager.getNotificationChannels();

      for(NotificationChannel channel : listChannels) {
        channels.add(channel.getId());
      }

      return channels;
    }

    public boolean channelBlocked(String channel_id) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
          return false;
      
      NotificationManager manager = notificationManager();

      if (manager == null)
          return false;

      NotificationChannel channel = manager.getNotificationChannel(channel_id);

      if(channel == null)
          return false;

      return NotificationManager.IMPORTANCE_NONE == channel.getImportance();
    }

    public boolean channelExists(String channel_id) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
          return false;
      
      NotificationManager manager = notificationManager();

      if (manager == null)
          return false;

      NotificationChannel channel = manager.getNotificationChannel(channel_id);

      return channel != null;
    }

    public void deleteChannel(String channel_id) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        
        NotificationManager manager = notificationManager();

        if (manager == null)
            return;

        manager.deleteNotificationChannel(channel_id);
    }

    private void checkOrCreateChannel(NotificationManager manager, String channel_id, String channel_name, String channel_description, Uri soundUri, int importance, long[] vibratePattern) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        if (manager == null)
            return;

        NotificationChannel channel = manager.getNotificationChannel(channel_id);

        if (channel == null) {
            if(channel_name == null) {
              channel_name = this.config.getChannelName(channel_id);
            }

            if(channel_description == null) {
              channel_description = this.config.getChannelDescription(channel_id);
            }

            channel = new NotificationChannel(channel_id, channel_name, importance);

            channel.setDescription(channel_description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(vibratePattern);

            if (soundUri != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                channel.setSound(soundUri, audioAttributes);
            } else {
                channel.setSound(null, null);
            }

            manager.createNotificationChannel(channel);
        }
    }
    
    public boolean isApplicationInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos != null) {
            for (RunningAppProcessInfo processInfo : processInfos) {
                if (processInfo.processName.equals(context.getPackageName())
                        && processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && processInfo.pkgList.length > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
