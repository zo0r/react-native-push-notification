## Handling Notifications

When any notification is opened or received the callback `onNotification` is called passing an object with the notification data.

Notification object example:

```javascript
{
    foreground: false, // BOOLEAN: If the notification was received in foreground or not
    userInteraction: false, // BOOLEAN: If the notification was opened by the user from the notification area or not
    message: 'My Notification Message', // STRING: The notification message
    data: {}, // OBJECT: The push data or the defined userInfo in local notifications
}
```

## Notification priority

(optional) Specify `priority` to set priority of notification. Default value: "high"

Available options:

"max" = NotficationCompat.PRIORITY_MAX\
"high" = NotficationCompat.PRIORITY_HIGH\
"low" = NotficationCompat.PRIORITY_LOW\
"min" = NotficationCompat.PRIORITY_MIN\
"default" = NotficationCompat.PRIORITY_DEFAULT

More information: https://developer.android.com/reference/android/app/Notification.html#PRIORITY_DEFAULT

## Notification visibility

(optional) Specify `visibility` to set visibility of notification. Default value: "private"

Available options:

"private" = NotficationCompat.VISIBILITY_PRIVATE\
"public" = NotficationCompat.VISIBILITY_PUBLIC\
"secret" = NotficationCompat.VISIBILITY_SECRET 

More information: https://developer.android.com/reference/android/app/Notification.html#VISIBILITY_PRIVATE

## Notification importance

(optional) Specify `importance` to set importance of notification. Default value: Importance.HIGH  
Constants available on the `Importance` object. `import PushNotification, {Importance} from 'react-native-push-notification';`

Available options:

Importance.DEFAULT = NotificationManager.IMPORTANCE_DEFAULT\
Importance.HIGH = NotificationManager.IMPORTANCE_HIGH\
Importance.LOW = NotificationManager.IMPORTANCE_LOW\
Importance.MIN = NotificationManager.IMPORTANCE_MIN\
Importance.NONE= NotificationManager.IMPORTANCE_NONE\
Importance.UNSPECIFIED = NotificationManager.IMPORTANCE_UNSPECIFIED

More information: https://developer.android.com/reference/android/app/NotificationManager#IMPORTANCE_DEFAULT

## Show notifications while the app is in foreground

If you want a consistent results in Android & iOS with the most flexibility, it is best to handle it manually by prompting a local notification when `onNotification` is triggered by a remote push notification on foreground (check `notification.foreground` prop).

Watch out for an infinite loop triggering `onNotification` - remote & local notification will trigger it. You can overcome this by marking local notifications' data.

## Notification while idle

(optional) Specify `allowWhileIdle` to set if the notification should be allowed to execute even when the system is on low-power idle modes.

On Android 6.0 (API level 23) and forward, the Doze was introduced to reduce battery consumption when the device is unused for long periods of time. But while on Doze the AlarmManager alarms (used to show scheduled notifications) are deferred to the next maintenance window. This may cause the notification to be delayed while on Doze.

This can significantly impact the power use of the device when idle. So it must only be used when the notification is required to go off on a exact time, for example on a calendar notification.

More information:
https://developer.android.com/training/monitoring-device-state/doze-standby


# Permissions

## Abandon Permissions

`PushNotification.abandonPermissions()` Revokes the current token and unregister for all remote notifications received via APNS or FCM.
