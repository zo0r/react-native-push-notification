# React Native Push Notifications
[![npm version](https://badge.fury.io/js/react-native-push-notification.svg)](http://badge.fury.io/js/react-native-push-notification)
[![npm downloads](https://img.shields.io/npm/dm/react-native-push-notification.svg?maxAge=2592000)](https://img.shields.io/npm/dm/react-native-push-notification.svg?maxAge=2592000)

React Native Local and Remote Notifications for iOS and Android

## Installation
`npm install react-native-push-notification`

## iOS Installation
The component uses PushNotificationIOS for the iOS part.

[Please see: PushNotificationIOS](https://facebook.github.io/react-native/docs/pushnotificationios.html#content)

## Android Installation

**NOTE: To use a specific `play-service-gcm` version, use in your `android/app/build.gradle` (change `8.1.0` for your version):**
```gradle
...

dependencies {
    ...

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

In `android/app/build.gradle`

```gradle
...

dependencies {
    ...

    compile project(':react-native-push-notification')
}
```

Register module (in `MainApplication.java`)

```java
import android.content.Intent; // <--- Import Intent
import com.dieam.reactnativepushnotification.ReactNativePushNotificationPackage;  // <--- Import Package

public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {

  private ReactNativePushNotificationPackage mReactNativePushNotificationPackage; // <------ Add Package Variable

   ...

   /**
   * A list of packages used by the app. If the app uses additional views
   * or modules besides the default ones, add more packages here.
   */
    @Override
    protected List<ReactPackage> getPackages() {
      mReactNativePushNotificationPackage = new ReactNativePushNotificationPackage(this); // <------ Initialize the Package
      return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        new VectorIconsPackage(),
        new FabricPackage(),
        mReactNativePushNotificationPackage // <---- Add the Package
      );
    }

    // Add onNewIntent
    @Override
    // in RN <= 0.27 you may need to use `protected void onNewIntent (Intent intent) {`
    public void onNewIntent (Intent intent) {
      super.onNewIntent(intent);

      mReactNativePushNotificationPackage.newIntent(intent);
    }

    ....
}
```

## Usage
```javascript
var PushNotification = require('react-native-push-notification');

PushNotification.configure({

    // (optional) Called when Token is generated (iOS and Android)
    onRegister: function(token) {
        console.log( 'TOKEN:', token );
    },

    // (required) Called when a remote or local notification is opened or received
    onNotification: function(notification) {
        console.log( 'NOTIFICATION:', notification );
    },

    // ANDROID ONLY: (optional) GCM Sender ID.
    senderID: "YOUR GCM SENDER ID",

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
      * IOS ONLY: (optional) default: true
      * - Specified if permissions will requested or not,
      * - if not, you must call PushNotificationsHandler.requestPermissions() later
      */
    requestPermissions: true,
});
```

## Handling Notifications
When any notification is opened or received the callback `onNotification` is called passing an object with the notification data.

Notification object example:
```javascript
{
    foreground: false, // BOOLEAN: If the notification was received in foreground or not
    message: 'My Notification Message', // STRING: The notification message
    data: {}, // OBJECT: The push data
}
```

## Local and Schedule Notifications
`PushNotification.localNotification(details: Object)`

`PushNotification.localNotificationSchedule(details: Object)`

EXAMPLE:
```javascript
PushNotification.localNotification({
    /* Android Only Properties */
    id: 0, // (optional) default: Autogenerated Unique ID
    title: "My Notification Title", // (optional)
    ticker: "My Notification Ticker", // (optional)
    autoCancel: true, (optional) default: true
    largeIcon: "ic_launcher", // (optional) default: "ic_launcher"
    smallIcon: "ic_notification", // (optional) default: "ic_notification" with fallback for "ic_launcher"
    bigText: "My big text that will be shown when notification is expanded", // (optional) default: "message" prop
    subText: "This is a subText", // (optional) default: none
    number: 10, // (optional) default: none (Cannot be zero)
    color: "red", // (optional) default: system default

    /* iOS and Android properties */
    message: "My Notification Message" // (required)
});

PushNotification.localNotificationSchedule({
	message: "My Notification Message", // (required)
	date: new Date(Date.now() + (60 * 1000)) // in 60 secs
});
```

## Sending Notification Data From Server
Same parameters as `PushNotification.localNotification()`

## iOS Only Methods
`PushNotification.checkPermissions(callback: Function)` Check permissions

`PushNotification.setApplicationIconBadgeNumber(number: number)` set badge number

`PushNotification.getApplicationIconBadgeNumber(callback: Function)` get badge number

`PushNotification.abandonPermissions()` Abandon permissions

### TODO
- [X] Add `PushNotification.localNotificationSchedule()` Android support
- [ ] Restore Android local notifications after reboot

