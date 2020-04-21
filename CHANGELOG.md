# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
