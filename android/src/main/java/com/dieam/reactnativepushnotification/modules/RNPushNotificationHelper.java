package com.dieam.reactnativepushnotification.modules;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
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
import androidx.core.app.NotificationCompat;

import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.service.notification.StatusBarNotification;
import android.text.Spanned;
import android.util.Log;
import androidx.core.app.RemoteInput;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;

import com.dieam.reactnativepushnotification.helpers.RNAsyncStorage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;
import static com.dieam.reactnativepushnotification.modules.RNPushNotificationAttributes.fromJson;
import static com.dieam.reactnativepushnotification.modules.RNPushNotification.KEY_TEXT_REPLY;

public class RNPushNotificationHelper {
    public static final String PREFERENCES_KEY = "rn_push_notification";
    private static final long DEFAULT_VIBRATION = 300L;

    private Context context;
    private RNPushNotificationConfig config;
    private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private boolean isActiveETA, isActiveCriticalAlerts, isTestNotificationAlert = false;
    private boolean isUserGold = false; //SIMULATOR USER GOLD

    private final SharedPreferences scheduledNotificationsPersistence;

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

            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

            return PendingIntent.getBroadcast(context, notificationID, notificationIntent, flags);
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
        public void call(Bitmap largeIconImage, Bitmap bigPictureImage, Bitmap bigLargeIconImage) {
          sendToNotificationCentreWithPicture(bundle, largeIconImage, bigPictureImage, bigLargeIconImage);
        }
      });

      aggregator.setLargeIconUrl(context, bundle.getString("largeIconUrl"));
      aggregator.setBigLargeIconUrl(context, bundle.getString("bigLargeIconUrl"));
      aggregator.setBigPictureUrl(context, bundle.getString("bigPictureUrl"));
    }

    public void sendToNotificationCentreWithPicture(Bundle bundle, Bitmap largeIconBitmap, Bitmap bigPictureBitmap, Bitmap bigLargeIconBitmap) {
        try {
            Class intentClass = getMainActivityClass();
            if (intentClass == null) {
                Log.e(LOG_TAG, "No activity class found for the notification");
                return;
            }

            if (bundle.getString("message") == null) {
                // this happens when a 'data' notification is received - we do not synthesize a local notification in this case
                Log.d(LOG_TAG, "Ignore this message if you sent data-only notification. Cannot send to notification centre because there is no 'message' field in: " + bundle);
                return;
            }

            String notificationIdString = bundle.getString("id");
            if (notificationIdString == null) {
                Log.e(LOG_TAG, "No notification ID specified for the notification");
                return;
            }

            Resources res = context.getResources();
            String packageName = context.getPackageName();

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
            
            String channel_id = bundle.getString("channelId");

            //NOTIFICATION ALERT
            //String typeNotification = bundle.getString("local");
            //Log.d(LOG_TAG, "type notification bundle "+typeNotification);
            /*
            if(typeNotification != null){
                userDataAsyncStorage();
            }
            */

           //isTestNotificationAlert = Boolean.parseBoolean(bundle.getString("isTestSystem", "false"));

            Uri soundUri = null;
            String soundName = null;

            if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                soundName = bundle.getString("soundName");
                Log.d(LOG_TAG, "set sound " + soundName);
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

                        soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + resId);
                    }
                }
            }

            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, this.getChannelId(soundName, soundUri))


            notification.setContentTitle(title)
                    .setTicker(bundle.getString("ticker"))
                    .setVisibility(visibility)
                    .setPriority(priority)
                    .setAutoCancel(bundle.getBoolean("autoCancel", true))
                    .setOnlyAlertOnce(bundle.getBoolean("onlyAlertOnce", false));
            
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

            int smallIconResId, largeIconResId;
            // Small icon
//            int smallIconResId = 0;

            String smallIcon = bundle.getString("smallIcon");

            if (smallIcon != null && !smallIcon.isEmpty()) {
                smallIconResId = res.getIdentifier(smallIcon, "drawable", packageName);
                if (smallIconResId == 0) {
                    smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
                }
            } else if(smallIcon == null) {
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
                int largeIconResId = 0;

                String largeIcon = bundle.getString("largeIcon");

                if (largeIcon != null && !largeIcon.isEmpty()) {
                    largeIconResId = res.getIdentifier(largeIcon, "drawable", packageName);
                    if (largeIconResId == 0) {
                        largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
                    }
                } else if(largeIcon == null) {
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

            String message = bundle.getString("message");

            notification.setContentText(message);

            String subText = bundle.getString("subText");

            if (subText != null) {
                notification.setSubText(subText);
            }

            NotificationCompat.Style style;

            if(bigPictureBitmap != null) {

              // Big large icon
              if(bigLargeIconBitmap == null) {
                  int bigLargeIconResId = 0;

                  String bigLargeIcon = bundle.getString("bigLargeIcon");

                  if (bigLargeIcon != null && !bigLargeIcon.isEmpty()) {
                    bigLargeIconResId = res.getIdentifier(bigLargeIcon, "mipmap", packageName);
                    if (bigLargeIconResId != 0) {
                      bigLargeIconBitmap = BitmapFactory.decodeResource(res, bigLargeIconResId);
                    }
                  }
              }

              style = new NotificationCompat.BigPictureStyle()
                      .bigPicture(bigPictureBitmap)
                      .setBigContentTitle(title)
                      .setSummaryText(message)
                      .bigLargeIcon(bigLargeIconBitmap);
            }
            else {
              String bigText = bundle.getString("bigText");

              if (bigText == null) {
                  style = new NotificationCompat.BigTextStyle().bigText(message);
              } else {
                  Spanned styledText = HtmlCompat.fromHtml(bigText, HtmlCompat.FROM_HTML_MODE_LEGACY);
                  style = new NotificationCompat.BigTextStyle().bigText(styledText);
              }
            }

            notification.setStyle(style);

            Intent intent = new Intent(context, intentClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            bundle.putBoolean("foreground", this.isApplicationInForeground());
            bundle.putBoolean("userInteraction", true);
            intent.putExtra("notification", bundle);

//            if (soundName != null && soundUri != null)

            // Add message_id to intent so react-native-firebase/messaging can identify it
            String messageId = bundle.getString("messageId");
            if (messageId != null) {
                intent.putExtra("message_id", messageId);
            }

            Uri soundUri = null;

            if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
                String soundName = bundle.getString("soundName");

                soundUri = getSoundUri(soundName);

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

            String localAlert = bundle.getString("local");
            if(localAlert != null){
                isTestNotificationAlert = Boolean.parseBoolean(bundle.getString("isTestSystem", "false"));

                if(isTestNotificationAlert){
                    String titleAlert = context.getString(getResourceId( "notification_drill_title", "string"));
                    notification.setContentTitle(titleAlert);
                }

                final Pattern pattern = Pattern.compile("Intensidad esperada en (.+)[,:]");

                JSONObject localJson = new JSONObject(localAlert);
                String intensity = localJson.getString("intensity");
                String impactAt = localJson.getString("impactAt");
                String nameIntensity = getNameIntensity(Integer.parseInt(intensity));

                //Time ETA
                Date dateNow = new Date(System.currentTimeMillis());
                formatDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date dateImpact = formatDate.parse(impactAt);

                long dateNowTime = dateNow.getTime();
                long dateImpactTime = dateImpact.getTime();
                long diffTimeImpact  =  dateImpactTime - dateNowTime;

                //int minutes = (int) ((diffTimeImpact / (1000 * 60 )) % 60);
                int seconds = (int) (diffTimeImpact / 1000);

                String textTime = readableTimeFormat("seconds", seconds);
                Log.d(LOG_TAG, ">> ETA parce :"+ textTime);

                //Location ETA
                JSONObject notificationJson = new JSONObject(bundle.getString("notification"));
                String rawMessage = notificationJson.getString("message");
                Matcher matcher = pattern.matcher(rawMessage);

                String location, message;
                if (matcher.find()) {
                    location = matcher.group(1);
                }else{
                    String textUnknown = context.getString(getResourceId("unknown", "string"));
                    location = textUnknown;
                }
                Log.d(LOG_TAG, ">> ETA Location :"+ location);

                if(diffTimeImpact > 0){
                    message = context.getString(getResourceId("notification_body_incoming", "string"), nameIntensity, textTime, location);

                }else if( diffTimeImpact > -30000){
                    message = context.getString(getResourceId("notification_body_now", "string"), nameIntensity, location);
                }else{
                    String nameSourceTitle = isTestNotificationAlert ? "notification_drill_title_finish" : "notification_title_finish";
                    String titleFinish = context.getString(getResourceId( nameSourceTitle, "string"));
                    notification.setContentTitle(titleFinish);

                    message = context.getString(getResourceId("notification_body_finish", "string"), nameIntensity, location);
                }

                String messageDrill = isTestNotificationAlert ? context.getString(getResourceId( "notification_drill_body_start", "string")) + " ": "";
                String newMessage = messageDrill + message;

                Log.d(LOG_TAG, ">> Notification msg :"+ newMessage);

                notification.setContentText(newMessage);
                notification.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(newMessage));
            }

            int notificationID = Integer.parseInt(notificationIdString);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationID, intent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = notificationManager();

            checkOrCreateChannel(notificationManager, soundName, soundUri);

            long[] vibratePattern = new long[]{0};

            if (!bundle.containsKey("vibrate") || bundle.getBoolean("vibrate")) {
                long vibration = bundle.containsKey("vibration") ? (long) bundle.getDouble("vibration") : DEFAULT_VIBRATION;
                if (vibration == 0)
                    vibration = DEFAULT_VIBRATION;

                vibratePattern = new long[]{0, vibration};

                notification.setVibrate(vibratePattern); 
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { 
              // Define the shortcutId
              String shortcutId = bundle.getString("shortcutId");
              
              if (shortcutId != null) {
                notification.setShortcutId(shortcutId);
              }
 
              Long timeoutAfter = (long) bundle.getDouble("timeoutAfter");
  
              if (timeoutAfter != null && timeoutAfter >= 0) {
                notification.setTimeoutAfter(timeoutAfter);
              }
            }

            Long when = (long) bundle.getDouble("when");
  
            if (when != null && when >= 0) {
              notification.setWhen(when);
            }

            notification.setUsesChronometer(bundle.getBoolean("usesChronometer", false));
                
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
                    if (messageId != null) {
                        intent.putExtra("message_id", messageId);
                    }

                    int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

                    PendingIntent pendingActionIntent = PendingIntent.getBroadcast(context, notificationID, actionIntent, flags);

                    if(action.equals("ReplyInput")){
                        //Action with inline reply
                        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH){
                            RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                                    .setLabel(bundle.getString("reply_placeholder_text"))
                                    .build();
                            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                                    icon, bundle.getString("reply_button_text"), pendingActionIntent)
                                    .addRemoteInput(remoteInput)
                                    .setAllowGeneratedReplies(true)
                                    .build();

                            notification.addAction(replyAction);
                        }
                        else{
                            // The notification will not have action
                            break;
                        }
                    }
                    else{
                        // Add "action" for later identifying which button gets pressed
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                          notification.addAction(new NotificationCompat.Action.Builder(icon, action, pendingActionIntent).build());
                        } else {
                          notification.addAction(icon, action, pendingActionIntent);
                        }
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

            if (!(this.isApplicationInForeground() && bundle.getBoolean("ignoreInForeground"))) {
                Notification info = notification.build();
                info.defaults |= Notification.DEFAULT_LIGHTS;

            //IF USER IS GOLD
/*            if(isUserGold && localKey != null){
                // IF CRITICAL ALERT is active in local storage
                // active > strong (3)
                JSONObject localJson = new JSONObject(localKey);
                String intensity = localJson.getString("intensity");
                String impactAt = localJson.getString("impactAt");

                if(intensity != null && isActiveCriticalAlerts){
                    alertsFilterDND(notificationManager, intensity);
                }

                // IF ETA is active in local storage
                Log.d(LOG_TAG, "user ETA " + isActiveETA);
                if(impactAt != null && isActiveETA){
                    notificationWithETA(notificationManager, notification, notificationID, bundle);
                }*/

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

            long newFireDate;
            if ("time".equals(repeatType)) {
                newFireDate = fireDate + repeatTime;
            } else {
                int repeatField = getRepeatField(repeatType);

                final Calendar nextEvent = Calendar.getInstance();
                nextEvent.setTimeInMillis(fireDate);
                // Limits repeat time increment to int instead of long
                int increment = repeatTime > 0 ? (int) repeatTime : 1;
                nextEvent.add(repeatField, increment);

                newFireDate = nextEvent.getTimeInMillis();
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

    private int getRepeatField(String repeatType) {
        switch (repeatType) {
            case "month":
                return Calendar.MONTH;
            case "week":
                return Calendar.WEEK_OF_YEAR;
            case "hour":
                return Calendar.HOUR;
            case "minute":
                return Calendar.MINUTE;
            case "day":
            default:
                return Calendar.DATE;
        }
    }

    private Uri getSoundUri(String soundName) {
        if (soundName == null || "default".equalsIgnoreCase(soundName)) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {

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

            return Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
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
      WritableArray result = Arguments.createArray();
  
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return result;
      }

      NotificationManager notificationManager = notificationManager();
      StatusBarNotification delivered[] = notificationManager.getActiveNotifications();
      Log.i(LOG_TAG, "Found " + delivered.length + " delivered notifications");
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
                notificationMap.putString("data", notification.getUserInfo());

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

    public void cancelScheduledNotification(String notificationIDString) {
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

    @TargetApi(26)
    private void checkOrCreateChannel(NotificationManager manager, String soundName, Uri soundUri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        if (manager == null)
            return;

        String channelId = this.getChannelId(soundName, soundUri);

        if (manager.getNotificationChannel(channelId) != null) {
            return;
        }

        Bundle bundle = new Bundle();

        String channelName = this.config.getChannelNameForId(soundName);
        String channelDescription = this.config.getChannelDescriptionForId(soundName);

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

            manager.createNotificationChannel(channel);

            return true;
        }
        }

    public void deleteChannel(String channel_id) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        
        NotificationManager manager = notificationManager();

        if (manager == null)
            return;

        manager.deleteNotificationChannel(channel_id);
    }

    private boolean checkOrCreateChannel(NotificationManager manager, String channel_id, String channel_name, String channel_description, Uri soundUri, int importance, long[] vibratePattern) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return false;
        if (manager == null)
            return false;

        NotificationChannel channel = manager.getNotificationChannel(channel_id);

        if (
          channel == null && channel_name != null && channel_description != null ||
          channel != null &&
          (
            channel_name != null && !channel_name.equals(channel.getName()) ||
            channel_description != null && !channel_description.equals(channel.getDescription())
          )
        ) {
            // If channel doesn't exist create a new one.
            // If channel name or description is updated then update the existing channel.
            channel = new NotificationChannel(channel_id, channel_name, importance);

            channel.setDescription(channel_description);
            channel.enableLights(true);
            channel.enableVibration(vibratePattern != null);
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

            return true;
        }


        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelDescription);
        channel.enableLights(true);
        channel.enableVibration(true);

        if (soundName != null && soundUri != null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            channel.setSound(soundUri, audioAttributes);
        }

        manager.createNotificationChannel(channel);
        return false;
    }

    public boolean createChannel(ReadableMap channelInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return false;

        String channelId = channelInfo.getString("channelId");
        String channelName = channelInfo.getString("channelName");
        String channelDescription = channelInfo.hasKey("channelDescription") ? channelInfo.getString("channelDescription") : "";
        boolean playSound = !channelInfo.hasKey("playSound") || channelInfo.getBoolean("playSound");
        String soundName = channelInfo.hasKey("soundName") ? channelInfo.getString("soundName") : "default";
        int importance = channelInfo.hasKey("importance") ? channelInfo.getInt("importance") : 4;
        boolean vibrate = channelInfo.hasKey("vibrate") && channelInfo.getBoolean("vibrate");
        long[] vibratePattern = vibrate ? new long[] { 0, DEFAULT_VIBRATION } : null;

        NotificationManager manager = notificationManager();

        Uri soundUri = playSound ? getSoundUri(soundName) : null;

        return checkOrCreateChannel(manager, channelId, channelName, channelDescription, soundUri, importance, vibratePattern);
    }
    
    public boolean isApplicationInForeground() {
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

    private String getChannelId(String soundName, Uri soundUri) {
        String channelPrefix = this.config.getChannelPrefix();
        return (soundName != null && soundUri != null) ? channelPrefix + soundName : NOTIFICATION_CHANNEL_ID;
    }


    private void userDataAsyncStorage(){
        try{
            RNAsyncStorage RNStorage = new RNAsyncStorage(context);
            isActiveETA = true; //RNStorage.getUserETA();
            isActiveCriticalAlerts = false; //RNStorage.getUserDisplayCriticalAlerts();
        }catch (Exception e){
            Log.e(LOG_TAG, "NO GET ASYNC STORAGE " + e);
        }
    }

    private void alertsFilterDND(NotificationManager notificationManager, String intensity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int statusDND = 0;
            try{
                statusDND = Settings.Global.getInt(context.getContentResolver(), "zen_mode");
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "NOT GET STATE DND");
            }

            if(statusDND != 0){
                Log.d(LOG_TAG, "Is active DND ");
                int level = Integer.parseInt(intensity);
                int statusFilter = level >= 3 ? NotificationManager.INTERRUPTION_FILTER_ALARMS : NotificationManager.INTERRUPTION_FILTER_NONE;
                changeInterruptionFiler(notificationManager, statusFilter, context);
            }else{
                Log.d(LOG_TAG, "Is disable DND");
            }
        }
    }

    private void notificationWithETA(final NotificationManager notificationManager,
                                 final NotificationCompat.Builder notification,
                                 final int notificationID, Bundle bundle){
        try {
            final Timer timer;
            final Date dateImpact;
            final String message;
            final int level;

            final String locationRegex = "Intensidad esperada en (.+)[,:]";

            JSONObject notificationJson = new JSONObject(bundle.getString("notification"));
            String rawMessage = notificationJson.getString("message");

            final Pattern pattern = Pattern.compile(locationRegex);
            final Matcher matcher = pattern.matcher(rawMessage);

            if (matcher.find()) {
                message = matcher.group(1);
            }else{
                message = "DESCONOCIDO";
            }

            JSONObject localJson = new JSONObject(bundle.getString("local"));
            String intensity = localJson.getString("intensity");
            String impactAt = localJson.getString("impactAt");

            dateImpact = formatDate.parse(impactAt);
            level = Integer.parseInt(intensity);

            JSONObject quakeJson = new JSONObject(bundle.getString("quake"));

            Log.d(LOG_TAG, ">>> Init Timer <<<");
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Date dateNow = new Date(System.currentTimeMillis());

                    long diffTimeImpact  =  dateImpact.getTime() - dateNow.getTime();

                    int minutes = (int) ((diffTimeImpact / (1000 * 60 )) % 60);
                    int seconds = (int) (diffTimeImpact / 1000) % 60 ;

                    String textTime = readableTimeFormat("minutes", minutes) + readableTimeFormat("seconds", seconds);

                    Log.d(LOG_TAG, "· ID:("+notificationID+") Remaining "+ textTime);

                    int idStringCollapsed = getResourceId("notification_message_collapsed", "string");
                    String messageCollapsed = context.getString(idStringCollapsed, message, textTime);

                    int idStringExpanded = getResourceId("notification_message_expanded", "string");
                    String messageExpand = context.getString(idStringExpanded, message, textTime);

                    notification.setContentText(messageCollapsed);
                    //notification.setColor(getColorIntensity(level));
                    notification.setStyle(new NotificationCompat.BigTextStyle()
                            .setSummaryText("Alerta v4")
                            .bigText(messageExpand));

                    //Change sound depending seconds remaining
/*                    if(seconds % 10 == 0){
                        setNewSoundNotification(seconds, notificationID);
                    }*/

                    //Group notifications
                    //notification.setGroup(groupBySegment);

                    if(seconds <= 0){
                        timer.cancel();
                        notificationManager.notify(notificationID, notification.build());
                        Log.d(LOG_TAG, ">> Finish timer for "+notificationID+"<<");
                    }else{
                        //Send Notification
                        notificationManager.notify(notificationID, notification.build());
                    }
                }
            }, 0, 1000);

        }catch (Exception e) {
            Log.e(LOG_TAG, "ETA could not be obtained" + e.getMessage());
        }
    }

    private void changeInterruptionFiler(NotificationManager notificationManager, int interruptionFilter, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // If api level minimum 23
            /*
                boolean isNotificationPolicyAccessGranted ()
                    Checks the ability to read/modify notification policy for the calling package.
                    Returns true if the calling package can read/modify notification policy.
                    Request policy access by sending the user to the activity that matches the
                    system intent action ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS.

                    Use ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED to listen for
                    user grant or denial of this access.

                Returns
                    boolean

            */
            // If notification policy access granted for this package
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                /*
                    void setInterruptionFilter (int interruptionFilter)
                        Sets the current notification interruption filter.

                        The interruption filter defines which notifications are allowed to interrupt
                        the user (e.g. via sound & vibration) and is applied globally.

                        Only available if policy access is granted to this package.

                    Parameters
                        interruptionFilter : int
                        Value is INTERRUPTION_FILTER_NONE, INTERRUPTION_FILTER_PRIORITY,
                        INTERRUPTION_FILTER_ALARMS, INTERRUPTION_FILTER_ALL
                        or INTERRUPTION_FILTER_UNKNOWN.
                */

                // Set the interruption filter
                notificationManager.setInterruptionFilter(interruptionFilter);
            } else {
                /*
                    String ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                        Activity Action : Show Do Not Disturb access settings.
                        Users can grant and deny access to Do Not Disturb configuration from here.

                    Input : Nothing.
                    Output : Nothing.
                    Constant Value : "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"
                */
                // If notification policy access not granted for this package

                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                Log.d(LOG_TAG, "Permissions no activate");
            }
        }
    }

    private void setNewSoundNotification(int seconds, int notificationID){
        //TODO: CHANGE SOUND
        // 10 | 20 | 30 | 60 | 80
        switch(seconds) {
            case 10:
                Log.d(LOG_TAG, "· ("+notificationID+") Sound -> less 10 change sound.mp3");
                break;
            case 20:
                Log.d(LOG_TAG, "· ("+notificationID+") Sound -> less 20 seconds.mp3");
                break;
            case 30:
                Log.d(LOG_TAG, "· ("+notificationID+") Sound -> less 30 seconds.mp3");
                break;
            case 60:
                Log.d(LOG_TAG, "· ("+notificationID+") Sound -> less 1 minute.mp3");
                break;
            case 80:
                Log.d(LOG_TAG, "· ("+notificationID+") Sound -> less 1 minute 20 seconds.mp3");
                break;
            default:
                break;
        }
    }

    private int getResourceId(String name, String defType){
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    private int getColorIntensity(int level){
        int idColorLevel = getResourceId("intensity_"+level, "color");
        return context.getResources().getColor(idColorLevel);
    }

    private String getNameIntensity(int level){
        int idColorLevel = getResourceId("intensity_name_"+level, "string");
        return context.getString(idColorLevel);
    }

    private String readableTimeFormat(String unit, int time){
        if(unit.equals("minutes")){
            int idSourceTextMin = getResourceId("plural_time_minutes", "plurals");
            return time >= 1 ? context.getResources().getQuantityString(idSourceTextMin, time, time) + ", " : "";
        }else if(unit.equals("seconds")){
            int idSourceTextSec = getResourceId("plural_time_seconds", "plurals");
            return context.getResources().getQuantityString(idSourceTextSec, time, time);
        }else{
            return "";
        }
    }

    private SpannableStringBuilder setColorIntensity(String message, int intensity){

        int startIndexIntensity = message.indexOf(":") + 1;
        int endIndexIntensity = message.length() - 1;

        SpannableStringBuilder messageStyle = new SpannableStringBuilder(message);
        messageStyle.setSpan(
                new ForegroundColorSpan(getColorIntensity(intensity)),
                startIndexIntensity,
                endIndexIntensity,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
        );

        return messageStyle;
    }
}