# React Native Push Notifications

[![npm version](https://badge.fury.io/js/react-native-push-notification.svg?update=9)](http://badge.fury.io/js/react-native-push-notification)
[![npm downloads](https://img.shields.io/npm/dm/react-native-push-notification.svg?update=9)](http://badge.fury.io/js/react-native-push-notification)

React Native Local and Remote Notifications for iOS and Android

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

`npm install --save react-native-push-notification` or `yarn add react-native-push-notification`

`react-native link`

**NOTE: For Android, you will still have to manually update the AndroidManifest.xml (as below) in order to use Scheduled Notifications.**

## Issues

Having a problem? Read the [troubleshooting](./trouble-shooting.md) guide before raising an issue.

## Pull Requests

[Please read...](./submitting-a-pull-request.md)

## iOS manual Installation

The component uses PushNotificationIOS for the iOS part.

[Please see: PushNotificationIOS](https://github.com/react-native-community/react-native-push-notification-ios)

## Android manual Installation

**NOTE: `play-service-gcm` and `firebase-messaging`, prior to version 15 requires to have the same version number in order to work correctly at build time and at run time. To use a specific version:**

In your `android/build.gradle`

```gradle
...

dependencies {
    ...

    compile project(':react-native-push-notification')
    compile ('com.google.android.gms:play-services-gcm:8.1.0') {
        force = true;
    }
}
```

In your `AndroidManifest.xml`

```xml
    .....
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <permission
        android:name="${applicationId}.permission.C2D_MESSAGE"
        android:protectionLevel="signature" />
    <uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>

    <application ....>
        <receiver
            android:name="com.google.android.gms.gcm.GcmReceiver"
            android:exported="true"
            android:permission="com.google.android.c2dm.permission.SEND" >
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                <category android:name="${applicationId}" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationPublisher" />
        <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationBootEventReceiver">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>
        <service android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationRegistrationService"/>
        <service
            android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationListenerService"
            android:exported="false" >
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
            </intent-filter>
        </service>
     .....
```

In `android/settings.gradle`

```gradle
...

include ':react-native-push-notification'
project(':react-native-push-notification').projectDir = file('../node_modules/react-native-push-notification/android')
```

In `android/app/src/res/values/colors.xml` (Create the file if it doesn't exist).

```xml
<resources>
    <color name="white">#FFF</color>
</resources>
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

```javascript
var PushNotification = require("react-native-push-notification");

PushNotification.configure({
  // (optional) Called when Token is generated (iOS and Android)
  onRegister: function(token) {
    console.log("TOKEN:", token);
  },

  // (required) Called when a remote or local notification is opened or received
  onNotification: function(notification) {
    console.log("NOTIFICATION:", notification);

    // process the notification

    // required on iOS only (see fetchCompletionHandler docs: https://github.com/react-native-community/react-native-push-notification-ios)
    notification.finish(PushNotificationIOS.FetchResult.NoData);
  },

  // ANDROID ONLY: GCM or FCM Sender ID (product_number) (optional - not required for local notifications, but is need to receive remote push notifications)
  senderID: "YOUR GCM (OR FCM) SENDER ID",

  // IOS ONLY (optional): default: all - Permissions to register.
  permissions: {
    alert: true,
    badge: true,
    sound: true
  },

  // Should the initial notification be popped automatically
  // default: true
  popInitialNotification: true,

  /**
   * (optional) default: true
   * - Specified if permissions (ios) and token (android and ios) will requested or not,
   * - if not, you must call PushNotificationsHandler.requestPermissions() later
   */
  requestPermissions: true
});
```

## Example app

Example folder contains an example app to demonstrate how to use this package. The notification Handling is done in `NotifService.js`. For Remote notifications, configure your SenderId in `app.json`. You can also edit it directly in the app.
To send Push notifications, you can use the online tool [PushWatch](https://www.pushwatch.com/gcm/).

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
    id: '0', // (optional) Valid unique 32 bit integer specified as string. default: Autogenerated Unique ID
    ticker: "My Notification Ticker", // (optional)
    autoCancel: true, // (optional) default: true
    largeIcon: "ic_launcher", // (optional) default: "ic_launcher"
    smallIcon: "ic_notification", // (optional) default: "ic_notification" with fallback for "ic_launcher"
    bigText: "My big text that will be shown when notification is expanded", // (optional) default: "message" prop
    subText: "This is a subText", // (optional) default: none
    color: "red", // (optional) default: system default
    vibrate: true, // (optional) default: true
    vibration: 300, // vibration length in milliseconds, ignored if vibrate=false, default: 1000
    tag: 'some_tag', // (optional) add tag to message
    group: "group", // (optional) add group to message
    ongoing: false, // (optional) set whether this is an "ongoing" notification
    actions: [{id: “actionId”, text: “Display Text”}],  // (optional) See the doc for notification actions to know more

    /* iOS only properties */
    alertAction: // (optional) default: view
    category: // (optional) default: null
    userInfo: // (optional) default: null (object containing additional notification data)

    /* iOS and Android properties */
    title: "My Notification Title", // (optional)
    message: "My Notification Message", // (required)
    playSound: false, // (optional) default: true
    soundName: 'default', // (optional) Sound to play when the notification is shown. Value of 'default' plays the default sound. It can be set to a custom sound such as 'android.resource://com.xyz/raw/my_sound'. It will look for the 'my_sound' audio file in 'res/raw' directory and play it. default: 'default' (default sound is played)
    number: '10', // (optional) Valid 32 bit integer specified as string. default: none (Cannot be zero)
    repeatType: 'day', // (Android only) Repeating interval. Could be one of `week`, `day`, `hour`, `minute, `time`. If specified as time, it should be accompanied by one more parameter 'repeatTime` which should the number of milliseconds between each interval
});
```

## Scheduled Notifications

`PushNotification.localNotificationSchedule(details: Object)`

EXAMPLE:

```javascript
PushNotification.localNotificationSchedule({
  message: "My Notification Message", // (required)
  date: new Date(Date.now() + 60 * 1000) // in 60 secs
});
```

## Custom sounds

In android, add your custom sound file to `[project_root]/android/app/src/main/res/raw`

In iOS, add your custom sound file to the project `Resources` in xCode.

In the location notification json specify the full file name:

    soundName: 'my_sound.mp3'

## Cancelling notifications

### 1) cancelLocalNotifications

#### Android

The `id` parameter for `PushNotification.localNotification` is required for this operation. The id supplied will then be used for the cancel operation.

```javascript
// Android
PushNotification.localNotification({
    ...
    id: '123'
    ...
});
PushNotification.cancelLocalNotifications({id: '123'});
```

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

#### IOS

The `userInfo` parameter for `PushNotification.localNotification` is required for this operation and must contain an `id` parameter. The id supplied will then be used for the cancel operation.

```javascript
PushNotification.cancelLocalNotifications({id: '123'});
```

### 2) cancelAllLocalNotifications

`PushNotification.cancelAllLocalNotifications()`

Cancels all scheduled notifications AND clears the notifications alerts that are in the notification centre.

_NOTE: there is currently no api for removing specific notification alerts from the notification centre._

## Repeating Notifications

(Android only) Specify `repeatType` and optionally `repeatTime` while scheduling the local notification. Check the local notification example above.

Property `repeatType` could be one of `month`, `week`, `day`, `hour`, `minute`, `time`. If specified as time, it should be accompanied by one more parameter `repeatTime` which should the number of milliseconds between each interval.

## Notification Actions

Couple things are required to setup notification actions.

This implementation ensures that our notification actions will always be executed even when application is not running. In such case, android will start our app in background and then execute headless task. Please also read through [react-native documentation](https://facebook.github.io/react-native/docs/headless-js-android.html) regarding `HeadlessJsTask`.

### 1) Specify notification actions for a notification

This is done by specifying an `actions` parameters while configuring the local notification. This is an array of strings where each string is a notification action that will be presented with the notification.

For e.g. `actions: '["Accept", "Reject"]' // Must be in string format`

The array itself is specified in string format to circumvent some problems because of the way JSON arrays are handled by react-native android bridge.

### 2) Specify handlers for the notification actions

For each action specified in the `actions` field, we need to add a handler that is called when the user clicks on the action. This can be done in the `componentWillMount` of your main app file or in a separate file which is imported in your main app file. Notification actions handlers can be configured as below:

```
import { AppRegistry } from 'react-native'

const notificationActionHandler = async (data) => {
  const action = data.notification.action;
  if (action == ‘firstAction’) { // id of the first action
    // Do work pertaining to Accept action here
  } else if (action == ‘secondAction’) { // id of the second action
    // Do work pretainng to Reject action here
  }
  // Add all the requuuirede actions handlers
}

AppRegistry.registerHeadlessTask(
    ‘RNPushNotificationActionHandlerTask’, // you must use the same name
    () => { return notificationActionHandler }
);
```
### 3) Modify AndroidManifest.xml
Add `service` and `receiver` with `intent-filter` to your `AndroidMainfest.xml` file.

```
<service android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationActionService" />
<receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationActionHandlerReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="${applicationId}.firstAction" />
        <action android:name="${applicationId}.secondAction" />
    </intent-filter>
</receiver>
```

For iOS, you can use this [package](https://github.com/holmesal/react-native-ios-notification-actions) to add notification actions.

## Set application badge icon

`PushNotification.setApplicationIconBadgeNumber(number: number)`

Works natively in iOS.

Uses the [ShortcutBadger](https://github.com/leolin310148/ShortcutBadger) on Android, and as such will not work on all Android devices.

## Sending Notification Data From Server

Same parameters as `PushNotification.localNotification()`

## Android Only Methods

`PushNotification.subscribeToTopic(topic: string)` Subscribe to a topic (works only with Firebase)

## Checking Notification Permissions

`PushNotification.checkPermissions(callback: Function)` Check permissions

`callback` will be invoked with a `permissions` object:

- `alert`: boolean
- `badge`: boolean
- `sound`: boolean

## iOS Only Methods
`PushNotification.checkPermissions(callback: Function)` Check permissions

`PushNotification.getApplicationIconBadgeNumber(callback: Function)` get badge number

`PushNotification.abandonPermissions()` Abandon permissions
