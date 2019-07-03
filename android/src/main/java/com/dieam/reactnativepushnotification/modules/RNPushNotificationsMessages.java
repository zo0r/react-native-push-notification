package com.dieam.reactnativepushnotification.modules;

import java.util.ArrayList;
import java.util.HashMap;

public class RNPushNotificationsMessages {

  public int countOfmessage = 0;

  public HashMap<String, ArrayList<RNPushNotificationMessage>> messageHashMap = new HashMap<String, ArrayList<RNPushNotificationMessage>>();

  public RNPushNotificationsMessages()
  {
    clear();
  }

  public void clear()
  {
    countOfmessage = 0;
    messageHashMap.clear();
  }

  public void addMessage(String dialog_id, RNPushNotificationMessage message)
  {
    if (messageHashMap.get(dialog_id) == null)
    {
      ArrayList<RNPushNotificationMessage> newMessageInDialog = new ArrayList<RNPushNotificationMessage>();
      newMessageInDialog.add(message);

      messageHashMap.put(dialog_id, newMessageInDialog);
    } else {
      messageHashMap.get(dialog_id).add(message);
    }

    countOfmessage++;
    System.out.println(messageHashMap);
  }

  public void deleteMessage(String dialog_id, String message_id)
  {
    if (messageHashMap.size() <= 0)
    {
      return;
    }

    if (messageHashMap.get(dialog_id) != null)
    {
      ArrayList<RNPushNotificationMessage> listOfMessages = messageHashMap.get(dialog_id);
      for(int i = 0; i < listOfMessages.size(); i++)
      {
        if (listOfMessages.get(i).message_id == message_id)
        {
          listOfMessages.remove(i);
          countOfmessage--;
          return;
        }
      }
    }
  }

  public int getCountOfDialogs()
  {
    return messageHashMap.size();
  }

  public int getCountOfMessage()
  {
    return countOfmessage;
  }

}
