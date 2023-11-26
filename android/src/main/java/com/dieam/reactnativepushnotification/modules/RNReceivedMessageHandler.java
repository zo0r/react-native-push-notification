package com.dieam.reactnativepushnotification.modules;

import android.os.Bundle;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;

import java.security.SecureRandom;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNReceivedMessageHandler {

    public void handleReceivedMessage(Bundle message) {
        String from = message.getString("from");
        final Bundle bundle = new Bundle();

        if (message != null) {
            bundle.putString("from", from);
            bundle.putAll(message);
        }

        Log.v(LOG_TAG, "onMessageReceived: " + bundle);

        handleRemotePushNotification((ReactApplicationContext) context, bundle);
    }

    private void handleRemotePushNotification(ReactApplicationContext context, Bundle bundle) {

        // If notification ID is not provided by the user for push notification, generate one at random
        if (bundle.getString("id") == null) {
            SecureRandom randomNumberGenerator = new SecureRandom();
            bundle.putString("id", String.valueOf(randomNumberGenerator.nextInt()));
        }

        Application applicationContext = (Application) context.getApplicationContext();

        RNPushNotificationHelper pushNotificationHelper = new RNPushNotificationHelper(applicationContext);

        boolean isForeground = pushNotificationHelper.isApplicationInForeground();

        RNPushNotificationJsDelivery jsDelivery = new RNPushNotificationJsDelivery(context);
        bundle.putBoolean("foreground", isForeground);
        bundle.putBoolean("userInteraction", false);
        jsDelivery.notifyNotification(bundle);

        if (config.getNotificationForeground() || !isForeground) {
            Log.v(LOG_TAG, "sendNotification: " + bundle);

            pushNotificationHelper.sendToNotificationCentre(bundle);
        }
    }
}