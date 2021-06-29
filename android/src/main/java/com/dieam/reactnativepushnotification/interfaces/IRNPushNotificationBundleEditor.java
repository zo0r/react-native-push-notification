package com.dieam.reactnativepushnotification.interfaces;

import android.os.Bundle;

import com.google.firebase.messaging.RemoteMessage;

public interface IRNPushNotificationBundleEditor {
    public void editBundle(Bundle bundle, RemoteMessage message);
}
