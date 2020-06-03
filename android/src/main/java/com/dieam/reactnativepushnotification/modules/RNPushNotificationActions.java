package com.dieam.reactnativepushnotification.modules;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationActions extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
      String intentActionPrefix = context.getPackageName() + ".ACTION_";

      Log.i(LOG_TAG, "RNPushNotificationBootEventReceiver loading scheduled notifications");

      if (null == intent.getAction() || !intent.getAction().startsWith(intentActionPrefix)) {
        return;
      }

      final Bundle bundle = intent.getBundleExtra("notification");

      // Dismiss the notification popup.
      NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
      int notificationID = Integer.parseInt(bundle.getString("id"));

      boolean autoCancel = bundle.getBoolean("autoCancel", true);

      if(autoCancel) {
        if (bundle.containsKey("tag")) {
            String tag = bundle.getString("tag");
            manager.cancel(tag, notificationID);
        } else {
            manager.cancel(notificationID);
        }
      }

      boolean invokeApp = bundle.getBoolean("invokeApp", true);

      // Notify the action.
      if(invokeApp) {
          RNPushNotificationHelper helper = new RNPushNotificationHelper((Application) context.getApplicationContext());

          helper.invokeApp(bundle);
      } else {

        // We need to run this on the main thread, as the React code assumes that is true.
        // Namely, DevServerHelper constructs a Handler() without a Looper, which triggers:
        // "Can't create handler inside thread that has not called Looper.prepare()"
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // Construct and load our normal React JS code bundle
                final ReactInstanceManager mReactInstanceManager = ((ReactApplication) context.getApplicationContext()).getReactNativeHost().getReactInstanceManager();
                ReactContext context = mReactInstanceManager.getCurrentReactContext();
                // If it's constructed, send a notification
                if (context != null) {
                    RNPushNotificationJsDelivery mJsDelivery = new RNPushNotificationJsDelivery(context);

                    mJsDelivery.notifyNotificationAction(bundle);
                } else {
                    // Otherwise wait for construction, then send the notification
                    mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                        public void onReactContextInitialized(ReactContext context) {
                            RNPushNotificationJsDelivery mJsDelivery = new RNPushNotificationJsDelivery(context);

                            mJsDelivery.notifyNotificationAction(bundle);
 
                            mReactInstanceManager.removeReactInstanceEventListener(this);
                        }
                    });
                    if (!mReactInstanceManager.hasStartedCreatingInitialContext()) {
                        // Construct it in the background
                        mReactInstanceManager.createReactContextInBackground();
                    }
                }
            }
        });
      }
    }
}