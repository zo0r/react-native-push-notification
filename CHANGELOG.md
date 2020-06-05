# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Breaking changes

- Now local scheduled notifications trigger `onNotification` before display [#574](https://github.com/zo0r/react-native-push-notification/pull/574).
- `RNPushNotificationRegistrationService` has been removed, old reference in AndroidManifest must be removed.
- `Notifications.registerNotificationActions()` has been removed and is not required for `actions`.
- `DeviceEventEmitter.addListener('notificationActionReceived', callback)` is replaced by `onAction`.
- Extra receiver must be added to manage actions.
  ```xml
      <receiver android:name="com.dieam.reactnativepushnotification.modules.RNPushNotificationActions" />
  ```

### Features

- (Android) `actions` accept an array of strings.
- (Android) `invokeApp` allow you to handle actions in background without invoking the application.
- (Android) `onAction` has been added to `.configure()` to handle action in background.
- (Android) `PushNotification.invokeApp(notification)` allow you to invoke the application when in background (notification for initial notification).
- (Android) `PushNotification.getChannels(callback)` allow you to get the list of channels.
- (Android) `PushNotification.channelExists(channel_id, callback)` allow you to check of a channel exists.
- (Android) `PushNotification.deleteChannel(channel_id)` allow you to delete a channel.
- (Android) Add `largeIconUrl` to load a largeIcon based on Url. Based on [#1444](https://github.com/zo0r/react-native-push-notification/pull/1444)
- (Android) Add `bigPictureUrl` to load a picture based on Url. Based on [#1444](https://github.com/zo0r/react-native-push-notification/pull/1444)
- (Android) Add `shortcutId` for better badges management.
- (Android) Add `showWhen` to display "when" it was published, default: true.
- (Android) Add `groupSummary` to allow grouping notifications. Based on [#1253](https://github.com/zo0r/react-native-push-notification/pull/1253)
- (Android) Add `channelId`, custom channel_id in android. Based on [#1159](https://github.com/zo0r/react-native-push-notification/pull/1159)
- (iOS) Add fire date in notification response, NOTE: `push-notification-ios` in version `> 1.2.0` [#1345](https://github.com/zo0r/react-native-push-notification/pull/1345)

### Fixed

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
