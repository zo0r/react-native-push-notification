package com.dieam.reactnativepushnotification.modules;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationRegistrationService extends IntentService {

    private static final String TAG = "RNPushNotification";

    public RNPushNotificationRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String SenderID = intent.getStringExtra("senderID");
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(SenderID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationToken(token);
        } catch (Exception e) {
            sendError(e);
        }
    }

    private void sendRegistrationToken(String token) {
        Intent intent = new Intent(this.getPackageName() + ".RNPushNotificationRegisteredToken");
        intent.putExtra("token", token);
        sendBroadcast(intent);
    }

    private void sendError(Exception e) {
        Intent intent = new Intent(this.getPackageName() + ".RNPushNotificationError");
        intent.putExtra("exception", e.toString());
        intent.putExtra("tag", TAG);
        sendBroadcast(intent);
    }
}
