package com.dieam.reactnativepushnotification.modules;

import android.annotation.TargetApi;
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
import android.util.Log;

import com.dieam.reactnativepushnotification.helpers.RNAsyncStorage;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;
import static com.dieam.reactnativepushnotification.modules.RNPushNotificationAttributes.fromJson;

public class RNPushNotificationHelper {
    public static final String PREFERENCES_KEY = "rn_push_notification";
    private static final long DEFAULT_VIBRATION = 300L;
    private static final String NOTIFICATION_CHANNEL_ID = "rn-push-notification-channel-id";

    private Context context;
    private RNPushNotificationConfig config;
    private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private boolean isActiveETA, isActiveCriticalAlerts, isTestNotificationAlert = false;
    private boolean isUserGold = true; //SIMULATOR USER GOLD

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

    public void sendToNotificationCentre(Bundle bundle) {
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

            String title = bundle.getString("title");
            if (title == null) {
                ApplicationInfo appInfo = context.getApplicationInfo();
                title = context.getPackageManager().getApplicationLabel(appInfo).toString();
            }

            int priority = NotificationCompat.PRIORITY_HIGH;
            final String priorityString = bundle.getString("priority");

            if (priorityString != null) {
                switch(priorityString.toLowerCase()) {
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
                switch(visibilityString.toLowerCase()) {
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

            //NOTIFICATION ALERT
            String typeNotification = bundle.getString("type");
            Log.d(LOG_TAG, "type notification bundle "+typeNotification);
            if(typeNotification != null){
                userDataAsyncStorage();
            }

           isTestNotificationAlert = Boolean.parseBoolean(bundle.getString("isTestSystem", "false"));

            Uri soundUri = null;
            String soundName = null;

            if (!bundle.containsKey("playSound") || bundle.getBoolean("playSound")) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                soundName = isTestNotificationAlert ? "test" : bundle.getString("soundName");
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
                    .setContentTitle(title)
                    .setTicker(bundle.getString("ticker"))
                    .setVisibility(visibility)
                    .setPriority(priority)
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
            if (numberString != null) {
                notification.setNumber(Integer.parseInt(numberString));
            }

            int smallIconResId, largeIconResId;

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

            if (largeIconResId != 0 && (largeIcon != null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                notification.setLargeIcon(largeIconBitmap);
            }

            notification.setSmallIcon(smallIconResId);
            String bigText = bundle.getString("bigText");

            if (bigText == null) {
                bigText = bundle.getString("message");
            }

            notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));

            Intent intent = new Intent(context, intentClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            bundle.putBoolean("userInteraction", true);
            intent.putExtra("notification", bundle);

            if (soundName != null && soundUri != null)
                notification.setSound(soundUri);

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
            checkOrCreateChannel(notificationManager, soundName, soundUri);

            notification.setContentIntent(pendingIntent);

            if (!bundle.containsKey("vibrate") || bundle.getBoolean("vibrate")) {
                long vibration = bundle.containsKey("vibration") ? (long) bundle.getDouble("vibration") : DEFAULT_VIBRATION;
                if (vibration == 0)
                    vibration = DEFAULT_VIBRATION;
                notification.setVibrate(new long[]{0, vibration});
            }

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

                    Intent actionIntent = new Intent(context, intentClass);
                    actionIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    actionIntent.setAction(context.getPackageName() + "." + action);

                    // Add "action" for later identifying which button gets pressed.
                    bundle.putString("action", action);
                    actionIntent.putExtra("notification", bundle);

                    PendingIntent pendingActionIntent = PendingIntent.getActivity(context, notificationID, actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    notification.addAction(icon, action, pendingActionIntent);
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
                commit(editor);
            }

            Notification info = notification.build();
            info.defaults |= Notification.DEFAULT_LIGHTS;

            //IF USER IS GOLD
            if(isUserGold && typeNotification != null){
                // IF CRITICAL ALERT is active in local storage
                // active > strong (3)
                String intensity = bundle.getString("intensity");
                Log.d(LOG_TAG, "user critical alerts " + isActiveCriticalAlerts);
                if(intensity != null && isActiveCriticalAlerts){
                    alertsFilterDND(notificationManager, intensity);
                }

                // IF ETA is active in local storage
                Log.d(LOG_TAG, "user ETA " + isActiveETA);
                if(bundle.getString("impactAt") != null && isActiveETA){
                    notificationWithETA(notificationManager, notification, notificationID, bundle);
                }
            }else{
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
    }

    private String getChannelId(String soundName, Uri soundUri) {
        String channelPrefix = this.config.getChannelPrefix();
        return (soundName != null && soundUri != null) ? channelPrefix + soundName : NOTIFICATION_CHANNEL_ID;
    }


    private void userDataAsyncStorage(){
        try{
            RNAsyncStorage RNStorage = new RNAsyncStorage(context);
            isActiveETA = true; //RNStorage.getUserETA();
            isActiveCriticalAlerts = RNStorage.getUserDisplayCriticalAlerts();
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
            final String message, groupBySegment;
            final int level;

            if(isTestNotificationAlert){
                message = bundle.getString("message").replace("<<System test>> ","");
            }else{
                message = bundle.getString("message");
            }

            //groupBySegment =  bundle.getString("segment");
            dateImpact = formatDate.parse(bundle.getString("impactAt"));
            level = Integer.parseInt(bundle.getString("intensity"));

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
                    notification.setColor(getColorIntensity(level));
                    notification.setStyle(new NotificationCompat.BigTextStyle()
                            //.setSummaryText("Alerta v4")
                            .bigText(messageExpand));

                    //Change sound depending seconds remaining
                    if(seconds % 10 == 0){
                        setNewSoundNotification(seconds, notificationID);
                    }

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

    private String readableTimeFormat(String unit, int time){
        if(unit.equals("minutes")){
            int idSourceTextMin = getResourceId("plural_time_minutes", "plurals");
            return time >= 1 && time < 2 ? context.getResources().getQuantityString(idSourceTextMin, time, time) + ", " : "";
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