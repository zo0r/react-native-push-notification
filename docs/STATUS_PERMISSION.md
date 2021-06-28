# Status and Caracteristics

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


## Notification while idle

(optional) Specify `allowWhileIdle` to set if the notification should be allowed to execute even when the system is on low-power idle modes.

On Android 6.0 (API level 23) and forward, the Doze was introduced to reduce battery consumption when the device is unused for long periods of time. But while on Doze the AlarmManager alarms (used to show scheduled notifications) are deferred to the next maintenance window. This may cause the notification to be delayed while on Doze.

This can significantly impact the power use of the device when idle. So it must only be used when the notification is required to go off on a exact time, for example on a calendar notification.

More information:
https://developer.android.com/training/monitoring-device-state/doze-standby


# Permissions

## Abandon Permissions

`PushNotification.abandonPermissions()` Revokes the current token and unregister for all remote notifications received via APNS or FCM.

## Checking Notification Permissions

`PushNotification.checkPermissions(callback: Function)` Check permissions

`callback` will be invoked with a `permissions` object:

- `alert`: boolean
- `badge`: boolean
- `sound`: boolean