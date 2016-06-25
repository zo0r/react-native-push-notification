package com.dieam.reactnativepushnotification.modules;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.*;

import android.content.Context;
import android.util.Log;

public class RNPushNotification extends ReactContextBaseJavaModule {
    private ReactContext mReactContext;
    private Activity mActivity;
    private RNPushNotificationHelper mRNPushNotificationHelper;

    public RNPushNotification(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);

        mActivity = activity;
        mReactContext = reactContext;
        mRNPushNotificationHelper = new RNPushNotificationHelper(activity.getApplication(), reactContext);
        registerNotificationsRegistration();
        registerNotificationsReceiveNotification();
    }

    @Override
    public String getName() {
        return "RNPushNotification";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        Intent intent = mActivity.getIntent();

        Bundle bundle = intent.getBundleExtra("notification");
        if ( bundle != null ) {
            bundle.putBoolean("foreground", false);
            String bundleString = convertJSON(bundle);
            constants.put("initialNotification", bundleString);
        }

        return constants;
    }

    private void sendEvent(String eventName, Object params) {
        if ( mReactContext.hasActiveCatalystInstance() ) {
            mReactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    public void newIntent(Intent intent) {
        if ( intent.hasExtra("notification") ) {
            Bundle bundle = intent.getBundleExtra("notification");
            bundle.putBoolean("foreground", false);
            intent.putExtra("notification", bundle);
            notifyNotification(bundle);
        }
    }

    private void registerNotificationsRegistration() {
        IntentFilter intentFilter = new IntentFilter("RNPushNotificationRegisteredToken");

        mReactContext.registerReceiver(new BroadcastReceiver() {
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
        IntentFilter intentFilter = new IntentFilter("RNPushNotificationReceiveNotification");
        mReactContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyNotification(intent.getBundleExtra("notification"));
            }
        }, intentFilter);
    }

    private void notifyNotification(Bundle bundle) {
        String bundleString = convertJSON(bundle);

        WritableMap params = Arguments.createMap();
        params.putString("dataJSON", bundleString);

        sendEvent("remoteNotificationReceived", params);
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
            } catch(JSONException e) {
                return null;
            }
        }
        return json.toString();
    }

    @ReactMethod
    public void requestPermissions(String senderID) {
        Intent GCMService = new Intent(mReactContext, RNPushNotificationRegistrationService.class);

        GCMService.putExtra("senderID", senderID);
        mReactContext.startService(GCMService);
    }

    @ReactMethod
    public void cancelAllLocalNotifications() {
        mRNPushNotificationHelper.cancelAll();
    }

    @ReactMethod
    public void presentLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        mRNPushNotificationHelper.sendNotification(bundle);
    }

    @ReactMethod
    public void scheduleLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        mRNPushNotificationHelper.sendNotificationScheduled(bundle);
    }

    @ReactMethod
    public void cancelLocalNotifications(ReadableMap details) {
        String notificationId = details.getString("notificationId");
        Log.i("Notification", "Deleting notification with ID " + notificationId);
        mRNPushNotificationHelper.cancelNotification(notificationId);
    }
}