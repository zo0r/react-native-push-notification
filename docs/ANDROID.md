# Methods Exclusive

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
