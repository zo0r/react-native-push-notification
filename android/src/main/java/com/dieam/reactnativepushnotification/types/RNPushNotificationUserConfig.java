package com.dieam.reactnativepushnotification.types;

import com.dieam.reactnativepushnotification.interfaces.IRNPushNotificationBundleEditor;

public class RNPushNotificationUserConfig {
    private IRNPushNotificationBundleEditor pushNotificationBundleEditor;

    public void setPushNotificationBundleEditor(IRNPushNotificationBundleEditor pushNotificationBundleEditor) {
        this.pushNotificationBundleEditor = pushNotificationBundleEditor;
    }

    public IRNPushNotificationBundleEditor getPushNotificationBundleEditor() {
        return this.pushNotificationBundleEditor;
    }
}
