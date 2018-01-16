package com.dieam.reactnativepushnotification.modules;

import android.content.Intent;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import javax.annotation.Nullable;

public class RNPushNotificationActionService extends HeadlessJsTaskService {
  @Nullable
  @Override
  protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    return new HeadlessJsTaskConfig(
        "RNPushNotificationActionHandlerTask",
        Arguments.fromBundle(intent.getExtras()),
        5000,
        true
    );
  }
}
