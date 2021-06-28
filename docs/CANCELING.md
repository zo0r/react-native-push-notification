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

