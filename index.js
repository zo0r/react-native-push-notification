/**
 * @providesModule Notifications
 */

'use strict';

var RNNotificationsComponent = require( './component' );

var AppState = RNNotificationsComponent.state;
var RNNotifications = RNNotificationsComponent.component;

var Platform = require('react-native').Platform;

var Notifications = {
	handler: RNNotifications,
	onRegister: false,
	onError: false,
	onNotification: false,

	loaded: false,

	permissions: {
		alert: true,
		badge: true,
		sound: true
	}
};

Notifications.callNative = function(name: String, params: Array) {
	if ( typeof this.handler[name] === 'function' ) {
		if ( typeof params !== 'array' &&
			 typeof params !== 'object' ) {
			params = [];
		}

		return this.handler[name](...params);
	} else {
		return null;
	}
};

/**
 * Configure local and remote notifications
 * @param {Object}		options
 * @param {function}	options.onRegister - Fired when the user registers for remote notifications.
 * @param {function}	options.onNotification - Fired when a remote notification is received.
 * @param {function} 	options.onError - None
 * @param {Object}		options.permissions - Permissions list
 * @param {Boolean}		options.requestPermissions - Check permissions when register
 */
Notifications.configure = function(options: Object) {
	if ( typeof options.onRegister !== 'undefined' ) {
		this.onRegister = options.onRegister;
	}

	if ( typeof options.onError !== 'undefined' ) {
		this.onError = options.onError;
	}

	if ( typeof options.onNotification !== 'undefined' ) {
		this.onNotification = options.onNotification;
	}

	if ( typeof options.permissions !== 'undefined' ) {
		this.permissions = options.permissions;
	}

	if ( typeof options.senderID !== 'undefined' ) {
		this.senderID = options.senderID;
	}

	if ( this.loaded === false ) {
		this.callNative( 'addEventListener', [ 'register', this._onRegister.bind(this) ] )
		this.callNative( 'addEventListener', [ 'notification', this._onNotification.bind(this) ] )

		if ( typeof options.popInitialNotification === 'undefined' || options.popInitialNotification === true ) {
			var tempFirstNotification = this.callNative( 'popInitialNotification' );

			if ( tempFirstNotification !== null ) {
				this._onNotification(tempFirstNotification, true);
			}
		}

		this.loaded = true;
	}

	if ( options.requestPermissions !== false ) {
		this.requestPermissions();
	}

};

/* Unregister */
Notifications.unregister = function() {
	this.callNative( 'removeEventListener', [ 'register', this._onRegister.bind(this) ] )
	this.callNative( 'removeEventListener', [ 'notification', this._onNotification.bind(this) ] )
};

/**
 * Local Notifications
 * @param {Object}		details
 * @param {String}		details.message - The message displayed in the notification alert.
 * @param {String}		details.title  -  ANDROID ONLY: The title displayed in the notification alert.
 * @param {String}		details.ticker -  ANDROID ONLY: The ticker displayed in the status bar.
 */
Notifications.localNotification = function(details: Object) {
	if ( Platform.OS === 'ios' ) {
		this.handler.presentLocalNotification({
			alertBody: details.message
		});
	} else {
		this.handler.presentLocalNotification(details);
	}
};

/**
 * Local Notifications Schedule
 * @param {Object}		details (same as localNotification)
 * @param {Date}		details.date - The date and time when the system should deliver the notification
 */
Notifications.localNotificationSchedule = function(details: Object) {
	if ( Platform.OS === 'ios' ) {
		this.handler.scheduleLocalNotification({
			fireDate: details.date,
			alertBody: details.message
		});
	} else {
		this.handler.scheduleLocalNotification(details);
	}
};

/* Internal Functions */
Notifications._onRegister = function(token: String) {
	if ( this.onRegister !== false ) {
		this.onRegister({
			token: token,
			os: Platform.OS
		});
	}
};

Notifications._onNotification = function(data, isFromBackground = null) {
	if ( isFromBackground === null ) {
		if ( Platform.OS === 'ios' ) {
			isFromBackground = ( AppState.currentState === 'background' );
		} else {
			isFromBackground = ( data.foreground === false );
		}
	}

	if ( this.onNotification !== false ) {
		if ( Platform.OS === 'ios' ) {
			this.onNotification({
				foreground: ! isFromBackground,
				message: data.getMessage(),
				data: data.getData(),
			});
		} else {
			this.onNotification({
				foreground: ! isFromBackground,
				message: data.message,
				data: (
					typeof data.data !== 'undefined'
					? data.data
					: {} 
				),
			});
		}
	}
};

Notifications.requestPermissions = function() {
	if ( Platform.OS === 'ios' ) {
		return this.callNative( 'requestPermissions', [ this.permissions ]);
	} else if ( typeof this.senderID !== 'undefined' ) {
		return this.callNative( 'requestPermissions', [ this.senderID ]);
	}
};

/* Fallback functions */
Notifications.presentLocalNotification = function() {
	return this.callNative('presentLocalNotification', arguments);
};

Notifications.scheduleLocalNotification = function() {
	return this.callNative('scheduleLocalNotification', arguments);
};

Notifications.cancelAllLocalNotifications = function() {
	return this.callNative('cancelAllLocalNotifications', arguments);
};

Notifications.cancelAllLocalNotifications = function() {
	return this.callNative('cancelAllLocalNotifications', arguments);
};

Notifications.setApplicationIconBadgeNumber = function() {
	return this.callNative('setApplicationIconBadgeNumber', arguments);
};

Notifications.getApplicationIconBadgeNumber = function() {
	return this.callNative('getApplicationIconBadgeNumber', arguments);
};

Notifications.popInitialNotification = function() {
	return this.callNative('popInitialNotification', arguments);
};

Notifications.abandonPermissions = function() {
	return this.callNative('abandonPermissions', arguments);
};

Notifications.checkPermissions = function() {
	return this.callNative('checkPermissions', arguments);
};

module.exports = Notifications;
