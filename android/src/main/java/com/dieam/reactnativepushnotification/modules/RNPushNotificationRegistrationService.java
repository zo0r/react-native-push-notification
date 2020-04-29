package com.dieam.reactnativepushnotification.modules;

import android.app.IntentService;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationRegistrationService extends IntentService {
  private static final String TAG = "RNPushNotification";

  public RNPushNotificationRegistrationService() {
      super(TAG);

      Log.w(LOG_TAG, TAG + " RNPushNotificationRegistrationService is not needed anymore.");
  }
}
