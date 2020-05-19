package com.dieam.reactnativepushnotification.modules;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.dieam.reactnativepushnotification.modules.RNReceivedMessageHandler;

public class RNPushNotificationListenerService extends FirebaseMessagingService {

    private RNReceivedMessageHandler mMessageReceivedHandler = new RNReceivedMessageHandler(this);

    @Override
    public void onNewToken(String token) {
        mMessageReceivedHandler.onNewToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        mMessageReceivedHandler.handleReceivedMessage(message);
    }
}
