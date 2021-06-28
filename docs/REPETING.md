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
