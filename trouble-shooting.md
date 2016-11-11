# Trouble shooting

Before submitting an issue please take a moment to read though the following. Most issues are common and some solutions are listed here.

# Android

 * Use a physical device for remote push notifications. They will not work on an emulator.
 * Try _grepping_ logcat for `ReactNativeJS|RNPushNotification` at **debug** level - it will likely shed some light onto whats happening.
 * Your CGM `senderID` can be obtained by obtaining a file from your google console called `google-services.json`.  From this file use the `project_number` as your ID.
 * `Native module cannot be null` error happens when your project isn't _linked_ correctly.  Please re-read the installation instructions, specifically the bit about `react-native link` and `MainApplication.java`.
 * Take a look at the [google docs](https://developers.google.com/cloud-messaging/http-server-ref#notification-payload-support) for more about remote push notifications.
 * Bages do not work on all devices, you should see an error being logged once when the app starts if the setting a badge isn't supported

# iOS

 * Use a physical device for remote push notifications. They will not work on a simulator.
 * If remote push notifications stop after a while its possible that the OS has penalised your app for not calling the `completionHandler`.  This will be supported from RN-0.38.

# About notifications...

There are a number of different types of notification, and they have subtly different behaviours.  There are essentially 4 types of notification, let's call them _local notifications_ (1), _noisy remote push notifications_ (2), _silent remote push notifications_ (3) and _mixed remote push notifications_ (4).

## 1. local notifications

Local notifications are sent from your JS/RN app to the notification centre, where they sit until either the user removes them.  They can contain text, as well as sounds, vibrations etc.  Different operating systems support different features.  You can send one by calling the `PushNotification.localNotification` method as described in the docs.  Local notifications can also be scheduled to run at a later date.

#### Android local notifications

These are highly customisable (more so than _noisy_ remote push notifications) **but** this library doesn't yet support all the features, such as "grouping".

## 2. _noisy_ remote push notifications

_Noisy_ remote push notifications are sent from a server, such as the Apple Push Notification Service (APNS), or the Google Cloud Messaging Service (GCM).  They appear only in the notification centre and do not interact with your application in any way when they are delivered.  Like local notifications they have a visual (or audible) element.

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

The crucial bit is presence of the _data_ field.  Your app will receive something like:

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

## 4. _mixed_ remote push notifications

_Mixed_ remote push notifications are both delivered to your app AND to the notification center.

#### Android _mixed_ remote push notifications

Android doesn't directly support mixed notifications.  If you try to combine the above approaches you will see a _noisy_ notification but it will not be delivered to your app.  This library does however provide a basic work-around.  By adding `message` field to a _silent_ notification the library will synthesize a local notification as well as deliver a _silent_ notification to your app.  Something like this:

```json
{
  "to": "<token>",
  "time_to_live": 86400,
  "collapse_key": "new_message",
  "delay_while_idle": false,
  "data": {
    "title": "title",
    "message": "this is a mixed test 14:03:29.676",
    "your-key": "your-value"
  }
}
```

The resulting local notification will include the message as well as a few other (optional) fields: _title_, _sound_ and _colour_