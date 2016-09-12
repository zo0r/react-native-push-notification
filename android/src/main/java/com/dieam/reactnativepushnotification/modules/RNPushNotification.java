package com.dieam.reactnativepushnotification.modules;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import main.java.com.dieam.reactnativepushnotification.modules.RNPushNotificationQueue;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RNPushNotification extends ReactContextBaseJavaModule implements ActivityEventListener {
    private RNPushNotificationHelper mRNPushNotificationHelper;

    public RNPushNotification(ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addActivityEventListener(this);
        mRNPushNotificationHelper = new RNPushNotificationHelper((Application) reactContext.getApplicationContext());
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

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("RN EVENT LISTENER", "onNewIntent");

        if (intent.hasExtra("notification")) {
            Bundle bundle = intent.getBundleExtra("notification");
            bundle.putBoolean("foreground", false);
            intent.putExtra("notification", bundle);
            notifyNotification(bundle);
        }
    }

    private void registerNotificationsRegistration() {
        IntentFilter intentFilter = new IntentFilter("RNPushNotificationRegisteredToken");

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
        IntentFilter intentFilter = new IntentFilter("RNPushNotificationReceiveNotification");
        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               notifyNotification(intent.getBundleExtra("notification"));
            }
        }, intentFilter);
    }

    private void notifyNotification(Bundle bundle) {
        Log.d("RN EVENT LISTENER", "notifyNotification");


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
    public void getInitialNotification(Promise promise) {

        Log.d("RN EVENT LISTENER", "getInitialNotification");

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

            RNPushNotificationQueue.getInstance().push(intent);
        }
        promise.resolve(params);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Ignored, required to implement ActivityEventListener
    }

    @ReactMethod
    public void onLoad() {
        Log.d("RN EVENT LISTENER", "I have loaded!!!");

        if(!RNPushNotificationQueue.getInstance().isEmpty())
        {
            Intent intent = RNPushNotificationQueue.getInstance().pop();
            Log.d("RN EVENT LISTENER", "intent to string: " + intent.toString());
            Log.d("RN EVENT LISTENER", "intent extras: " + intent.getExtras().toString());
            new RNPushNotificationHelper(getCurrentActivity().getApplication()).sendNotification(intent.getBundleExtra("notification"));
        }


        RNPushNotificationQueue.getInstance().setLoaded(true);
        System.out.println("I have loaded!");
    }
}
