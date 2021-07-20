# Changelog

All notable changes to this project will be documented in this file.

This project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

### Breaking changes

### Features

### Fixed

## [7.4.0] 2021-06-24

### Features

 - (Android): Allow for repeat to specify amount of the given repeat type. [#2063](https://github.com/zo0r/react-native-push-notification/pull/2030)
 - (iOS): Add support for subtitle notification property. [#2063](https://github.com/zo0r/react-native-push-notification/pull/2063)

## [7.3.2] 2021-06-19

### Fixed

- (Android) Fix: Foreground notifications missing small icon. [#1927](https://github.com/zo0r/react-native-push-notification/pull/1927)

## [7.3.1] 2021-05-12

### Fixed

- (Android) Pin the firebase-messaging dependency to `21.1.0`.
- (Android) Fix: android missing channelId warning should now show [#1995](https://github.com/zo0r/react-native-push-notification/pull/1995).

## [7.3.0] 2021-05-12

### Features
- (Android) Add constants for notification importance [#1959](https://github.com/zo0r/react-native-push-notification/pull/1959)

### Fixed

- (Android) Fix: Task :react-native-push-notification:compileDebugJavaWithJavac FAILED [#1979](https://github.com/zo0r/react-native-push-notification/issues/1979)

## [7.2.3] 2021-03-18

### Fixed

- (Android) Fix: Notification drawer doesn't close after click on action that navigates you to app [#1914](https://github.com/zo0r/react-native-push-notification/issues/1914)
- (iOS) Fix: foreground notification property [#1916](https://github.com/zo0r/react-native-push-notification/pull/1916)

## [7.2.2] 2021-03-04

### Fixed

- (Android) Fix: Could not invoke RNPushNotification.getDeliveredNotifications. [#1878](https://github.com/zo0r/react-native-push-notification/issues/1878)
- (fix) deep clone details and notifications. [#1793](https://github.com/zo0r/react-native-push-notification/issues/1793)

## [7.2.1] 2021-02-11

### Fixed

- (iOS) Fix `playSound` options on local notifications. [#1858](https://github.com/zo0r/react-native-push-notification/issues/1858#issuecomment-775714298)

## [7.2.0] 2021-01-24

### Features

- (Android) Handle localization for notification title and body [#1837](https://github.com/zo0r/react-native-push-notification/pull/1837)

## [7.1.1] 2021-01-20

### Fixed

- (Android) unsubscribeFromTopic function fix [#1831](https://github.com/zo0r/react-native-push-notification/pull/1831)

## [7.1.0] 2021-01-16

### Features

- (Android) Add hooks to intent handling and bundle parsing [#1819](https://github.com/zo0r/react-native-push-notification/pull/1819)
 
## [7.0.0] 2020-12-23

### Breaking changes

- (iOS) Replace deprecated local notification methods on iOS [#1751](https://github.com/zo0r/react-native-push-notification/pull/1751)
- (Android) Rename the Android package from `RNPushNotification` to `ReactNativePushNotification` resolve [#893](https://github.com/zo0r/react-native-push-notification/issues/893)
- (Android) Allow `userInfo` to be stored in scheduled notification as in iOS (mapped as `data` on press or list scheduled notifications).

### Features

- (Android) silent channel using playSound flag
- (Android) implement 'bigLargeIcon' for Android notifications (must be combined with BigPicture) [#1730](https://github.com/zo0r/react-native-push-notification/pull/1730)
- (Android) notification with inline reply [#612](https://github.com/zo0r/react-native-push-notification/pull/612)
- (Android) Support using drawable as Android small icon [#1787](https://github.com/zo0r/react-native-push-notification/pull/1787)

## [6.1.3] 2020-11-09

### Fixed

- (Android) Null pointer exception when trying to create channel [#1734](https://github.com/zo0r/react-native-push-notification/issues/1734)

## [6.1.2] 2020-10-29

### Fixed

- (Android) Fix for vibration on notifs for Android API >= 26 [#1686](https://github.com/zo0r/react-native-push-notification/pull/1686)

## [6.1.1] 2020-09-29

### Fixed

- (Android) Fix a crash when the application is in background [#1676](https://github.com/zo0r/react-native-push-notification/issues/1676)

## [6.1.0] 2020-09-28

### Features

- (Android) Allow a default channel in the `AndroidManifest`:
  ```xml
        <meta-data android:name="com.dieam.reactnativepushnotification.default_notification_channel_id" android:value="..."/>
  ```
  If not defined, fallback to the Firebase value of:
  ```xml
        <meta-data android:name="com.google.firebase.messaging.default_notification_channel_id" android:value="..."/>
  ```
  If not defined, fallback to the default Firebase channel id `fcm_fallback_notification_channel`

## [6.0.0] 2020-09-26

### Breaking changes

- (Android) Channel Management: In order to limit the scope of responsability of this library, developers are now responsible of the creation of the channels. You can find the documentation at https://github.com/zo0r/react-native-push-notification#channel-management-android. These changes are also made to allow improvements in the future of the library. Here the list of impacts:
  - You must create your channels before triggering a notification.
  - These entries in `AndroidManifest` are deprecated:
  ```xml
        <meta-data android:name="com.dieam.reactnativepushnotification.notification_channel_name" android:value="..."/>
        <meta-data android:name="com.dieam.reactnativepushnotification.notification_channel_description" android:value="..."/>
        <meta-data android:name="com.dieam.reactnativepushnotification.channel_create_default" android:value="..."/>
  ```
  -  Followings options changed on Android in `localNotification` and `localNotificationSchedule`:
     - `channelId` becomes mandatory (warning if not provided)
     - `channelName` is deprecated
     - `channelDescription` is deprecated
     - `importance` is deprecated
  - These changes help to avoid an issue [#1649](https://github.com/zo0r/react-native-push-notification/issues/1649)
- (Android) Remove check for the intent `BOOT_COMPLETED`, this should allow more intent action such as `QUICKBOOT_POWERON`. It's recommended to update `AndroidManifest`, the `RNPushNotificationBootEventReceiver` to:
  ```xml
        <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationBootEventReceiver">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
                <action android:name="com.htc.intent.action.QUICKBOOT_POWERON"/>
            </intent-filter>
        </receiver>
  ```
- `@react-native-community/push-notification-ios` is now a `peerDependency`, please make sure that you installed this library with NPM or YARN.
- (Android) Fix a bug where notification data are not inside `data` property after been pressed by user. When sending notification + data and app in background.
- (Android) Add more fields from the firebase notification part. (Thanks to @fattomhk with this PR [#1626](https://github.com/zo0r/react-native-push-notification/pull/1626))
  - `notificationPriority`
  - `image`
  - `tag`
  - `visibility`
- (Android) `data.twi_body` is no more used to trigger a notification in notification-center. Revert of [#744](https://github.com/zo0r/react-native-push-notification/pull/744)

### Fixed

- (iOS) upgrade `@react-native-community/push-notification-ios`, fixe the value of `userInteraction` [@react-native-community/push-notification-ios#122](https://github.com/react-native-community/push-notification-ios/pull/122).

## [5.1.1] 2020-09-15

### Fixed

- (Android) Fatal Exception: java.lang.NullPointerException [#1641](https://github.com/zo0r/react-native-push-notification/issues/1641)

## [5.1.0] 2020-08-31

### Features

- (Android) Add support for specifying a delegate FirebaseMessagingService [#1589](https://github.com/zo0r/react-native-push-notification/pull/1589)
- (Android) Add support of `when`, `usesChronometer` and `timeoutAfter`.

### Fixed

- (Android) Fix a bug where `userInteraction` is not set, notification when app in background pressed by user.


## [5.0.1] 2020-08-04

### Fixed

- (Android) Fix change that make gradle build fail [#1578](https://github.com/zo0r/react-native-push-notification/pull/1578).

## [5.0.0] 2020-08-03

### Breaking changes

- (Android/iOS) Unify returned values between iOS and Android [#1516](https://github.com/zo0r/react-native-push-notification/pull/1516).
- (Android/iOS) `.popInitialNotification(callback)` now return the same format as `onNotification()`.
- (Android) `popInitialNotification` in `configure()` now trigger only once on app startup, same as iOS.
- (Android) `notification.foreground` now return the good value, before the value was `false` most of the time.

### Features

- (Android) Add function `createChannel` for custom Android channel support [#1509](https://github.com/zo0r/react-native-push-notification/pull/1509)
- (Android) Add Android `messageId` to enable integration with `react-native-firebase/messaging` [#1510](https://github.com/zo0r/react-native-push-notification/pull/1510)
- (Android) Add support for `onlyAlertOnce` property [#1519](https://github.com/zo0r/react-native-push-notification/pull/1519)
- (Android) Allow to change default notification channel name after it's creation [#1549](https://github.com/zo0r/react-native-push-notification/pull/1549)

### Fixed

- (Android) `popInitialNotification` in `configure()` now trigger only once and do not trigger twice `onNotification()` when user press the notification, more details: [#1516](https://github.com/zo0r/react-native-push-notification/pull/1516).
- (Android) `notification.foreground` now return the good value, before the value was `false` most of the time.

## [4.0.0] 2020-07-06

### Breaking changes

- `RNPushNotificationRegistrationService` has been removed, old reference in AndroidManifest must be removed.
- `Notifications.registerNotificationActions()` has been removed and is not required for `actions`.
- `DeviceEventEmitter.addListener('notificationActionReceived', callback)` is replaced by `onAction`.
- Extra receiver must be added to manage actions.
  ```xml
      <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationActions" />
  ```
- (iOS) `userInfo` is now populated with id by default to allow operation based on `id`.

### Features

- (Android) `actions` accept an array of strings.
- (Android) `invokeApp` allow you to handle actions in background without invoking the application.
- (Android) `onAction` has been added to `.configure()` to handle action in background.
- (Android) `PushNotification.invokeApp(notification)` allow you to invoke the application when in background (notification for initial notification).
- (Android) `PushNotification.getChannels(callback)` allow you to get the list of channels.
- (Android) `PushNotification.channelExists(channel_id, callback)` allow you to check of a channel exists.
- (Android) `PushNotification.channelBlocked(channel_id, callback)` allow you to check of a channel is blocked. Based on [#1249](https://github.com/zo0r/react-native-push-notification/pull/1249)
- (Android) `PushNotification.deleteChannel(channel_id)` allow you to delete a channel.
- (Android) Add `largeIconUrl` to load a largeIcon based on Url. Based on [#1444](https://github.com/zo0r/react-native-push-notification/pull/1444)
- (Android) Add `bigPictureUrl` to load a picture based on Url. Based on [#1444](https://github.com/zo0r/react-native-push-notification/pull/1444)
- (Android) Add `shortcutId` for better badges management.
- (Android) Add `showWhen` to display "when" it was published, default: true.
- (Android) Add `groupSummary` to allow grouping notifications. Based on [#1253](https://github.com/zo0r/react-native-push-notification/pull/1253)
- (Android) Add `channelId`, custom channel_id in android. Based on [#1159](https://github.com/zo0r/react-native-push-notification/pull/1159)
- (Android) Add `channelName`, custom channel_name in android.
- (Android) Add `channelDescription`, custom channel_description in android.
- (iOS) Add fire date in notification response, NOTE: `push-notification-ios` in version `> 1.2.0` [#1345](https://github.com/zo0r/react-native-push-notification/pull/1345)
- (iOS) `onRegistrationError` has been added to `.configure()` to handle `registrationError` events.
- (Android/iOS) Add method getScheduledLocalNotifications()[#1466](https://github.com/zo0r/react-native-push-notification/pull/1466)

### Fixed

- (Android) Replace java.util.Random with java.security.SecureRandom [#1497](https://github.com/zo0r/react-native-push-notification/pull/1497)
- (Android) WAKE_LOCK permission removed from documentation. [#1494](https://github.com/zo0r/react-native-push-notification/issues/1494)
- (Android) Some options were ignored on scheduled/repeating notifications (allowWhileIdle, ignoreInForeground).
- (Android/iOS) popInitialInotification might be ignored in `.configure()`

## [3.5.2] - 2020-05-25

### Fixed

- (Android) Sounds are playing even in Do Not Disturb [#1432](https://github.com/zo0r/react-native-push-notification/issues/1432#issuecomment-633367111)
- (Android) onNotification fires every time when the app goes from background to foreground [#1455](https://github.com/zo0r/react-native-push-notification/issues/1455)
- (Android) java.lang.NullPointerException: Attempt to invoke virtual method 'void com.dieam.reactnativepushnotification.modules.d.c(android.os.Bundle)' on a null object reference [#1431](https://github.com/zo0r/react-native-push-notification/issues/1431#issuecomment-633315150)

## [3.5.1] - 2020-05-20

### Fixed

- (Android) When updating 3.4 to 3.5, unable to compile Android [#1449](https://github.com/zo0r/react-native-push-notification/pull/1449)

## [3.5.0] - 2020-05-20

### Features

- (Android) Enables the ability to support multiple push providers [#1445](https://github.com/zo0r/react-native-push-notification/pull/1445)

### Fixed

- (Android) No sound on notifications [#1432](https://github.com/zo0r/react-native-push-notification/issues/1432)
- (Android) onNotification is not calling when app is in background [#1446](https://github.com/zo0r/react-native-push-notification/pull/1446)
- (Android) `number` and `id` do not crash if NaN is passed in Android.

## [3.4.0] - 2020-05-08

### Features

- (Android) Call `onRegister` when [Firebase renew token](<https://firebase.google.com/docs/reference/android/com/google/firebase/messaging/FirebaseMessagingService#onNewToken(java.lang.String)>).
- (Android) Added Abandon Permissions method to Android [#1425](https://github.com/zo0r/react-native-push-notification/pull/1425)
- (Android) Add a new key in `AndroidManifest.xml` to allow/remove notification in foreground.

```xml
        <meta-data  android:name="com.dieam.reactnativepushnotification.notification_foreground"
                    android:value="false"/>
```

### Fixed

- (Android) `number` and `id` are now correctly handled as number in Android.
- (iOS) Update push-notification-ios to 1.2.0 [#1410](https://github.com/zo0r/react-native-push-notification/pull/1410)
- Make sure to import PushNotificationIOS from react-native first [#617](https://github.com/zo0r/react-native-push-notification/pull/617)

## [3.3.1] - 2020-05-01

### Fixed

- (Android) Fix regression with the importance of the notification.

## [3.3.0] - 2020-04-29

### Features

- (Android) Keep interface parity with PushNotificationIOS [#909](https://github.com/zo0r/react-native-push-notification/pull/909)
- (Android) Unsubscribe from topic [#917](https://github.com/zo0r/react-native-push-notification/pull/917)
- (Android) Add notification data in onNotification [#1212](https://github.com/zo0r/react-native-push-notification/pull/1212)

### Fixed

- (Android) Create default channel to receive notification when background / killed.
- (Android) Fix vibrate: false is ignored [#878](https://github.com/zo0r/react-native-push-notification/issues/1140)
- `package.json` fix suffic in main, `index` => `index.js` [#878](https://github.com/zo0r/react-native-push-notification/pull/878)

### Breaking changes

- (Android) Remove specific code for GCM [#1322](https://github.com/zo0r/react-native-push-notification/issues/1322)
- `<service android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationRegistrationService"/>` must be removed.

## [3.2.1] - 2020-04-20

### Fixed

- Invalid type `Strint` wrong typo
- Missing import

## [3.2.0] - 2020-04-20

### Features

- (Android) Allow to silence Android foreground notifications [#1183](https://github.com/zo0r/react-native-push-notification/pull/1183)
- (Android) Allow to set the notification to executes on idle [#959](https://github.com/zo0r/react-native-push-notification/pull/959)
- (iOS) Add missing "category" parameter when scheduling local notifications. [#457](https://github.com/zo0r/react-native-push-notification/pull/457)

### Fixed

- Fix: Breaking android x compatibility regression
- Fix: Use FirebaseInstanceId for deviceToken, not from Intent [#1355](https://github.com/zo0r/react-native-push-notification/pull/1355)
- Fix: security issue `limit the components that Intent will resolve to` [#687](https://github.com/zo0r/react-native-push-notification/pull/687)
- Fix: remove fishy reference from android project files [#1226](https://github.com/zo0r/react-native-push-notification/pull/1226)
- Fix: `JSON value '<null>' of type NSNull cannot be converted to NSDictionary` [#1030](https://github.com/zo0r/react-native-push-notification/pull/1030)
- Fix: Fixed foreground FCM banner notifications and notification sound [#1042](https://github.com/zo0r/react-native-push-notification/pull/1042)
- Upgrade ShortCutBadger to 1.1.22 [#646](https://github.com/zo0r/react-native-push-notification/pull/646)
- Upgrade exemple to React-Native 0.62.2
- Remove Types from the code use [@types/react-native-push-notification](https://github.com/DefinitelyTyped/DefinitelyTyped/blob/master/types/react-native-push-notification) instead.
- Remove GCM and C2DM references in README.md

### Possible Breaking change

- Rename firebaseVersion to firebaseMessagingVersion [#1191](https://github.com/zo0r/react-native-push-notification/pull/1191) in gradle.build

### Documentation

- Abandon permissions unregisters remote only [#1282](https://github.com/zo0r/react-native-push-notification/pull/1282)
- Use full path for manifest [#567](https://github.com/zo0r/react-native-push-notification/pull/567)
- Update broken link to docs [#995](https://github.com/zo0r/react-native-push-notification/pull/995)
- Missing step for android manual installation [#1363](https://github.com/zo0r/react-native-push-notification/pull/1363)

## [3.1.3] - 2019-05-25

## Fixed

- Fix Configuration 'compile' is obsolete and has been replaced with 'implementation' and Configuration 'testCompile' is obsolete and has been replaced with 'testImplementation'.
  It will be removed at the end of 2018. [#1106](https://github.com/zo0r/react-native-push-notification/issues/1106)

## [3.1.2] - 2018-10-16

## Added

- Allow to set notification's priority, visibility and importance options on Android, [@lorenc-tomasz](https://github.com/lorenc-tomasz) `aaf2d19` [#854](https://github.com/zo0r/react-native-push-notification/pull/854)
- Added the "old" GCM listener to get compatibility with GCM back, [@zo0r](https://github.com/zo0r) `3bd0b6f` [#835](https://github.com/zo0r/react-native-push-notification/pull/835)
- Allow configuring notification channel and color through manifest, [@Truebill](https://github.com/Truebill) `f7b4759` [#822](https://github.com/zo0r/react-native-push-notification/pull/822)

## Fixed

- Fix example app cancel notification, [@hshiraiwa](https://github.com/danibonilha) `1143632` [#869](https://github.com/zo0r/react-native-push-notification/pull/869)
- Update documentation to avoid falling Androids' build, [@danibonilha](https://github.com/danibonilha) `fc5c722` [#879](https://github.com/zo0r/react-native-push-notification/pull/879)
- Fix react-native link command, [@lfkwtz](https://github.com/lfkwtz) `9708445` [#839](https://github.com/zo0r/react-native-push-notification/pull/839)
- Standardize Changelog, [@rodrigobdz](https://github.com/rodrigobdz) `a95af74` [#831](https://github.com/zo0r/react-native-push-notification/pull/831)
- Updated sdk and build tool version on gradle file, [@receme](https://github.com/receme) `8718e61` [#826](https://github.com/zo0r/react-native-push-notification/pull/826)
- Fix requestPermissions crash, [@zo0r](https://github.com/zo0r) `feada0c` [#809](https://github.com/zo0r/react-native-push-notification/pull/809)
- Readme clarification on localNotificationSchedule(), [@brownmagik352](https://github.com/brownmagik352) `beedb16` [#816](https://github.com/zo0r/react-native-push-notification/pull/816)
- Fix title and message for Firebase, [@Truebill](https://github.com/Truebill) `ccd9edc` [#806](https://github.com/zo0r/react-native-push-notification/pull/806)
- Fix pop initial for firebase, [@Truebill](https://github.com/Truebill) `b61ce08` [#807](https://github.com/zo0r/react-native-push-notification/pull/807)
- Various readme typos, [@elitree](https://github.com/elitree) `a071458` [#802](https://github.com/zo0r/react-native-push-notification/pull/802)

## [3.1.1] - 2018-07-31

## Added

- Android Oreo support (SDK >= 26) (PR [#657](https://github.com/zo0r/react-native-push-notification/pull/657))
- Firebase (FCM) Support (PR [#717](https://github.com/zo0r/react-native-push-notification/pull/717))
- Twilio support (PR [#744](https://github.com/zo0r/react-native-push-notification/pull/744))
- clearLocalNotification (PR [#711](https://github.com/zo0r/react-native-push-notification/pull/711))

## Fixed

- checkPermissions (PR [#721](https://github.com/zo0r/react-native-push-notification/pull/721))
- Remove default alert for silent push (PR [#707](https://github.com/zo0r/react-native-push-notification/pull/707))

[unreleased]: https://github.com/zo0r/react-native-push-notification/compare/v3.1.2...HEAD
[3.1.2]: https://github.com/zo0r/react-native-push-notification/compare/v3.1.1...v3.1.2
[3.1.1]: https://github.com/zo0r/react-native-push-notification/compare/...v3.1.1


## Supported React Native Versions

| Component Version | RN Versions          | README                                                                                                                 |
| ----------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **1.0.7**         | **<= 0.27**          | [Open](https://github.com/zo0r/react-native-push-notification/blob/f42723817f1687e0da23e6753eb8a9f0385b6ac5/README.md) |
| **1.0.8**         | **0.28**             | [Open](https://github.com/zo0r/react-native-push-notification/blob/2eafd1961273ca6a82ad4dd6514fbf1d1a829089/README.md) |
| **2.0.1**         | **0.29**             | [Open](https://github.com/zo0r/react-native-push-notification/blob/c7ab7cd84ea19e42047379aefaf568bb16a81936/README.md) |
| **2.0.2**         | **0.30, 0.31, 0.32** | [Open](https://github.com/zo0r/react-native-push-notification/blob/a0f7d44e904ba0b92933518e5bf6b444f1c90abb/README.md) |
| **>= 2.1.0**      | **>= 0.33**          | [Open](https://github.com/zo0r/react-native-push-notification/blob/a359e5c00954aa324136eaa9808333d6ca246171/README.md) |
