'use strict';

import { NativeModules, DeviceEventEmitter } from "react-native";

let RNPushNotification = NativeModules.ReactNativePushNotification;
let _notifHandlers = new Map();

var DEVICE_NOTIF_EVENT = 'remoteNotificationReceived';
var NOTIF_REGISTER_EVENT = 'remoteNotificationsRegistered';
var NOTIF_ACTION_EVENT = 'notificationActionReceived';
var REMOTE_FETCH_EVENT = 'remoteFetch';

let NotificationsComponent = function() {

};

NotificationsComponent.prototype.getInitialNotification = function () {
    return RNPushNotification.getInitialNotification()
        .then(function (notification) {
            if (notification && notification.dataJSON) {
                return JSON.parse(notification.dataJSON);
            }
            return null;
        });
};

NotificationsComponent.prototype.requestPermissions = function() {
	RNPushNotification.requestPermissions();
};

NotificationsComponent.prototype.subscribeToTopic = function(topic) {
	RNPushNotification.subscribeToTopic(topic);
};

NotificationsComponent.prototype.unsubscribeFromTopic = function(topic) {
	RNPushNotification.unsubscribeFromTopic(topic);
};

NotificationsComponent.prototype.cancelLocalNotifications = function(details) {
	RNPushNotification.cancelLocalNotifications(details);
};

NotificationsComponent.prototype.clearLocalNotification = function(details, tag) {
	RNPushNotification.clearLocalNotification(details, tag);
};

NotificationsComponent.prototype.cancelAllLocalNotifications = function() {
	RNPushNotification.cancelAllLocalNotifications();
};

NotificationsComponent.prototype.presentLocalNotification = function(details) {
	RNPushNotification.presentLocalNotification(details);
};

NotificationsComponent.prototype.scheduleLocalNotification = function(details) {
	RNPushNotification.scheduleLocalNotification(details);
};

NotificationsComponent.prototype.setApplicationIconBadgeNumber = function(number) {
       if (!RNPushNotification.setApplicationIconBadgeNumber) {
               return;
       }
       RNPushNotification.setApplicationIconBadgeNumber(number);
};

NotificationsComponent.prototype.checkPermissions = function(callback) {
	RNPushNotification.checkPermissions().then(alert => callback({ alert }));
};

NotificationsComponent.prototype.addEventListener = function(type, handler) {
	let listener;
	if (type === 'notification') {
		listener =  DeviceEventEmitter.addListener(
			DEVICE_NOTIF_EVENT,
			function(notifData) {
				if (notifData && notifData.dataJSON) {
					let data = JSON.parse(notifData.dataJSON);
					handler(data);
				}
			}
		);
	} else if (type === 'register') {
		listener = DeviceEventEmitter.addListener(
			NOTIF_REGISTER_EVENT,
			function(registrationInfo) {
				handler(registrationInfo.deviceToken);
			}
		);
	} else if (type === 'remoteFetch') {
		listener = DeviceEventEmitter.addListener(
			REMOTE_FETCH_EVENT,
			function(notifData) {
				if (notifData && notifData.dataJSON) {
					let notificationData = JSON.parse(notifData.dataJSON)
					handler(notificationData);
				}
			}
		);
	} else if (type === 'action') {
		listener = DeviceEventEmitter.addListener(
			NOTIF_ACTION_EVENT,
			function(actionData) {
				if (actionData && actionData.dataJSON) {
					var action = JSON.parse(actionData.dataJSON)
					handler(action);
				}
			}
		);
  }

	_notifHandlers.set(type, listener);
};

NotificationsComponent.prototype.removeEventListener = function(type, handler) {
	let listener = _notifHandlers.get(type);
	if (!listener) {
		return;
	}
	listener.remove();
	_notifHandlers.delete(type);
}

NotificationsComponent.prototype.registerNotificationActions = function(details) {
	RNPushNotification.registerNotificationActions(details);
}

NotificationsComponent.prototype.clearAllNotifications = function() {
	RNPushNotification.clearAllNotifications()
}

NotificationsComponent.prototype.removeAllDeliveredNotifications = function() {
  RNPushNotification.removeAllDeliveredNotifications();
}

NotificationsComponent.prototype.getDeliveredNotifications = function(callback) {
  RNPushNotification.getDeliveredNotifications(callback);
}
NotificationsComponent.prototype.getScheduledLocalNotifications = function(callback) {
  RNPushNotification.getScheduledLocalNotifications(callback);
}
NotificationsComponent.prototype.removeDeliveredNotifications = function(identifiers) {
  RNPushNotification.removeDeliveredNotifications(identifiers);
}

NotificationsComponent.prototype.abandonPermissions = function() {
	RNPushNotification.abandonPermissions();
}

NotificationsComponent.prototype.invokeApp = function(data) {
	RNPushNotification.invokeApp(data);
}

NotificationsComponent.prototype.getChannels = function(callback) {
	RNPushNotification.getChannels(callback);
}

NotificationsComponent.prototype.channelExists = function(channel_id, callback) {
	RNPushNotification.channelExists(channel_id, callback);
}

NotificationsComponent.prototype.createChannel = function(channelInfo, callback) {
	RNPushNotification.createChannel(channelInfo, callback);
}

NotificationsComponent.prototype.channelBlocked = function(channel_id, callback) {
	RNPushNotification.channelBlocked(channel_id, callback);
}

NotificationsComponent.prototype.deleteChannel = function(channel_id) {
	RNPushNotification.deleteChannel(channel_id);
}

module.exports = {
	component: new NotificationsComponent()
};
