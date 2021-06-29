# Installation

Here you find every step needed to install correctly `react-native-push-notification`

## Install with NPM or YARN

`npm install --save react-native-push-notification` or `yarn add react-native-push-notification`

## Attention!

**NOTE: If you target iOS you also need to follow the [installation instructions for PushNotificationIOS](https://github.com/react-native-community/react-native-push-notification-ios) since this package depends on it.**

**NOTE: For Android, you will still have to manually update the AndroidManifest.xml (as below) in order to use Scheduled Notifications.**

## iOS manual Installation

The component uses PushNotificationIOS for the iOS part. You should follow their [installation instructions](https://github.com/react-native-community/react-native-push-notification-ios).

## Android manual Installation

**NOTE: `firebase-messaging`, prior to version 15 requires to have the same version number in order to work correctly at build time and at run time. To use a specific version:**

In your `android/build.gradle`

```gradle
ext {
    googlePlayServicesVersion = "<Your play services version>" // default: "+"
    firebaseMessagingVersion = "<Your Firebase version>" // default: "21.1.0"

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
        <!-- Change the value to true to enable pop-up for in foreground on receiving remote notifications (for prevent duplicating while showing local notifications set this to false) -->
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

If your app has an @Override on onNewIntent in `MainActivity.java` ensure that function includes a super call on onNewIntent (if your `MainActivity.java` does not have an @Override for onNewIntent skip this):

```java
    @Override
    public void onNewIntent(Intent intent) {
        ...
        super.onNewIntent(intent);
        ...
    }
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