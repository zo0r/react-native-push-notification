# React Native Push Notifications

[![npm version](https://badge.fury.io/js/react-native-push-notification.svg?update=9)](http://badge.fury.io/js/react-native-push-notification)
[![npm downloads](https://img.shields.io/npm/dm/react-native-push-notification.svg?update=9)](http://badge.fury.io/js/react-native-push-notification)

React Native Local and Remote Notifications for iOS and Android


## 🎉 Version 6.x is live ! 🎉

Check out for changes and migration in the CHANGELOG:

[Changelog](https://github.com/zo0r/react-native-push-notification/blob/master/CHANGELOG.md)

# Supporting the project

Maintaining this project takes time. To help allocate time, you can Buy Me a Coffee :wink:

<a href="https://www.buymeacoffee.com/Dallas62" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-blue.png" alt="Buy Me A Coffee" style="height: 51px !important;width: 217px !important;" ></a>

## Supported React Native Versions

| Component Version | RN Versions          | README                                                                                                                 |
| ----------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **1.0.7**         | **<= 0.27**          | [Open](https://github.com/zo0r/react-native-push-notification/blob/f42723817f1687e0da23e6753eb8a9f0385b6ac5/README.md) |
| **1.0.8**         | **0.28**             | [Open](https://github.com/zo0r/react-native-push-notification/blob/2eafd1961273ca6a82ad4dd6514fbf1d1a829089/README.md) |
| **2.0.1**         | **0.29**             | [Open](https://github.com/zo0r/react-native-push-notification/blob/c7ab7cd84ea19e42047379aefaf568bb16a81936/README.md) |
| **2.0.2**         | **0.30, 0.31, 0.32** | [Open](https://github.com/zo0r/react-native-push-notification/blob/a0f7d44e904ba0b92933518e5bf6b444f1c90abb/README.md) |
| **>= 2.1.0**      | **>= 0.33**          | [Open](https://github.com/zo0r/react-native-push-notification/blob/a359e5c00954aa324136eaa9808333d6ca246171/README.md) |

## Changelog

Changelog is available from version 3.1.3 here: [Changelog](https://github.com/zo0r/react-native-push-notification/blob/master/CHANGELOG.md)

## Installation

### NPM

`npm install --save react-native-push-notification`

### Yarn

`yarn add react-native-push-notification`

**NOTE: If you target iOS you also need to follow the [installation instructions for PushNotificationIOS](https://github.com/react-native-community/react-native-push-notification-ios) since this package depends on it.**

**NOTE: For Android, you will still have to manually update the AndroidManifest.xml (as below) in order to use Scheduled Notifications.**

## Issues

Having a problem? Read the [troubleshooting](./trouble-shooting.md) guide before raising an issue.

## Pull Requests

[Please read...](./submitting-a-pull-request.md)

## iOS manual Installation

The component uses PushNotificationIOS for the iOS part.

[Please see: PushNotificationIOS](https://github.com/react-native-community/react-native-push-notification-ios)

## Android manual Installation

**NOTE: `firebase-messaging`, prior to version 15 requires to have the same version number in order to work correctly at build time and at run time. To use a specific version:**

In your `android/build.gradle`

```gradle
ext {
    googlePlayServicesVersion = "<Your play services version>" // default: "+"
    firebaseMessagingVersion = "<Your Firebase version>" // default: "+"

    // Other settings
    compileSdkVersion = <Your compile SDK version> // default: 23
    buildToolsVersion = "<Your build tools version>" // default: "23.0.1"
    targetSdkVersion = <Your target SDK version> // default: 23
    supportLibVersion = "<Your support lib version>" // default: 23.1.1
}
```

**NOTE: localNotification() works without changes in the application part, while localNotificationSchedule() only works with these changes:**

In your `android/app/src/main/AndroidManifest.xml`

```xml
    .....
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>

    <application ....>
        <!-- Change the value to true to enable pop-up for in foreground (remote-only, for local use ignoreInForeground) -->
        <meta-data  android:name="com.dieam.reactnativepushnotification.notification_foreground"
                    android:value="false"/>
        <!-- Change the resource name to your App's accent color - or any other color you want -->
        <meta-data  android:name="com.dieam.reactnativepushnotification.notification_color"
                    android:resource="@color/white"/> <!-- or @android:color/{name} to use a standard color -->

        <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationActions" />
        <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationPublisher" />
        <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationBootEventReceiver">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
                <action android:name="com.htc.intent.action.QUICKBOOT_POWERON"/>
            </intent-filter>
        </receiver>

        <service
            android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationListenerService"
            android:exported="false" >
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
     .....
```

If not using a built in Android color (`@android:color/{name}`) for the `notification_color` `meta-data` item.
In `android/app/src/main/res/values/colors.xml` (Create the file if it doesn't exist).

```xml
<resources>
    <color name="white">#FFF</color>
</resources>
```

### If you use remote notifications

Make sure you have installed setup Firebase correctly.

In `android/build.gradle`

```gradle

buildscript {
    ...
    dependencies {
        ...
        classpath('com.google.gms:google-services:4.3.3')
        ...
    }
}
```

In `android/app/build.gradle`

```gradle
dependencies {
  ...
  implementation 'com.google.firebase:firebase-analytics:17.3.0'
  ...
}

apply plugin: 'com.google.gms.google-services'

```

Then put your `google-services.json` in `android/app/`.

**Note: [firebase/release-notes](https://firebase.google.com/support/release-notes/android)**

> The Firebase Android library `firebase-core` is no longer needed. This SDK included the Firebase SDK for Google Analytics.
>
> Now, to use Analytics or any Firebase product that recommends the use of Analytics (see table below), you need to explicitly add the Analytics dependency: `com.google.firebase:firebase-analytics:17.3.0`.

### If you don't use autolink

In `android/settings.gradle`

```gradle
...
include ':react-native-push-notification'
project(':react-native-push-notification').projectDir = file('../node_modules/react-native-push-notification/android')
```

In your `android/app/build.gradle`

```gradle
 dependencies {
    ...
    implementation project(':react-native-push-notification')
    ...
 }
```

Manually register module in `MainApplication.java` (if you did not use `react-native link`):

```java
import com.dieam.reactnativepushnotification.ReactNativePushNotificationPackage;  // <--- Import Package

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
      @Override
      protected boolean getUseDeveloperSupport() {
        return BuildConfig.DEBUG;
      }

      @Override
      protected List<ReactPackage> getPackages() {

      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new ReactNativePushNotificationPackage() // <---- Add the Package
      );
    }
  };

  ....
}
```

## Usage

**DO NOT USE `.configure()` INSIDE A COMPONENT, EVEN `App`**
> If you do, notification handlers will not fire, because they are not loaded. Instead, use `.configure()` in the app's first file, usually `index.js`.


```javascript
import PushNotificationIOS from "@react-native-community/push-notification-ios";
var PushNotification = require("react-native-push-notification");

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

## Example app

Example folder contains an example app to demonstrate how to use this package. The notification Handling is done in `NotifService.js`.

Please test your PRs with this example app before submitting them. It'll help maintaining this repo.

## Handling Notifications

When any notification is opened or received the callback `onNotification` is called passing an object with the notification data.

Notification object example:

```javascript
{
    foreground: false, // BOOLEAN: If the notification was received in foreground or not
    userInteraction: false, // BOOLEAN: If the notification was opened by the user from the notification area or not
    message: 'My Notification Message', // STRING: The notification message
    data: {}, // OBJECT: The push data
}
```

## Local Notifications

`PushNotification.localNotification(details: Object)`

EXAMPLE:

```javascript
PushNotification.localNotification({
  /* Android Only Properties */
  channelId: "your-channel-id", // (required) channelId, if the channel doesn't exist, it will be created with options passed above (importance, vibration, sound). Once the channel is created, the channel will not be update. Make sure your channelId is different if you change these options. If you have created a custom channel, it will apply options of the channel.
  ticker: "My Notification Ticker", // (optional)
  showWhen: true, // (optional) default: true
  autoCancel: true, // (optional) default: true
  largeIcon: "ic_launcher", // (optional) default: "ic_launcher". Use "" for no large icon.
  largeIconUrl: "https://www.example.tld/picture.jpg", // (optional) default: undefined
  smallIcon: "ic_notification", // (optional) default: "ic_notification" with fallback for "ic_launcher". Use "" for default small icon.
  bigText: "My big text that will be shown when notification is expanded", // (optional) default: "message" prop
  subText: "This is a subText", // (optional) default: none
  bigPictureUrl: "https://www.example.tld/picture.jpg", // (optional) default: undefined
  color: "red", // (optional) default: system default
  vibrate: true, // (optional) default: true
  vibration: 300, // vibration length in milliseconds, ignored if vibrate=false, default: 1000
  tag: "some_tag", // (optional) add tag to message
  group: "group", // (optional) add group to message
  groupSummary: false, // (optional) set this notification to be the group summary for a group of notifications, default: false
  ongoing: false, // (optional) set whether this is an "ongoing" notification
  priority: "high", // (optional) set notification priority, default: high
  visibility: "private", // (optional) set notification visibility, default: private
  ignoreInForeground: false, // (optional) if true, the notification will not be visible when the app is in the foreground (useful for parity with how iOS notifications appear)
  shortcutId: "shortcut-id", // (optional) If this notification is duplicative of a Launcher shortcut, sets the id of the shortcut, in case the Launcher wants to hide the shortcut, default undefined
  onlyAlertOnce: false, // (optional) alert will open only once with sound and notify, default: false
  
  when: null, // (optionnal) Add a timestamp pertaining to the notification (usually the time the event occurred). For apps targeting Build.VERSION_CODES.N and above, this time is not shown anymore by default and must be opted into by using `showWhen`, default: null.
  usesChronometer: false, // (optional) Show the `when` field as a stopwatch. Instead of presenting `when` as a timestamp, the notification will show an automatically updating display of the minutes and seconds since when. Useful when showing an elapsed time (like an ongoing phone call), default: false.
  timeoutAfter: null, // (optional) Specifies a duration in milliseconds after which this notification should be canceled, if it is not already canceled, default: null

  messageId: "google:message_id", // (optional) added as `message_id` to intent extras so opening push notification can find data stored by @react-native-firebase/messaging module. 

  actions: ["Yes", "No"], // (Android only) See the doc for notification actions to know more
  invokeApp: true, // (optional) This enable click on actions to bring back the application to foreground or stay in background, default: true

  /* iOS only properties */
  alertAction: "view", // (optional) default: view
  category: "", // (optional) default: empty string

  /* iOS and Android properties */
  id: 0, // (optional) Valid unique 32 bit integer specified as string. default: Autogenerated Unique ID
  title: "My Notification Title", // (optional)
  message: "My Notification Message", // (required)
  userInfo: {}, // (optional) default: {} (using null throws a JSON value '<null>' error)
  playSound: false, // (optional) default: true
  soundName: "default", // (optional) Sound to play when the notification is shown. Value of 'default' plays the default sound. It can be set to a custom sound such as 'android.resource://com.xyz/raw/my_sound'. It will look for the 'my_sound' audio file in 'res/raw' directory and play it. default: 'default' (default sound is played)
  number: 10, // (optional) Valid 32 bit integer specified as string. default: none (Cannot be zero)
  repeatType: "day", // (optional) Repeating interval. Check 'Repeating Notifications' section for more info.
});
```

## Scheduled Notifications

`PushNotification.localNotificationSchedule(details: Object)`

EXAMPLE:

```javascript
PushNotification.localNotificationSchedule({
  //... You can use all the options from localNotifications
  message: "My Notification Message", // (required)
  date: new Date(Date.now() + 60 * 1000), // in 60 secs
  allowWhileIdle: false, // (optional) set notification to work while on doze, default: false
});
```

## Get the initial notification

`PushNotification.popInitialNotification(callback)`

EXAMPLE:

```javascript
PushNotification.popInitialNotification((notification) => {
  console.log('Initial Notification', notification);
});
```

## Custom sounds

In android, add your custom sound file to `[project_root]/android/app/src/main/res/raw`

In iOS, add your custom sound file to the project `Resources` in xCode.

In the location notification json specify the full file name:

    soundName: 'my_sound.mp3'

## Channel Management (Android)

To use channels, create them at startup and pass the matching `channelId` through to `PushNotification.localNotification` or `PushNotification.localNotificationSchedule`.

```javascript
  PushNotification.createChannel(
    {
      channelId: "channel-id", // (required)
      channelName: "My channel", // (required)
      channelDescription: "A channel to categorise your notifications", // (optional) default: undefined.
      soundName: "default", // (optional) See `soundName` parameter of `localNotification` function
      importance: 4, // (optional) default: 4. Int value of the Android notification importance
      vibrate: true, // (optional) default: true. Creates the default vibration patten if true.
    },
    (created) => console.log(`createChannel returned '${created}'`) // (optional) callback returns whether the channel was created, false means it already existed.
  );
```

**NOTE: Without channel, notifications don't work**

In the notifications options, you must provide a channel id with `channelId: "your-channel-id"`, if the channel doesn't exist the notification might not e triggered. Once the channel is created, the channel cannot be update. Make sure your `channelId` is different if you change these options. If you have created a channel in another way, it will apply options of the channel.

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

## Cancelling notifications

### 1) cancelLocalNotifications

The `id` parameter for `PushNotification.localNotification` is required for this operation. The id supplied will then be used for the cancel operation.

```javascript
PushNotification.localNotification({
    ...
    id: '123'
    ...
});
PushNotification.cancelLocalNotifications({id: '123'});
```

**iOS: `userInfo` is populated `id` if not defined this allow the previous method**

### 2) cancelAllLocalNotifications

`PushNotification.cancelAllLocalNotifications()`

Cancels all scheduled notifications AND clears the notifications alerts that are in the notification centre.

_NOTE: there is currently no api for removing specific notification alerts from the notification centre._

### 3) removeAllDeliveredNotifications

```javascript
PushNotificationIOS.removeAllDeliveredNotifications();
```

Remove all delivered notifications from Notification Center

### 4) getDeliveredNotifications

```javascript
PushNotificationIOS.getDeliveredNotifications(callback);
```

Provides you with a list of the app’s notifications that are still displayed in Notification Center

**Parameters:**

| Name     | Type     | Required | Description                                                 |
| -------- | -------- | -------- | ----------------------------------------------------------- |
| callback | function | Yes      | Function which receive an array of delivered notifications. |

A delivered notification is an object containing:

- `identifier` : The identifier of this notification.
- `title` : The title of this notification.
- `body` : The body of this notification.
- `category` : The category of this notification (optional).
- `userInfo` : An object containing additional notification data (optional).
- `thread-id` : The thread identifier of this notification, if has one.

### 5) removeDeliveredNotifications

```javascript
PushNotificationIOS.removeDeliveredNotifications(identifiers);
```

Removes the specified notifications from Notification Center

**Parameters:**

| Name        | Type  | Required | Description                        |
| ----------- | ----- | -------- | ---------------------------------- |
| identifiers | array | Yes      | Array of notification identifiers. |

### 6) getScheduledLocalNotifications

```javascript
PushNotificationIOS.getScheduledLocalNotifications(callback);
```

Provides you with a list of the app’s scheduled local notifications that are yet to be displayed

**Parameters:**

| Name     | Type     | Required | Description                                                 |
| -------- | -------- | -------- | ----------------------------------------------------------- |
| callback | function | Yes      | Function which receive an array of delivered notifications. |

Returns an array of local scheduled notification objects containing:

| Name           | Type   | Description                               |
| -------------- | ------ | ----------------------------------------- |
| id             | number | The identifier of this notification.      |
| date           | Date   | The fire date of this notification.       |
| title          | string | The title of this notification.           |
| message        | string | The message body of this notification.    |
| soundName      | string | The sound name of this notification.      |
| repeatInterval | number | The repeat interval of this notification. |
| number         | number | App notification badge count number.      |

## Abandon Permissions

`PushNotification.abandonPermissions()` Revokes the current token and unregister for all remote notifications received via APNS or FCM.

## Notification priority

(optional) Specify `priority` to set priority of notification. Default value: "high"

Available options:

"max" = NotficationCompat.PRIORITY_MAX
"high" = NotficationCompat.PRIORITY_HIGH
"low" = NotficationCompat.PRIORITY_LOW
"min" = NotficationCompat.PRIORITY_MIN
"default" = NotficationCompat.PRIORITY_DEFAULT

More information: https://developer.android.com/reference/android/app/Notification.html#PRIORITY_DEFAULT

## Notification visibility

(optional) Specify `visibility` to set visibility of notification. Default value: "private"

Available options:

"private" = NotficationCompat.VISIBILITY_PRIVATE
"public" = NotficationCompat.VISIBILITY_PUBLIC
"secret" = NotficationCompat.VISIBILITY_SECRET

More information: https://developer.android.com/reference/android/app/Notification.html#VISIBILITY_PRIVATE

## Notification importance

(optional) Specify `importance` to set importance of notification. Default value: "high"

Available options:

"default" = NotificationManager.IMPORTANCE_DEFAULT
"max" = NotificationManager.IMPORTANCE_MAX
"high" = NotificationManager.IMPORTANCE_HIGH
"low" = NotificationManager.IMPORTANCE_LOW
"min" = NotificationManager.IMPORTANCE_MIN
"none" = NotificationManager.IMPORTANCE_NONE
"unspecified" = NotificationManager.IMPORTANCE_UNSPECIFIED

More information: https://developer.android.com/reference/android/app/NotificationManager#IMPORTANCE_DEFAULT

## Notification while idle

(optional) Specify `allowWhileIdle` to set if the notification should be allowed to execute even when the system is on low-power idle modes.

On Android 6.0 (API level 23) and forward, the Doze was introduced to reduce battery consumption when the device is unused for long periods of time. But while on Doze the AlarmManager alarms (used to show scheduled notifications) are deferred to the next maintenance window. This may cause the notification to be delayed while on Doze.

This can significantly impact the power use of the device when idle. So it must only be used when the notification is required to go off on a exact time, for example on a calendar notification.

More information:
https://developer.android.com/training/monitoring-device-state/doze-standby

## Repeating Notifications

(optional) Specify `repeatType` and optionally `repeatTime` (Android-only) while scheduling the local notification. Check the local notification example above.

Property `repeatType` could be one of `month`, `week`, `day`, `hour`, `minute`, `time`. If specified as time, it should be accompanied by one more parameter `repeatTime` which should the number of milliseconds between each interval.

## Notification Actions

(Android Only)

This is done by specifying an `actions` parameters while configuring the local notification. This is an array of strings where each string is a notification action that will be presented with the notification.

For e.g. `actions: ['Accept', 'Reject']`

When you handle actions in background (`invokeApp: false`), you can open the application and pass the initial notification by using use `PushNotification.invokeApp(notification)`.

Make sure you have the receiver in `AndroidManifest.xml`:

```xml
  <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationActions" />
```

For iOS, you can use this [package](https://github.com/holmesal/react-native-ios-notification-actions) to add notification actions.

## Set application badge icon

`PushNotification.setApplicationIconBadgeNumber(number: number)`

Works natively in iOS.

Uses the [ShortcutBadger](https://github.com/leolin310148/ShortcutBadger) on Android, and as such will not work on all Android devices.

## Android Only Methods

`PushNotification.subscribeToTopic(topic: string)` Subscribe to a topic (works only with Firebase)

`PushNotification.unsubscribeFromTopic(topic: string)` Unsubscribe from a topic (works only with Firebase)

## Checking Notification Permissions

`PushNotification.checkPermissions(callback: Function)` Check permissions

`callback` will be invoked with a `permissions` object:

- `alert`: boolean
- `badge`: boolean
- `sound`: boolean

## iOS Only Methods

`PushNotification.getApplicationIconBadgeNumber(callback: Function)` Get badge number
