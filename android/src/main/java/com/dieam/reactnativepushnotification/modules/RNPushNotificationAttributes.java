package com.dieam.reactnativepushnotification.modules;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import org.json.JSONException;
import org.json.JSONObject;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationAttributes {
    private static final String ID = "id";
    private static final String MESSAGE = "message";
    private static final String FIRE_DATE = "fireDate";
    private static final String TITLE = "title";
    private static final String TICKER = "ticker";
    private static final String SHOW_WHEN = "showWhen";
    private static final String AUTO_CANCEL = "autoCancel";
    private static final String LARGE_ICON = "largeIcon";
    private static final String LARGE_ICON_URL = "largeIconUrl";
    private static final String SMALL_ICON = "smallIcon";
    private static final String BIG_TEXT = "bigText";
    private static final String SUB_TEXT = "subText";
    private static final String BIG_PICTURE_URL = "bigPictureUrl";
    private static final String SHORTCUT_ID = "shortcutId";
    private static final String CHANNEL_ID = "channelId";
    private static final String NUMBER = "number";
    private static final String SOUND = "sound";
    private static final String COLOR = "color";
    private static final String GROUP = "group";
    private static final String GROUP_SUMMARY = "groupSummary";
    private static final String MESSAGE_ID = "messageId";
    private static final String PLAY_SOUND = "playSound";
    private static final String VIBRATE = "vibrate";
    private static final String VIBRATION = "vibration";
    private static final String ACTIONS = "actions";
    private static final String INVOKE_APP = "invokeApp";
    private static final String TAG = "tag";
    private static final String REPEAT_TYPE = "repeatType";
    private static final String REPEAT_TIME = "repeatTime";
    private static final String WHEN = "when";
    private static final String USES_CHRONOMETER = "usesChronometer";
    private static final String TIMEOUT_AFTER = "timeoutAfter";
    private static final String ONLY_ALERT_ONCE = "onlyAlertOnce";
    private static final String ONGOING = "ongoing";
    private static final String ALLOW_WHILE_IDLE = "allowWhileIdle";
    private static final String IGNORE_IN_FOREGROUND = "ignoreInForeground";

    private final String id;
    private final String message;
    private final double fireDate;
    private final String title;
    private final String ticker;
    private final boolean showWhen;
    private final boolean autoCancel;
    private final String largeIcon;
    private final String largeIconUrl;
    private final String smallIcon;
    private final String bigText;
    private final String subText;
    private final String bigPictureUrl;
    private final String shortcutId;
    private final String number;
    private final String channelId;
    private final String sound;
    private final String color;
    private final String group;
    private final boolean groupSummary;
    private final String messageId;
    private final boolean playSound;
    private final boolean vibrate;
    private final double vibration;
    private final String actions;
    private final boolean invokeApp;
    private final String tag;
    private final String repeatType;
    private final double repeatTime;
    private final double when;
    private final boolean usesChronometer;
    private final double timeoutAfter;
    private final boolean onlyAlertOnce;
    private final boolean ongoing;
    private final boolean allowWhileIdle;
    private final boolean ignoreInForeground;

    public RNPushNotificationAttributes(Bundle bundle) {
        id = bundle.getString(ID);
        message = bundle.getString(MESSAGE);
        fireDate = bundle.getDouble(FIRE_DATE);
        title = bundle.getString(TITLE);
        ticker = bundle.getString(TICKER);
        showWhen = bundle.getBoolean(SHOW_WHEN);
        autoCancel = bundle.getBoolean(AUTO_CANCEL);
        largeIcon = bundle.getString(LARGE_ICON);
        largeIconUrl = bundle.getString(LARGE_ICON_URL);
        smallIcon = bundle.getString(SMALL_ICON);
        bigText = bundle.getString(BIG_TEXT);
        subText = bundle.getString(SUB_TEXT);
        bigPictureUrl= bundle.getString(BIG_PICTURE_URL);
        shortcutId = bundle.getString(SHORTCUT_ID);
        number = bundle.getString(NUMBER);
        channelId = bundle.getString(CHANNEL_ID);
        sound = bundle.getString(SOUND);
        color = bundle.getString(COLOR);
        group = bundle.getString(GROUP);
        groupSummary = bundle.getBoolean(GROUP_SUMMARY);
        messageId = bundle.getString(MESSAGE_ID);
        playSound = bundle.getBoolean(PLAY_SOUND);
        vibrate = bundle.getBoolean(VIBRATE);
        vibration = bundle.getDouble(VIBRATION);
        actions = bundle.getString(ACTIONS);
        invokeApp = bundle.getBoolean(INVOKE_APP);
        tag = bundle.getString(TAG);
        repeatType = bundle.getString(REPEAT_TYPE);
        repeatTime = bundle.getDouble(REPEAT_TIME);
        when = bundle.getDouble(WHEN);
        usesChronometer = bundle.getBoolean(USES_CHRONOMETER);
        timeoutAfter = bundle.getDouble(TIMEOUT_AFTER);
        onlyAlertOnce = bundle.getBoolean(ONLY_ALERT_ONCE);
        ongoing = bundle.getBoolean(ONGOING);
        allowWhileIdle = bundle.getBoolean(ALLOW_WHILE_IDLE);
        ignoreInForeground = bundle.getBoolean(IGNORE_IN_FOREGROUND);
    }

    private RNPushNotificationAttributes(JSONObject jsonObject) {
        try {
            id = jsonObject.has(ID) ? jsonObject.getString(ID) : null;
            message = jsonObject.has(MESSAGE) ? jsonObject.getString(MESSAGE) : null;
            fireDate = jsonObject.has(FIRE_DATE) ? jsonObject.getDouble(FIRE_DATE) : 0.0;
            title = jsonObject.has(TITLE) ? jsonObject.getString(TITLE) : null;
            ticker = jsonObject.has(TICKER) ? jsonObject.getString(TICKER) : null;
            showWhen = jsonObject.has(SHOW_WHEN) ? jsonObject.getBoolean(SHOW_WHEN) : true;
            autoCancel = jsonObject.has(AUTO_CANCEL) ? jsonObject.getBoolean(AUTO_CANCEL) : true;
            largeIcon = jsonObject.has(LARGE_ICON) ? jsonObject.getString(LARGE_ICON) : null;
            largeIconUrl = jsonObject.has(LARGE_ICON_URL) ? jsonObject.getString(LARGE_ICON_URL) : null;
            smallIcon = jsonObject.has(SMALL_ICON) ? jsonObject.getString(SMALL_ICON) : null;
            bigText = jsonObject.has(BIG_TEXT) ? jsonObject.getString(BIG_TEXT) : null;
            subText = jsonObject.has(SUB_TEXT) ? jsonObject.getString(SUB_TEXT) : null;
            bigPictureUrl = jsonObject.has(BIG_PICTURE_URL) ? jsonObject.getString(BIG_PICTURE_URL) : null;
            shortcutId = jsonObject.has(SHORTCUT_ID) ? jsonObject.getString(SHORTCUT_ID) : null;
            number = jsonObject.has(NUMBER) ? jsonObject.getString(NUMBER) : null;
            channelId = jsonObject.has(CHANNEL_ID) ? jsonObject.getString(CHANNEL_ID) : null;
            sound = jsonObject.has(SOUND) ? jsonObject.getString(SOUND) : null;
            color = jsonObject.has(COLOR) ? jsonObject.getString(COLOR) : null;
            group = jsonObject.has(GROUP) ? jsonObject.getString(GROUP) : null;
            groupSummary = jsonObject.has(GROUP_SUMMARY) ? jsonObject.getBoolean(GROUP_SUMMARY) : false;
            messageId = jsonObject.has(MESSAGE_ID) ? jsonObject.getString(MESSAGE_ID) : null;
            playSound = jsonObject.has(PLAY_SOUND) ? jsonObject.getBoolean(PLAY_SOUND) : true;
            vibrate = jsonObject.has(VIBRATE) ? jsonObject.getBoolean(VIBRATE) : true;
            vibration = jsonObject.has(VIBRATION) ? jsonObject.getDouble(VIBRATION) : 1000;
            actions = jsonObject.has(ACTIONS) ? jsonObject.getString(ACTIONS) : null;
            invokeApp = jsonObject.has(INVOKE_APP) ? jsonObject.getBoolean(INVOKE_APP) : true;
            tag = jsonObject.has(TAG) ? jsonObject.getString(TAG) : null;
            repeatType = jsonObject.has(REPEAT_TYPE) ? jsonObject.getString(REPEAT_TYPE) : null;
            repeatTime = jsonObject.has(REPEAT_TIME) ? jsonObject.getDouble(REPEAT_TIME) : 0.0;
            when = jsonObject.has(WHEN) ? jsonObject.getDouble(WHEN) : -1;
            usesChronometer = jsonObject.has(USES_CHRONOMETER) ? jsonObject.getBoolean(USES_CHRONOMETER) : false;
            timeoutAfter = jsonObject.has(TIMEOUT_AFTER) ? jsonObject.getDouble(TIMEOUT_AFTER) : -1;
            onlyAlertOnce = jsonObject.has(ONLY_ALERT_ONCE) ? jsonObject.getBoolean(ONLY_ALERT_ONCE) : false;
            ongoing = jsonObject.has(ONGOING) ? jsonObject.getBoolean(ONGOING) : false;
            allowWhileIdle = jsonObject.has(ALLOW_WHILE_IDLE) ? jsonObject.getBoolean(ALLOW_WHILE_IDLE) : false;
            ignoreInForeground = jsonObject.has(IGNORE_IN_FOREGROUND) ? jsonObject.getBoolean(IGNORE_IN_FOREGROUND) : false;
        } catch (JSONException e) {
            throw new IllegalStateException("Exception while initializing RNPushNotificationAttributes from JSON", e);
        }
    }

    @NonNull
    public static RNPushNotificationAttributes fromJson(String notificationAttributesJson) throws JSONException {
        JSONObject jsonObject = new JSONObject(notificationAttributesJson);
        return new RNPushNotificationAttributes(jsonObject);
    }

    /**
     * User to find notifications:
     * <p>
     *
     * @param userInfo map of fields to match
     * @return true all fields in userInfo object match, false otherwise
     */
    public boolean matches(ReadableMap userInfo) {
        Bundle bundle = toBundle();

        ReadableMapKeySetIterator iterator = userInfo.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();

            if (!bundle.containsKey(key))
                return false;

            switch (userInfo.getType(key)) {
                case Null: {
                    if (bundle.get(key) != null)
                        return false;
                    break;
                }
                case Boolean: {
                    if (userInfo.getBoolean(key) != bundle.getBoolean(key))
                        return false;
                    break;
                }
                case Number: {
                    if ((userInfo.getDouble(key) != bundle.getDouble(key)) && (userInfo.getInt(key) != bundle.getInt(key)))
                        return false;
                    break;
                }
                case String: {
                    if (!userInfo.getString(key).equals(bundle.getString(key)))
                        return false;
                    break;
                }
                case Map:
                    return false;//there are no maps in the bundle
                case Array:
                    return false;//there are no arrays in the bundle
            }
        }

        return true;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(ID, id);
        bundle.putString(MESSAGE, message);
        bundle.putDouble(FIRE_DATE, fireDate);
        bundle.putString(TITLE, title);
        bundle.putString(TICKER, ticker);
        bundle.putBoolean(SHOW_WHEN, showWhen);
        bundle.putBoolean(AUTO_CANCEL, autoCancel);
        bundle.putString(LARGE_ICON, largeIcon);
        bundle.putString(LARGE_ICON_URL, largeIconUrl);
        bundle.putString(SMALL_ICON, smallIcon);
        bundle.putString(BIG_TEXT, bigText);
        bundle.putString(SUB_TEXT, subText);
        bundle.putString(BIG_PICTURE_URL, bigPictureUrl);
        bundle.putString(SHORTCUT_ID, shortcutId);
        bundle.putString(NUMBER, number);
        bundle.putString(CHANNEL_ID, channelId);
        bundle.putString(SOUND, sound);
        bundle.putString(COLOR, color);
        bundle.putString(GROUP, group);
        bundle.putBoolean(GROUP_SUMMARY, groupSummary);
        bundle.putString(MESSAGE_ID, messageId);
        bundle.putBoolean(PLAY_SOUND, playSound);
        bundle.putBoolean(VIBRATE, vibrate);
        bundle.putDouble(VIBRATION, vibration);
        bundle.putString(ACTIONS, actions);
        bundle.putBoolean(INVOKE_APP, invokeApp);
        bundle.putString(TAG, tag);
        bundle.putString(REPEAT_TYPE, repeatType);
        bundle.putDouble(REPEAT_TIME, repeatTime);
        bundle.putDouble(WHEN, when);
        bundle.putBoolean(USES_CHRONOMETER, usesChronometer);
        bundle.putDouble(TIMEOUT_AFTER, timeoutAfter);
        bundle.putBoolean(ONLY_ALERT_ONCE, onlyAlertOnce);
        bundle.putBoolean(ONGOING, ongoing);
        bundle.putBoolean(ALLOW_WHILE_IDLE, allowWhileIdle);
        bundle.putBoolean(IGNORE_IN_FOREGROUND, ignoreInForeground);
        return bundle;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ID, id);
            jsonObject.put(MESSAGE, message);
            jsonObject.put(FIRE_DATE, fireDate);
            jsonObject.put(TITLE, title);
            jsonObject.put(TICKER, ticker);
            jsonObject.put(SHOW_WHEN, showWhen);
            jsonObject.put(AUTO_CANCEL, autoCancel);
            jsonObject.put(LARGE_ICON, largeIcon);
            jsonObject.put(LARGE_ICON_URL, largeIconUrl);
            jsonObject.put(SMALL_ICON, smallIcon);
            jsonObject.put(BIG_TEXT, bigText);
            jsonObject.put(BIG_PICTURE_URL, bigPictureUrl);
            jsonObject.put(SUB_TEXT, subText);
            jsonObject.put(SHORTCUT_ID, shortcutId);
            jsonObject.put(NUMBER, number);
            jsonObject.put(CHANNEL_ID, channelId);
            jsonObject.put(SOUND, sound);
            jsonObject.put(COLOR, color);
            jsonObject.put(GROUP, group);
            jsonObject.put(GROUP_SUMMARY, groupSummary);
            jsonObject.put(MESSAGE_ID, messageId);
            jsonObject.put(PLAY_SOUND, playSound);
            jsonObject.put(VIBRATE, vibrate);
            jsonObject.put(VIBRATION, vibration);
            jsonObject.put(ACTIONS, actions);
            jsonObject.put(INVOKE_APP, invokeApp);
            jsonObject.put(TAG, tag);
            jsonObject.put(REPEAT_TYPE, repeatType);
            jsonObject.put(REPEAT_TIME, repeatTime);
            jsonObject.put(WHEN, when);
            jsonObject.put(USES_CHRONOMETER, usesChronometer);
            jsonObject.put(TIMEOUT_AFTER, timeoutAfter);
            jsonObject.put(ONLY_ALERT_ONCE, onlyAlertOnce);
            jsonObject.put(ONGOING, ongoing);
            jsonObject.put(ALLOW_WHILE_IDLE, allowWhileIdle);
            jsonObject.put(IGNORE_IN_FOREGROUND, ignoreInForeground);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Exception while converting RNPushNotificationAttributes to " +
                    "JSON. Returning an empty object", e);
            return new JSONObject();
        }
        return jsonObject;
    }

    @Override
    // For debugging
    public String toString() {
        return "RNPushNotificationAttributes{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", fireDate=" + fireDate +
                ", title='" + title + '\'' +
                ", ticker='" + ticker + '\'' +
                ", showWhen=" + showWhen +
                ", autoCancel=" + autoCancel +
                ", largeIcon='" + largeIcon + '\'' +
                ", largeIconUrl='" + largeIconUrl + '\'' +
                ", smallIcon='" + smallIcon + '\'' +
                ", bigText='" + bigText + '\'' +
                ", subText='" + subText + '\'' +
                ", bigPictureUrl='" + bigPictureUrl + '\'' +
                ", shortcutId='" + shortcutId + '\'' +
                ", number='" + number + '\'' +
                ", channelId='" + channelId + '\'' +
                ", sound='" + sound + '\'' +
                ", color='" + color + '\'' +
                ", group='" + group + '\'' +
                ", groupSummary='" + groupSummary + '\'' +
                ", messageId='" + messageId + '\'' +
                ", playSound=" + playSound +
                ", vibrate=" + vibrate +
                ", vibration=" + vibration +
                ", actions='" + actions + '\'' +
                ", invokeApp=" + invokeApp +
                ", tag='" + tag + '\'' +
                ", repeatType='" + repeatType + '\'' +
                ", repeatTime=" + repeatTime +
                ", when=" + when +
                ", usesChronometer=" + usesChronometer +
                ", timeoutAfter=" + timeoutAfter +
                ", onlyAlertOnce=" + onlyAlertOnce +
                ", ongoing=" + ongoing +
                ", allowWhileIdle=" + allowWhileIdle +
                ", ignoreInForeground=" + ignoreInForeground +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getSound() {
        return sound;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public String getNumber() {
        return number;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public double getFireDate() {
        return fireDate;
    }
}
