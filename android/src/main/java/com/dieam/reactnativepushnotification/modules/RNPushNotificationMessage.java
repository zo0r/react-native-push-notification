package com.dieam.reactnativepushnotification.modules;

public class RNPushNotificationMessage {

  public int notificationID;

  public String sender_id;
  public String sender;

  public String message_id;
  public String message;

  public RNPushNotificationMessage(int notificationID, String sender_id, String sender, String message_id, String message)
  {
    this.notificationID = notificationID;

    this.sender_id = sender_id;
    this.sender = sender;

    this.message_id = message_id;
    this.message = message;
  }
}
