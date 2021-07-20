package com.dieam.reactnativepushnotification.modules;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.dieam.reactnativepushnotification.helpers.ApplicationBadgeHelper;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class RNPushNotification extends ReactContextBaseJavaModule implements ActivityEventListener {
    public static final String LOG_TAG = "RNPushNotification";// all logging should use this tag
    public static final String KEY_TEXT_REPLY = "key_text_reply";

    public interface RNIntentHandler {
        void onNewIntent(Intent intent);
  
        @Nullable
        Bundle getBundleFromIntent(Intent intent);
    }
  
    public static ArrayList<RNIntentHandler> IntentHandlers = new ArrayList();

    private RNPushNotificationHelper mRNPushNotificationHelper;
    private final SecureRandom mRandomNumberGenerator = new SecureRandom();
    private RNPushNotificationJsDelivery mJsDelivery;

    public RNPushNotification(ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addActivityEventListener(this);

        Application applicationContext = (Application) reactContext.getApplicationContext();

        // The @ReactNative methods use this
        mRNPushNotificationHelper = new RNPushNotificationHelper(applicationContext);
        // This is used to delivery callbacks to JS
        mJsDelivery = new RNPushNotificationJsDelivery(reactContext);
    }

    @Override
    public String getName() {
        return "ReactNativePushNotification";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        return constants;
    }

    private Bundle getBundleFromIntent(Intent intent) {
        Bundle bundle = null;
        if (intent.hasExtra("notification")) {
            bundle = intent.getBundleExtra("notification");
        } else if (intent.hasExtra("google.message_id")) {
            bundle = new Bundle();

            bundle.putBundle("data", intent.getExtras());
        }

        if (bundle == null) {
            for (RNIntentHandler handler : IntentHandlers) {
                bundle = handler.getBundleFromIntent(intent);
            }
        }

        if(null != bundle && !bundle.getBoolean("foreground", false) && !bundle.containsKey("userInteraction")) {
          bundle.putBoolean("userInteraction", true);
        }

        return bundle;
    }

    @Override
    public void onNewIntent(Intent intent) {
        for (RNIntentHandler handler : IntentHandlers) {
            handler.onNewIntent(intent);
        }
        
        Bundle bundle = this.getBundleFromIntent(intent);
        if (bundle != null) {
            mJsDelivery.notifyNotification(bundle);
        }
    }

    @ReactMethod
    public void invokeApp(ReadableMap data) {
        Bundle bundle = null;

        if (data != null) {
            bundle = Arguments.toBundle(data);
        }

        mRNPushNotificationHelper.invokeApp(bundle);
    }

    @ReactMethod
    public void checkPermissions(Promise promise) {
        ReactContext reactContext = getReactApplicationContext();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(reactContext);
        promise.resolve(managerCompat.areNotificationsEnabled());
    }

    @ReactMethod
    public void requestPermissions() {
      final RNPushNotificationJsDelivery fMjsDelivery = mJsDelivery;
      
      FirebaseMessaging.getInstance().getToken()
              .addOnCompleteListener(new OnCompleteListener<String>() {
                  @Override
                  public void onComplete(@NonNull Task<String> task) {
                      if (!task.isSuccessful()) {
                          Log.e(LOG_TAG, "exception", task.getException());
                          return;
                      }

                      WritableMap params = Arguments.createMap();
                      params.putString("deviceToken", task.getResult());
                      fMjsDelivery.sendEvent("remoteNotificationsRegistered", params);
                  }
              });
    }

    @ReactMethod
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }
    
    @ReactMethod
    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }

    @ReactMethod
    public void presentLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        // If notification ID is not provided by the user, generate one at random
        if (bundle.getString("id") == null) {
            bundle.putString("id", String.valueOf(mRandomNumberGenerator.nextInt()));
        }
        mRNPushNotificationHelper.sendToNotificationCentre(bundle);
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
            Bundle bundle = this.getBundleFromIntent(activity.getIntent());
            if (bundle != null) {
                bundle.putBoolean("foreground", false);
                String bundleString = mJsDelivery.convertJSON(bundle);
                params.putString("dataJSON", bundleString);
            }
        }
        promise.resolve(params);
    }

    @ReactMethod
    public void setApplicationIconBadgeNumber(int number) {
        ApplicationBadgeHelper.INSTANCE.setApplicationIconBadgeNumber(getReactApplicationContext(), number);
    }

    // removed @Override temporarily just to get it working on different versions of RN
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    // removed @Override temporarily just to get it working on different versions of RN
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Ignored, required to implement ActivityEventListener for RN 0.33
    }

    @ReactMethod
    /**
     * Cancels all scheduled local notifications, and removes all entries from the notification
     * centre.
     *
     */
    public void cancelAllLocalNotifications() {
        mRNPushNotificationHelper.cancelAllScheduledNotifications();
        mRNPushNotificationHelper.clearNotifications();
    }

    @ReactMethod
    /**
     * Cancel scheduled notifications, and removes notifications from the notification centre.
     *
     */
    public void cancelLocalNotifications(ReadableMap userInfo) {
        mRNPushNotificationHelper.cancelScheduledNotification(userInfo);
    }

    @ReactMethod
    /**
     * Clear notification from the notification centre.
     */
    public void clearLocalNotification(String tag, int notificationID) {
        mRNPushNotificationHelper.clearNotification(tag, notificationID);
    }

    @ReactMethod
    /**
     * Clears all notifications from the notification center
     *
     */
    public void removeAllDeliveredNotifications() {
      mRNPushNotificationHelper.clearNotifications();
    }

    @ReactMethod
    /**
     * Returns a list of all notifications currently in the Notification Center
     */
    public void getDeliveredNotifications(Callback callback) {
        callback.invoke(mRNPushNotificationHelper.getDeliveredNotifications());
    }

    @ReactMethod
    /**
     * Returns a list of all currently scheduled notifications
     */
    public void getScheduledLocalNotifications(Callback callback) {
        callback.invoke(mRNPushNotificationHelper.getScheduledLocalNotifications());
    }

    @ReactMethod
    /**
     * Removes notifications from the Notification Center, whose id matches
     * an element in the provided array
     */
    public void removeDeliveredNotifications(ReadableArray identifiers) {
      mRNPushNotificationHelper.clearDeliveredNotifications(identifiers);
    }

    @ReactMethod
    /**
     * Unregister for all remote notifications received
     */
    public void abandonPermissions() {
      FirebaseMessaging.getInstance().deleteToken();
      Log.i(LOG_TAG, "InstanceID deleted");
    }

    @ReactMethod
    /**
     * List all channels id
     */
    public void getChannels(Callback callback) {
      WritableArray array = Arguments.fromList(mRNPushNotificationHelper.listChannels());
      
      if(callback != null) {
        callback.invoke(array);
      }
    }

    @ReactMethod
    /**
     * Check if channel exists with a given id
     */
    public void channelExists(String channel_id, Callback callback) {
      boolean exists = mRNPushNotificationHelper.channelExists(channel_id);

      if(callback != null) {
        callback.invoke(exists);
      }
    }

    @ReactMethod
    /**
     * Creates a channel if it does not already exist. Returns whether the channel was created.
     */
    public void createChannel(ReadableMap channelInfo, Callback callback) {
      boolean created = mRNPushNotificationHelper.createChannel(channelInfo);

      if(callback != null) {
        callback.invoke(created);
      }
    }

    @ReactMethod
    /**
     * Check if channel is blocked with a given id
     */
    public void channelBlocked(String channel_id, Callback callback) {
      boolean blocked = mRNPushNotificationHelper.channelBlocked(channel_id);

      if(callback != null) {
        callback.invoke(blocked);
      }
    }

    @ReactMethod
    /**
     * Delete channel with a given id
     */
    public void deleteChannel(String channel_id) {
      mRNPushNotificationHelper.deleteChannel(channel_id);
    }
}
