# React Native Push Notifications

[![npm version](https://badge.fury.io/js/react-native-push-notification.svg?update=9)](http://badge.fury.io/js/react-native-push-notification)
[![npm downloads](https://img.shields.io/npm/dm/react-native-push-notification.svg?update=9)](http://badge.fury.io/js/react-native-push-notification)

React Native Local and Remote Notifications for iOS and Android

## Documents

[Instalation](https://github.com/zo0r/react-native-push-notification/blob/master/docs/INSTALLATION.m)

[Usage] (https://github.com/zo0r/react-native-push-notification/blob/master/docs/USAGE.m)

[LocalNotifications] (https://github.com/zo0r/react-native-push-notification/blob/master/docs/LOCAL.m)


## 🎉 Version 7.x is live ! 🎉

Check out for changes and migration in the CHANGELOG:

[Changelog](https://github.com/zo0r/react-native-push-notification/blob/master/CHANGELOG.md)

# Supporting the project

Maintainers are welcome ! Feel free to contact me :wink:

## Changelog

Changelog is available from version 3.1.3 here: [Changelog](https://github.com/zo0r/react-native-push-notification/blob/master/CHANGELOG.md)


## Issues

Having a problem? Read the [troubleshooting](./trouble-shooting.md) guide before raising an issue.

## Pull Requests

[Please read...](./submitting-a-pull-request.md)

## Scheduled Notifications

`PushNotification.localNotificationSchedule(details: Object)`

EXAMPLE:

```javascript
PushNotification.localNotificationSchedule({
  //... You can use all the options from localNotifications
  message: "My Notification Message", // (required)
  date: new Date(Date.now() + 60 * 1000), // in 60 secs
  allowWhileIdle: false, // (optional) set notification to work while on doze, default: false

  /* Android Only Properties */
  repeatTime: 1, // (optional) Increment of configured repeatType. Check 'Repeating Notifications' section for more info.
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

### 3) removeAllDeliveredNotifications

```javascript
PushNotification.removeAllDeliveredNotifications();
```

Remove all delivered notifications from Notification Center

### 4) getDeliveredNotifications

```javascript
PushNotification.getDeliveredNotifications(callback);
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
PushNotification.removeDeliveredNotifications(identifiers);
```

Removes the specified notifications from Notification Center

**Parameters:**

| Name        | Type  | Required | Description                        |
| ----------- | ----- | -------- | ---------------------------------- |
| identifiers | array | Yes      | Array of notification identifiers. |

### 6) getScheduledLocalNotifications

```javascript
PushNotification.getScheduledLocalNotifications(callback);
```

Provides you with a list of the app’s scheduled local notifications that are yet to be displayed

**Parameters:**

| Name     | Type     | Required | Description                                                 |
| -------- | -------- | -------- | ----------------------------------------------------------- |
| callback | function | Yes      | Function which receive an array of delivered notifications. |

Returns an array of local scheduled notification objects containing:

| Name           | Type   | Description                                              |
| -------------- | ------ | -------------------------------------------------------- |
| id             | number | The identifier of this notification.                     |
| date           | Date   | The fire date of this notification.                      |
| title          | string | The title of this notification.                          |
| message        | string | The message body of this notification.                   |
| soundName      | string | The sound name of this notification.                     |
| repeatInterval | number | (Android only) The repeat interval of this notification. |
| number         | number | App notification badge count number.                     |
| data           | any    | The user info of this notification.                      |


## Repeating Notifications

(optional) Specify `repeatType` and optionally `repeatTime` (Android-only) while scheduling the local notification. Check the local notification example above.

### iOS
Property `repeatType` can only be `day`.

### Android
Property `repeatType` could be one of `month`, `week`, `day`, `hour`, `minute`, `time`. 

The interval used can be configured to a different interval using `repeatTime`. If `repeatType` is `time`, `repeatTime` must be specified as the number of milliseconds between each interval.
For example, to configure a notification every other day
```javascript
PushNotification.localNotificationSchedule({
    ...
    repeatType: 'day',
    repeatTime: 2,
    ...
});
```


## Set application badge icon

`PushNotification.setApplicationIconBadgeNumber(number: number)`

Works natively in iOS.

Uses the [ShortcutBadger](https://github.com/leolin310148/ShortcutBadger) on Android, and as such will not work on all Android devices.

## Android Only Methods

`PushNotification.subscribeToTopic(topic: string)` Subscribe to a topic (works only with Firebase)

`PushNotification.unsubscribeFromTopic(topic: string)` Unsubscribe from a topic (works only with Firebase)

## Android Custom Notification Handling

Unlike iOS, Android apps handle the creation of their own notifications. React Native Push Notifications does a "best guess" to create and handle incoming notifications. However, when using 3rd party notification platforms and tools, the initial notification creation process may need to be customized.

### Customizing Notification Creation

If your notification service uses a custom data payload format, React Native Push Notifications will not be able to parse the data correctly to create an initial notification.

For these cases, you should:

1. Remove the intent handler configuration for React Native Push Notifications from your `android/app/src/main/AndroidManifest.xml`.
2. Implement initial notification creation as per the instructions from your Provider.

### Handling Custom Payloads

Data payloads of notifications from 3rd party services may not match the format expected by React Native Push Notification. When tapped, these notifications will not pass the details and data to the `onNotification()` event handler. Custom `IntentHandlers` allow you to fix this so that correct `notification` objects are sent to your `onNotification()` method.

Custom handlers are added in Application init or `MainActivity.onCreate()` methods:

```
RNPushNotification.IntentHandlers.add(new RNPushNotification.RNIntentHandler() {
  @Override
  public void onNewIntent(Intent intent) {
    // If your provider requires some parsing on the intent before the data can be
    // used, add that code here. Otherwise leave empty.
  }

  @Nullable
  @Override
  public Bundle getBundleFromIntent(Intent intent) {
    // This should return the bundle data that will be serialized to the `notification.data`
    // property sent to the `onNotification()` handler. Return `null` if there is no data
    // or this is not an intent from your provider.
    
    // Example:
    if (intent.hasExtra("MY_NOTIFICATION_PROVIDER_DATA_KEY")) {
      return intent.getBundleExtra("MY_NOTIFICATION_PROVIDER_DATA_KEY");
    }
    return null;
  }
});
```

## Checking Notification Permissions

`PushNotification.checkPermissions(callback: Function)` Check permissions

`callback` will be invoked with a `permissions` object:

- `alert`: boolean
- `badge`: boolean
- `sound`: boolean

## iOS Only Methods

`PushNotification.getApplicationIconBadgeNumber(callback: Function)` Get badge number
