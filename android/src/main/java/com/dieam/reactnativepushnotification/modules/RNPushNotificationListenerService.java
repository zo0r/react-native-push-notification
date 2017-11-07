package com.dieam.reactnativepushnotification.modules;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dieam.reactnativepushnotification.helpers.ApplicationBadgeHelper;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, final Bundle bundle) {
        // {google.sent_time=1509616201546,
        // custom={"a":{"additionalData2":"456","additionalData1":"123"},"i":"3e526372-f895-406e-ad6f-ab0cb2251e98"},
        // o=[{"p":"HELLO_ICON","i":"1","n":"HELLO"},{"p":"XIN_CHAO_ICON","i":"2","n":"XIN CHAO"}],
        // bgn=1,
        // grp=groupkey,
        // pri=5,
        // vis=1,
        // bgac=accentcolor,
        // ledc=ledcolor,
        // alert=THIS IS TEST MESSAGEEEEEEE,
        // bicon=bigpicture,
        // licon=largeicon,
        // sicon=smallicon,
        // sound=sound,
        // title=TEST NOTIFICATIOn,
        // grp_msg=groupmessage,
        // google.message_id=0:1509616201550812%b9f27667b1063f92,
        // collapse_key=collapsekey}

        JSONObject osCustomdata = getPushData(bundle.getString("custom"));

        // Check if the notification is from OneSignal by checking 'custom' attribute, if it exists so the notification is came from OneSignal
        if (osCustomdata != null) { 
            if (!bundle.containsKey("message")) {
                String message = bundle.getString("alert");
                bundle.putString("message", message != null ? message : "Notification Received");
            }
            
            bundle.putString("color", bundle.getString("bgac"));

            // OneSignal Actions
            JSONArray actions = null;
            
            if(bundle.getString("o") != null) {
                try {
                    actions = new JSONArray(bundle.getString("o"));
                } 
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            
            if (actions != null) {
                JSONArray newActions = new JSONArray();
                for (int i = 0 ; i < actions.length(); i++) {
                    try {
                        JSONObject obj = actions.getJSONObject(i);
                        newActions.put(obj.getString("i"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                bundle.putString("actions", newActions.toString());
            }

            // Sound
            bundle.putString("soundName", "default");

            bundle.putString("mId", osCustomdata.optString("i"));

            // OneSignal Additional Data
            String additionalData = osCustomdata.optString("a", null);

            if (additionalData != null) {
                bundle.putString("data", additionalData);
            }

            bundle.remove("custom"); // custom data
            bundle.remove("alert"); // message
            bundle.remove("bgac"); // accent color
            bundle.remove("ledc"); // LED color
            bundle.remove("o"); // actions
            bundle.remove("grp"); // group key
            bundle.remove("grp_msg"); // group message
            bundle.remove("bicon"); // big picture
            bundle.remove("licon"); // large icon
            bundle.remove("sicon"); // small icon
            bundle.remove("sound"); // sound
            bundle.remove("collapse_key"); // collapse key
        } else {
            JSONObject data = getPushData(bundle.getString("data"));
            if (data != null) {
                if (!bundle.containsKey("message")) {
                    bundle.putString("message", data.optString("alert", "Notification received"));
                }
                if (!bundle.containsKey("title")) {
                    bundle.putString("title", data.optString("title", null));
                }
                if (!bundle.containsKey("sound")) {
                    bundle.putString("soundName", data.optString("sound", null));
                }
                if (!bundle.containsKey("color")) {
                    bundle.putString("color", data.optString("color", null));
                }

                final int badge = data.optInt("badge", -1);
                if (badge >= 0) {
                    ApplicationBadgeHelper.INSTANCE.setApplicationIconBadgeNumber(this, badge);
                }
            }
        }

        Log.v(LOG_TAG, "onMessageReceived: " + bundle);

        // We need to run this on the main thread, as the React code assumes that is true.
        // Namely, DevServerHelper constructs a Handler() without a Looper, which triggers:
        // "Can't create handler inside thread that has not called Looper.prepare()"
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // Construct and load our normal React JS code bundle
                ReactInstanceManager mReactInstanceManager = ((ReactApplication) getApplication()).getReactNativeHost().getReactInstanceManager();
                ReactContext context = mReactInstanceManager.getCurrentReactContext();
                // If it's constructed, send a notification
                if (context != null) {
                    handleRemotePushNotification((ReactApplicationContext) context, bundle);
                } else {
                    // Otherwise wait for construction, then send the notification
                    mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                        public void onReactContextInitialized(ReactContext context) {
                            handleRemotePushNotification((ReactApplicationContext) context, bundle);
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

    private JSONObject getPushData(String dataString) {
        try {
            return new JSONObject(dataString);
        } catch (Exception e) {
            return null;
        }
    }

    private void handleRemotePushNotification(ReactApplicationContext context, Bundle bundle) {

        // If notification ID is not provided by the user for push notification, generate one at random
        if (bundle.getString("id") == null) {
            Random randomNumberGenerator = new Random(System.currentTimeMillis());
            bundle.putString("id", String.valueOf(randomNumberGenerator.nextInt()));
        }

        Boolean isForeground = isApplicationInForeground();

        RNPushNotificationJsDelivery jsDelivery = new RNPushNotificationJsDelivery(context);
        bundle.putBoolean("foreground", isForeground);
        bundle.putBoolean("userInteraction", false);
        jsDelivery.notifyNotification(bundle);

        // If contentAvailable is set to true, then send out a remote fetch event
        if (bundle.getString("contentAvailable", "false").equalsIgnoreCase("true")) {
            jsDelivery.notifyRemoteFetch(bundle);
        }

        Log.v(LOG_TAG, "sendNotification: " + bundle);

        if (!isForeground) {
            Application applicationContext = (Application) context.getApplicationContext();
            RNPushNotificationHelper pushNotificationHelper = new RNPushNotificationHelper(applicationContext);
            pushNotificationHelper.sendToNotificationCentre(bundle);
        }
    }

    private boolean isApplicationInForeground() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos != null) {
            for (RunningAppProcessInfo processInfo : processInfos) {
                if (processInfo.processName.equals(getApplication().getPackageName())) {
                    if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String d : processInfo.pkgList) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
