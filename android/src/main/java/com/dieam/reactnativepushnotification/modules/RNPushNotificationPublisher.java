package com.dieam.reactnativepushnotification.modules;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.ReactApplication;

import java.util.List;
import java.security.SecureRandom;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationPublisher extends BroadcastReceiver {
    final static String NOTIFICATION_ID = "notificationId";

    @Override
    public void onReceive(final Context context, Intent intent) {
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        long currentTime = System.currentTimeMillis();

        Log.i(LOG_TAG, "NotificationPublisher: Prepare To Publish: " + id + ", Now Time: " + currentTime);

        final Bundle bundle = intent.getExtras();

        Log.v(LOG_TAG, "onMessageReceived: " + bundle);

        handleLocalNotification(context, bundle);
    }

    private void handleLocalNotification(Context context, Bundle bundle) {

        // If notification ID is not provided by the user for push notification, generate one at random
        if (bundle.getString("id") == null) {
            SecureRandom randomNumberGenerator = new SecureRandom();
            bundle.putString("id", String.valueOf(randomNumberGenerator.nextInt()));
        }

        Application applicationContext = (Application) context.getApplicationContext();
        RNPushNotificationHelper pushNotificationHelper = new RNPushNotificationHelper(applicationContext);

        boolean isForeground = pushNotificationHelper.isApplicationInForeground();
        bundle.putBoolean("foreground", isForeground);
        bundle.putBoolean("userInteraction", false);

        if (isForeground) {
            final ReactInstanceManager mReactInstanceManager = ((ReactApplication) context.getApplicationContext()).getReactNativeHost().getReactInstanceManager();
            ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
            RNPushNotificationJsDelivery jsDelivery = new RNPushNotificationJsDelivery(reactContext);
            jsDelivery.notifyNotification(bundle);
        }
        
        Log.v(LOG_TAG, "sendNotification: " + bundle);

        pushNotificationHelper.sendToNotificationCentre(bundle);
    }
}