package com.dieam.reactnativepushnotification.modules;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.content.res.Resources;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class RNPushNotificationRegistrationService extends IntentService {

    private static final String TAG = "RNPushNotification";

    public RNPushNotificationRegistrationService() {super(TAG);}

    @Override
    protected void onHandleIntent(Intent intent) {
        Resources resources = getApplication().getResources();
        String packageName = getApplication().getPackageName();
        int resourceId = resources.getIdentifier("gcm_defaultSenderId", "string", packageName);
        String SenderID = getString(resourceId);
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(SenderID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationToken(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRegistrationToken(String token) {
        Intent intent = new Intent("RNPushNotificationRegisteredToken");
        intent.putExtra("token", token);
        sendBroadcast(intent);
    }

}