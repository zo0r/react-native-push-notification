'use strict';

var {
  NativeModules,
  DeviceEventEmitter,
} = require('react-native');

var RNPushNotification = NativeModules.RNPushNotification;
var _notifHandlers = new Map();

var DEVICE_NOTIF_EVENT = 'remoteNotificationReceived';
var NOTIF_REGISTER_EVENT = 'remoteNotificationsRegistered';

var NotificationsComponent = function() {
	this.initalPop = false;
};

NotificationsComponent.prototype.popInitialNotification = function() {
	if ( this.initalPop === false &&
		 RNPushNotification.initialNotification ) {
		this.initalPop = true;
		return JSON.parse(RNPushNotification.initialNotification);
	} else {
		return null;
	}
};

NotificationsComponent.prototype.requestPermissions = function(senderID: string) {
	RNPushNotification.requestPermissions(senderID);
};

NotificationsComponent.prototype.cancelAllLocalNotifications = function() {
	RNPushNotification.cancelAllLocalNotifications();
};

NotificationsComponent.prototype.cancelLocalNotifications = function(details) {
	RNPushNotification.cancelLocalNotifications(details);
};

NotificationsComponent.prototype.presentLocalNotification = function(details: Object) {
	RNPushNotification.presentLocalNotification(details);
};

NotificationsComponent.prototype.scheduleLocalNotification = function(details: Object) {
	RNPushNotification.scheduleLocalNotification(details);
};

NotificationsComponent.prototype.abandonPermissions = function() {
	/* Void */
};

NotificationsComponent.prototype.checkPermissions = function(callback: Function) {
	/* Void */
};

NotificationsComponent.prototype.addEventListener = function(type: string, handler: Function) {
	var listener;
	if (type === 'notification') {
		listener =  DeviceEventEmitter.addListener(
			DEVICE_NOTIF_EVENT,
			function(notifData) {
				var data = JSON.parse(notifData.dataJSON);
				handler(data);
			}
		);
	} else if (type === 'register') {
		listener = DeviceEventEmitter.addListener(
			NOTIF_REGISTER_EVENT,
			function(registrationInfo) {
				handler(registrationInfo.deviceToken);
			}
		);
	}

	_notifHandlers.set(handler, listener);
};

NotificationsComponent.prototype.removeEventListener = function(type: string, handler: Function) {
	var listener = _notifHandlers.get(handler);
	if (!listener) {
		return;
	}
	listener.remove();
	_notifHandlers.delete(handler);
}

NotificationsComponent.prototype.registerNotificationActions = function(details) {
	RNPushNotification.registerNotificationActions(details);
}

module.exports = {
	state: false,
	component: new NotificationsComponent()
};

