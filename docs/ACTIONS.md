
## Notification Actions

(Android Only)

This is done by specifying an `actions` parameters while configuring the local notification. This is an array of strings where each string is a notification action that will be presented with the notification.

For e.g. `actions: ['Accept', 'Reject']`

When you handle actions in background (`invokeApp: false`), you can open the application and pass the initial notification by using use `PushNotification.invokeApp(notification)`.

Make sure you have the receiver in `AndroidManifest.xml`:

```xml
  <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationActions" />
```

Notifications with inline reply: 

You must register an action as "ReplyInput", this will show in the notifications an input to write in. 

EXAMPLE:
```javascript
PushNotification.localNotificationSchedule({
  message: "My Notification Message", // (required)
  date: new Date(Date.now() + (60 * 1000)), // in 60 secs
  actions: ["ReplyInput"],
  reply_placeholder_text: "Write your response...", // (required)
  reply_button_text: "Reply" // (required)
});
```

To get the text from the notification: 

```javascript
...
if(notification.action === "ReplyInput"){
  console.log("texto", notification.reply_text)// this will contain the inline reply text. 
}
...
```

For iOS, you can use:

```javascript
PushNotification.setNotificationCategories(categories);
```

And use the `category` field in the notification.

Documentation [here](https://github.com/react-native-push-notification-ios/push-notification-ios#how-to-perform-different-action-based-on-user-selected-action) to add notification actions.