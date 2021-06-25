# Trouble shooting

Before submitting an issue please take a moment to read though the following. Most issues are common and some solutions are listed here.

Known bugs and issues:

 * (Android) Tapping an alert in the notification centre will sometimes not result in `onNotification` being called [issue 281](https://github.com/zo0r/react-native-push-notification/issues/281)
 * (Android) Not all local notification features are supported yet (PRs welcome)
 * (iOS) The OS can penalise your app for not calling the completion handler and will stop (or delay) sending notifications to your app. This will be supported from RN-0.38 [PR 227](https://github.com/zo0r/react-native-push-notification/pull/277)
 * (Android and iOS) Don't use a string to get the date for schedule a local notification, it only works with remote debugger enabled, [explanation](https://stackoverflow.com/a/41881765/8519917).
  

  ```javascript
  // It doesn't work with the javascript engine used by React Native 
  const date = new Date("10-10-2020 12:30");
  ```
  A good practice to get valid date could be:

  ```javascript
  // Get date to schedule a local notification today at 12:30:00
  const hour = 12;
  const minute = 30;
  const second = 0;

  const now = new Date();
  const date = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate(),
    hour,
    minute,
    second
  );
  ```
 
# Android tips

 * Use a physical device for remote push notifications. They will not work on an emulator.
 * Try _"grepping"_ logcat for `ReactNativeJS|RNPushNotification` at **debug** level - it will likely shed some light onto what's happening.
 * Your GCM `senderID` can be obtained by obtaining a file from your google console called `google-services.json`.  From this file use the `project_number` as your ID.
 * `Native module cannot be null` error happens when your project isn't _linked_ correctly.  Please re-read the installation instructions, specifically the bit about `react-native link` and `MainApplication.java`.
 * Take a look at the [google docs](https://developers.google.com/cloud-messaging/http-server-ref#notification-payload-support) for more about remote push notifications.
 * Badges do not work on all devices. You should see an error being logged once when the app starts if setting a badge isn't supported.

# iOS tips

 * Use a physical device for remote push notifications. They will not work on a simulator.
 * Add a log statement (`NSLog(@"push-notification received: %@", notification);`) to your `didReceiveRemoteNotification` method in `AppDelegate.m`
 * Look out for `APNS` log messages in the device logs.
  
# About notifications...

There are a number of different types of notifications, and they have subtly different behaviours.  There are essentially 4 types, let's call them _local notifications_ (1), _noisy remote push notifications_ (2) and _silent remote push notifications_ (3).

## 1. local notifications

Local notifications are sent from your JS/RN app and appear as alerts in the notification centre, where they sit until the user removes them.  They can contain text as well as sounds, vibrations, colour, images etc.  Different operating systems support different features.  You can send one by calling the `PushNotification.localNotification` method as described in the docs.  Local notifications can also be scheduled to run at a later date.

If a user taps an alert, your app will be started or brought to the foreground and `onNotification` will be called.

#### Android local notifications

These are highly customisable (more so than _noisy_ remote push notifications) **but** this library doesn't yet support all features, for example you cannot stack notifications using the "grouping" feature.

## 2. _noisy_ remote push notifications

_Noisy_ remote push notifications are sent from a server, such as the Apple Push Notification Service (APNS), or the Google Cloud Messaging Service (GCM).  When the app is in the background, they appear only as alerts in the notification centre and may not interact with your application in any way when they are delivered.  Like local notifications they have a visual (or audible) element.

When a user taps an alert in the notification centre that was created by a _noisy_ remote push notification, your app will be either started or brought to the foreground.  The `onNotification` method will fired.

#### Android _noisy_ remote push notifications

Your server will send something like this to GCM:

```json
{
  "to": "<token>",
  "time_to_live": 86400,
  "collapse_key": "new_message",
  "delay_while_idle": false,
  "notification": {
    "title": "title",
    "body": "this is a noisy test",
    "tag": "new_message",
    "icon": "new_message",
    "color": "#18d821",
    "sound": "default"
  }
}
```

Your app will not be invoked when this is received.

#### iOS _noisy_ remote push notifications

Your server will send something like this to APNS:

```json
{
  "aps": {
    "alert": {
      "body": "the body text",
      "title": "the title"
    },
    "badge": 6,
    "sound": " default"
  }
}
```

Your app will not be invoked if it is running in the background, and the notification will result in an alert in the notification centre.  If you app is running in the foreground, `onNotification` will be called with something like:

```json
{
  "foreground": true,
  "userInteraction": false,
  "message": {
    "title": "the title",
    "body": "the body text"
  },
  "data": {
    "remote": true,
    "notificationId": "A3DC5EEE-FF97-4695-B562-3A7E89E43199"
  },
  "badge": 4,
  "alert": {
    "title": "the title",
    "body": "the body text"
  },
  "sound": " default"
}
```

Tapping the alert in the notification centre will start or bring the app to the foreground and `onNotification` will be called.

## 3. _silent_ remote push notifications

_Silent_ remote push notifications are also sent from a server.  They are delivered to your app but **not** to the notification centre, and as such have no visual or audible content.  When receiving a notification the `onNotification` in your JS app will be called.  The app can be running in the foreground or background, the notification will always be delivered to the app.

Using a _silent_ remote push notifications which in turn creates a customised _local_ notification is a common pattern for providing a rich user experience.

#### Android _silent_ remote push notifications

Your server will send something like this to GCM:

```json
{
  "to": "<token>",
  "time_to_live": 86400,
  "collapse_key": "new_message",
  "delay_while_idle": false,
  "data": {
    "your-key": "your-value"
  }
}
```

The crucial bit is presence of the `data` field.  Your RN/JS app will receive something like:

```json
{
  "foreground": true,
  "your-key": "your-value",
  "google.sent_time": 1478872536263,
  "userInteraction": false,
  "google.message_id": "0:999999999999",
  "collapse_key": "new_message"
}
```

If your Android app is not running when a _silent_ notification is received then this library will start it.  It will be started in the background however, and if the OS starts your app in this way it will not start the react-native lifecycle.  This means that if your notification delivery code relies on the react-native lifecycle then it will not get invoked in this situation.  You need to structure your app in such a way that `PushNotification.configure` gets called as a side effect of merely importing the root `index.android.js` file.

#### iOS _silent_ remote push notifications

Send something like this to the APNS (here are the [docs](https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingwithAPNs.html#//apple_ref/doc/uid/TP40008194-CH11-SW1)):

```json
{
  "aps": {
    "content-available": 1
  },
  "payload": "{\"your-key\":\"your-value\"}"
}
```

This is a _pure_ silent push notification.  It must not include a badge, sound or any alert text.  These types of silent notifications are of limited use.  They MUST be sent with a priority of 5 (10 is the default) and are subject to delays - basically, the OS may delay delivery if the battery is low and the phone isn't plugged in.

You can create an alternative _non-pure_ iOS silent push notification by adding an empty string as the alert body or sound name (see this [discussion](http://stackoverflow.com/questions/19239737/silent-push-notification-in-ios-7-does-not-work)).  This will be delivered as a high priority message and will not be subject to OS imposed delays.  Obviously this is a bit of a hack.  A better approach to silent push notifications is to use [react-native-voip-push-notification](https://github.com/ianlin/react-native-voip-push-notification).

The crucial bit of an iOS silent notification is presence of the `"content-available": 1` field.  Your RN/JS app will receive something like:

```json
{
  "foreground": true,
  "userInteraction": false,
  "data": {
    "remote": true,
    "payload": "{\"your-key\":\"your-value\"}",
    "notificationId": "8D8C24FF-B4F0-4D13-BA0E-295D0E474279"
  }
}
```

After you have processed the notification you must call isn't `finish` method (as of RN 0.38).

#### Some useful links

 * https://devcenter.verivo.com/display/doc/Handling+Push+Notifications+on+iOS
 * https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CreatingtheNotificationPayload.html#//apple_ref/doc/uid/TP40008194-CH10-SW1
 * http://stackoverflow.com/questions/12071726/how-to-use-beginbackgroundtaskwithexpirationhandler-for-already-running-task-in
