package com.dieam.reactnativepushnotification.modules;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import com.dieam.reactnativepushnotification.helpers.ApplicationBadgeHelper;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RNPushNotification extends ReactContextBaseJavaModule implements ActivityEventListener {
    public static final String LOG_TAG = "RNPushNotification";// all logging should use this tag

    private RNPushNotificationHelper mRNPushNotificationHelper;
    private final Random mRandomNumberGenerator = new Random(System.currentTimeMillis());

    public RNPushNotification(ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addActivityEventListener(this);

        Application applicationContext = (Application) reactContext.getApplicationContext();
        mRNPushNotificationHelper = new RNPushNotificationHelper(applicationContext);

        registerNotificationsRegistration();
        registerNotificationsReceiveNotification();
        registerNotificationsRemoteFetch();
    }

    @Override
    public String getName() {
        return "RNPushNotification";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        return constants;
    }

    private void sendEvent(String eventName, Object params) {
        ReactContext reactContext = getReactApplicationContext();

        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    public void onNewIntent(Intent intent) {
        if (intent.hasExtra("notification")) {
            Bundle bundle = intent.getBundleExtra("notification");
            bundle.putBoolean("foreground", false);
            intent.putExtra("notification", bundle);
            notifyNotification(bundle);
        }
    }

    private void registerNotificationsRegistration() {
        IntentFilter intentFilter = new IntentFilter(getReactApplicationContext().getPackageName() + ".RNPushNotificationRegisteredToken");

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String token = intent.getStringExtra("token");
                WritableMap params = Arguments.createMap();
                params.putString("deviceToken", token);

                sendEvent("remoteNotificationsRegistered", params);
            }
        }, intentFilter);
    }

    private void registerNotificationsReceiveNotification() {
        IntentFilter intentFilter = new IntentFilter(getReactApplicationContext().getPackageName() + ".RNPushNotificationReceiveNotification");
        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyNotification(intent.getBundleExtra("notification"));
            }
        }, intentFilter);
    }

    private void registerNotificationsRemoteFetch() {
        IntentFilter intentFilter = new IntentFilter(getReactApplicationContext().getPackageName() + ".RNPushNotificationRemoteFetch");
        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getBundleExtra("notification");
                String bundleString = convertJSON(bundle);
                WritableMap params = Arguments.createMap();
                params.putString("dataJSON", bundleString);
                sendEvent("remoteFetch", params);
            }
        }, intentFilter);
    }

    private void notifyNotification(Bundle bundle) {
        String bundleString = convertJSON(bundle);

        WritableMap params = Arguments.createMap();
        params.putString("dataJSON", bundleString);

        sendEvent("remoteNotificationReceived", params);
    }

    private void registerNotificationsReceiveNotificationActions(ReadableArray actions) {
        IntentFilter intentFilter = new IntentFilter();
        // Add filter for each actions.
        for (int i = 0; i < actions.size(); i++) {
            String action = actions.getString(i);
            intentFilter.addAction(getReactApplicationContext().getPackageName() + "." + action);
        }
        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getBundleExtra("notification");

                // Notify the action.
                notifyNotificationAction(bundle);

                // Dismiss the notification popup.
                NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                int notificationID = Integer.parseInt(bundle.getString("id"));
                manager.cancel(notificationID);
            }
        }, intentFilter);
    }

    private void notifyNotificationAction(Bundle bundle) {
        String bundleString = convertJSON(bundle);

        WritableMap params = Arguments.createMap();
        params.putString("dataJSON", bundleString);

        sendEvent("notificationActionReceived", params);
    }

    private String convertJSON(Bundle bundle) {
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    json.put(key, JSONObject.wrap(bundle.get(key)));
                } else {
                    json.put(key, bundle.get(key));
                }
            } catch (JSONException e) {
                return null;
            }
        }
        return json.toString();
    }

    @ReactMethod
    public void requestPermissions(String senderID) {
        ReactContext reactContext = getReactApplicationContext();

        Intent GCMService = new Intent(reactContext, RNPushNotificationRegistrationService.class);

        GCMService.putExtra("senderID", senderID);
        reactContext.startService(GCMService);
    }

    @ReactMethod
    public void presentLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        // If notification ID is not provided by the user, generate one at random
        if (bundle.getString("id") == null) {
            bundle.putString("id", String.valueOf(mRandomNumberGenerator.nextInt()));
        }
        mRNPushNotificationHelper.sendNotification(bundle);
    }

    @ReactMethod
    public void scheduleLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        // If notification ID is not provided by the user, generate one at random
        if (bundle.getString("id") == null) {
            bundle.putString("id", String.valueOf(mRandomNumberGenerator.nextInt()));
        }
        mRNPushNotificationHelper.sendNotificationScheduled(bundle);
    }

    @ReactMethod
    public void getInitialNotification(Promise promise) {
        WritableMap params = Arguments.createMap();
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Intent intent = activity.getIntent();
            Bundle bundle = intent.getBundleExtra("notification");
            if (bundle != null) {
                bundle.putBoolean("foreground", false);
                String bundleString = convertJSON(bundle);
                params.putString("dataJSON", bundleString);
            }
        }
        promise.resolve(params);
    }

    @ReactMethod
    public void setApplicationIconBadgeNumber(int number) {
        ApplicationBadgeHelper.INSTANCE.setApplicationIconBadgeNumber(getReactApplicationContext(), number);
    }

    // removed @Override temporarily just to get the buld stable
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // Ignored, required to implement ActivityEventListener for RN < 0.33
    }

    // removed @Override temporarily just to get the buld stable
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Ignored, required to implement ActivityEventListener for RN 0.33
    }

    @ReactMethod
    /**
     * Cancels all *scheduled* localNotifications.
     *
     * @see <a href="https://facebook.github.io/react-native/docs/pushnotificationios.html">rn docs</a>
     */
    public void cancelAllLocalNotifications() {
        mRNPushNotificationHelper.cancelAllScheduledNotifications();
    }

    @ReactMethod
    /**
     * Cancel local notifications. Optionally restricts the set of canceled notifications to those notifications to a single id.
     *
     * If the id is not specified, this has the effect of clearing the notification cetner only.  No scheduled
     * notifications will be cancelled.
     *
     * If the id is specified, that single notification will be remove, and if it is a scheduled
     * notification then it will be cancelled.
     *
     * I think the react docs are unclear as to what the exact behaviour of this method should be.  I think
     * this implementation provides a good amount of functionality.
     *
     * @see <a href="https://facebook.github.io/react-native/docs/pushnotificationios.html">rn docs</a>
     */
    public void cancelLocalNotifications(ReadableMap details) {
        String notificationIdString = details.getString("id");

        if(notificationIdString != null) {
            mRNPushNotificationHelper.clearNotifications();
        } else {
            mRNPushNotificationHelper.clearNotification(notificationIdString);
            mRNPushNotificationHelper.cancelScheduledNotification(notificationIdString);
        }
    }

    @ReactMethod
    public void registerNotificationActions(ReadableArray actions) {
        registerNotificationsReceiveNotificationActions(actions);
    }
}
