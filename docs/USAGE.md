# USAGE

Here we describe how you can receive and work with notifications.

## Exemple

### Example app

Example folder contains an example app to demonstrate how to use this package. The notification Handling is done in `NotifService.js`.

Please test your PRs with this example app before submitting them. It'll help maintaining this repo.


**DO NOT USE `.configure()` INSIDE A COMPONENT, EVEN `App`**
> If you do, notification handlers will not fire, because they are not loaded. Instead, use `.configure()` in the app's first file, usually `index.js`.


```javascript
import PushNotificationIOS from "@react-native-community/push-notification-ios";
import PushNotification from "react-native-push-notification";

// Must be outside of any component LifeCycle (such as `componentDidMount`).
PushNotification.configure({
  // (optional) Called when Token is generated (iOS and Android)
  onRegister: function (token) {
    console.log("TOKEN:", token);
  },

  // (required) Called when a remote is received or opened, or local notification is opened
  onNotification: function (notification) {
    console.log("NOTIFICATION:", notification);

    // process the notification

    // (required) Called when a remote is received or opened, or local notification is opened
    notification.finish(PushNotificationIOS.FetchResult.NoData);
  },

  // (optional) Called when Registered Action is pressed and invokeApp is false, if true onNotification will be called (Android)
  onAction: function (notification) {
    console.log("ACTION:", notification.action);
    console.log("NOTIFICATION:", notification);

    // process the action
  },

  // (optional) Called when the user fails to register for remote notifications. Typically occurs when APNS is having issues, or the device is a simulator. (iOS)
  onRegistrationError: function(err) {
    console.error(err.message, err);
  },

  // IOS ONLY (optional): default: all - Permissions to register.
  permissions: {
    alert: true,
    badge: true,
    sound: true,
  },

  // Should the initial notification be popped automatically
  // default: true
  popInitialNotification: true,

  /**
   * (optional) default: true
   * - Specified if permissions (ios) and token (android and ios) will requested or not,
   * - if not, you must call PushNotificationsHandler.requestPermissions() later
   * - if you are not using remote notification or do not have Firebase installed, use this:
   *     requestPermissions: Platform.OS === 'ios'
   */
  requestPermissions: true,
});
```

## Required Channel Management (Android)

To use channels, create them at startup and pass the matching `channelId` through to `PushNotification.localNotification` or `PushNotification.localNotificationSchedule`.

```javascript
import PushNotification, {Importance} from 'react-native-push-notification';
...
  PushNotification.createChannel(
    {
      channelId: "channel-id", // (required)
      channelName: "My channel", // (required)
      channelDescription: "A channel to categorise your notifications", // (optional) default: undefined.
      playSound: false, // (optional) default: true
      soundName: "default", // (optional) See `soundName` parameter of `localNotification` function
      importance: Importance.HIGH, // (optional) default: Importance.HIGH. Int value of the Android notification importance
      vibrate: true, // (optional) default: true. Creates the default vibration pattern if true.
    },
    (created) => console.log(`createChannel returned '${created}'`) // (optional) callback returns whether the channel was created, false means it already existed.
  );
```

**NOTE: Without channel, notifications don't work**

In the notifications options, you must provide a channel id with `channelId: "your-channel-id"`, if the channel doesn't exist the notification might not be triggered. Once the channel is created, the channel cannot be updated. Make sure your `channelId` is different if you change these options. If you have created a channel in another way, it will apply options of the channel.

If you want to use a different default channel for remote notification, refer to the documentation of Firebase:

[Set up a Firebase Cloud Messaging client app on Android](https://firebase.google.com/docs/cloud-messaging/android/client?hl=fr)

```xml
  <meta-data
      android:name="com.google.firebase.messaging.default_notification_channel_id"
      android:value="@string/default_notification_channel_id" />
```

For local notifications, the same kind of option is available:

- you can use:
  ```xml
    <meta-data
        android:name="com.dieam.reactnativepushnotification.default_notification_channel_id"
        android:value="@string/default_notification_channel_id" />
  ```
- If not defined, fallback to the Firebase value defined in the `AndroidManifest`:
  ```xml
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_channel_id"
        android:value="..." />
  ```
- If not defined, fallback to the default Firebase channel id `fcm_fallback_notification_channel`

### List channels

You can list available channels with:

```js
PushNotification.getChannels(function (channel_ids) {
  console.log(channel_ids); // ['channel_id_1']
});
```

### Channel exists

You can check if a channel exists with:

```js
PushNotification.channelExists(channel_id, function (exists) {
  console.log(exists); // true/false
});
```

### Channel blocked

You can check if a channel blocked with:

```js
PushNotification.channelBlocked(channel_id, function (blocked) {
  console.log(blocked); // true/false
});
```

### Delete channel

You can delete a channel with:

```js
PushNotification.deleteChannel(channel_id);
```

## Details of onNotification

### Show notifications while the app is in foreground

If you want a consistent results in Android & iOS with the most flexibility, it is best to handle it manually by prompting a local notification when `onNotification` is triggered by a remote push notification on foreground (check `notification.foreground` prop).

Watch out for an infinite loop triggering `onNotification` - remote & local notification will trigger it. You can overcome this by marking local notifications' data.

### Handling Notifications

When any notification is opened or received the callback `onNotification` is called passing an object with the notification data.

Notification object example:

```javascript
{
    foreground: false, // BOOLEAN: If the notification was received in foreground or not
    userInteraction: false, // BOOLEAN: If the notification was opened by the user from the notification area or not
    message: 'My Notification Message', // STRING: The notification message
    data: {}, // OBJECT: The push data or the defined userInfo in local notifications
}
```