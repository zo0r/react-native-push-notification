package com.dieam.reactnativepushnotification.modules;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationRegistrationService extends IntentService {
  private static final String TAG = "RNPushNotification";

  public RNPushNotificationRegistrationService() {
      super(TAG);

      Log.w(LOG_TAG, TAG + " RNPushNotificationRegistrationService is not needed anymore.");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.w(LOG_TAG, TAG + " RNPushNotificationRegistrationService is not needed anymore.");
  }
}
