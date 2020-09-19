/**
 * @providesModule Notifications
 */

'use strict';

import { AppState } from "react-native";

var RNNotificationsComponent = require( './component' );

var RNNotifications = RNNotificationsComponent.component;

let Platform = require('react-native').Platform;

var Notifications = {
  handler: RNNotifications,
  onRegister: false,
  onRegistrationError: false,
  onNotification: false,
  onAction: false,
  onRemoteFetch: false,
  isLoaded: false,
  isPopInitialNotification: false,

  isPermissionsRequestPending: false,

  permissions: {
    alert: true,
    badge: true,
    sound: true
  }
};

Notifications.callNative = function(name, params) {
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
 * @param {Object}    options
 * @param {function}  options.onRegister - Fired when the user registers for remote notifications.
 * @param {function}  options.onNotification - Fired when a remote notification is received.
 * @param {function}  options.onAction - Fired when a remote notification is received.
 * @param {function}  options.onRegistrationError - Fired when the user fails to register for remote notifications.
 * @param {Object}    options.permissions - Permissions list
 * @param {Boolean}   options.requestPermissions - Check permissions when register
 */
Notifications.configure = function(options) {
  if ( typeof options.onRegister !== 'undefined' ) {
    this.onRegister = options.onRegister;
  }

  if ( typeof options.onRegistrationError !== 'undefined' ) {
    this.onRegistrationError = options.onRegistrationError;
  }

  if ( typeof options.onNotification !== 'undefined' ) {
    this.onNotification = options.onNotification;
  }

  if ( typeof options.onAction !== 'undefined' ) {
    this.onAction = options.onAction;
  }

  if ( typeof options.permissions !== 'undefined' ) {
    this.permissions = options.permissions;
  }

  if ( typeof options.onRemoteFetch !== 'undefined' ) {
    this.onRemoteFetch = options.onRemoteFetch;
  }

  if ( this.isLoaded === false ) {
    this._onRegister = this._onRegister.bind(this);
    this._onRegistrationError = this._onRegistrationError.bind(this);
    this._onNotification = this._onNotification.bind(this);
    this._onRemoteFetch = this._onRemoteFetch.bind(this);
    this._onAction = this._onAction.bind(this);
    this.callNative( 'addEventListener', [ 'register', this._onRegister ] );
    this.callNative( 'addEventListener', [ 'registrationError', this._onRegistrationError ] );
    this.callNative( 'addEventListener', [ 'notification', this._onNotification ] );
    this.callNative( 'addEventListener', [ 'localNotification', this._onNotification ] );
    Platform.OS === 'android' ? this.callNative( 'addEventListener', [ 'action', this._onAction ] ) : null
    Platform.OS === 'android' ? this.callNative( 'addEventListener', [ 'remoteFetch', this._onRemoteFetch ] ) : null

    this.isLoaded = true;
  }

  const handlePopInitialNotification = (state) => {
    if('active' !== state) {
      return;
    }

    if (options.popInitialNotification === undefined || options.popInitialNotification === true) {
      this.popInitialNotification(function(firstNotification) {
        if(this.isPopInitialNotification) {
          return;
        }
        
        this.isPopInitialNotification = true;
        
        if (!firstNotification || false === firstNotification.userInteraction) {
          return;
        }
        
        this._onNotification(firstNotification, true);
      }.bind(this));
    }
  }

  AppState.addEventListener('change', handlePopInitialNotification.bind(this));

  handlePopInitialNotification(AppState.currentState);

  if ( options.requestPermissions !== false ) {
    this._requestPermissions();
  }
};

/* Unregister */
Notifications.unregister = function() {
  this.callNative( 'removeEventListener', [ 'register', this._onRegister ] )
  this.callNative( 'removeEventListener', [ 'registrationError', this._onRegistrationError ] )
  this.callNative( 'removeEventListener', [ 'notification', this._onNotification ] )
  this.callNative( 'removeEventListener', [ 'localNotification', this._onNotification ] )
  Platform.OS === 'android' ? this.callNative( 'removeEventListener', [ 'action', this._onAction ] ) : null
  Platform.OS === 'android' ? this.callNative( 'removeEventListener', [ 'remoteFetch', this._onRemoteFetch ] ) : null
  this.isLoaded = false;
};

/**
 * Local Notifications
 * @param {Object}    details
 * @param {String}    details.title  -  The title displayed in the notification alert.
 * @param {String}    details.message - The message displayed in the notification alert.
 * @param {String}    details.ticker -  ANDROID ONLY: The ticker displayed in the status bar.
 * @param {Object}    details.userInfo -  iOS ONLY: The userInfo used in the notification alert.
 */
Notifications.localNotification = function(details) {
  if ('android' === Platform.os && details && !details.channelId) {
    console.warn('No channel id passed, notifications may not work.');
  }

  if (details && typeof details.id === 'number') {
    if (isNaN(details.id)) {
      console.warn('NaN value has been passed as id');
      delete details.id;
    }
    else {
      details.id = '' + details.id;
    }
  }

  if (details.userInfo) {
    details.userInfo.id = details.userInfo.id || details.id;
  } else {
    details.userInfo = {id: details.id};
  }

  if (Platform.OS === 'ios') {
    // https://developer.apple.com/reference/uikit/uilocalnotification

    let soundName = details.soundName ? details.soundName : 'default'; // play sound (and vibrate) as default behaviour

    if (details.hasOwnProperty('playSound') && !details.playSound) {
      soundName = ''; // empty string results in no sound (and no vibration)
    }

    // for valid fields see: https://developer.apple.com/library/archive/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/PayloadKeyReference.html
    // alertTitle only valid for apple watch: https://developer.apple.com/library/ios/documentation/iPhone/Reference/UILocalNotification_Class/#//apple_ref/occ/instp/UILocalNotification/alertTitle

    this.handler.presentLocalNotification({
      alertTitle: details.title,
      alertBody: details.message,
      alertAction: details.alertAction,
      category: details.category,
      soundName: soundName,
      applicationIconBadgeNumber: details.number,
      userInfo: details.userInfo
    });
  } else {
    if (details && typeof details.number === 'number') {
      if(isNaN(details.number)) {
        console.warn('NaN value has been passed as number');
        delete details.number;
      }
      else {
        details.number = '' + details.number;
      }
    }

    if (details && typeof details.shortcutId === 'number') {
      if(isNaN(details.shortcutId)) {
        console.warn('NaN value has been passed as shortcutId');
        delete details.shortcutId;
      }
      else {
        details.shortcutId = '' + details.shortcutId;
      }
    }

    if(details && Array.isArray(details.actions)) {
      details.actions = JSON.stringify(details.actions);
    }
  
    this.handler.presentLocalNotification(details);
  }
};

/**
 * Local Notifications Schedule
 * @param {Object}    details (same as localNotification)
 * @param {Date}    details.date - The date and time when the system should deliver the notification
 */
Notifications.localNotificationSchedule = function(details) {
  if ('android' === Platform.os && details && !details.channelId) {
    console.warn('No channel id passed, notifications may not work.');
  }
  
  if (details && typeof details.id === 'number') {
    if(isNaN(details.id)) {
      console.warn('NaN value has been passed as id');
      delete details.id;
    }
    else {
      details.id = '' + details.id;
    }
  }

  if (details.userInfo) {
    details.userInfo.id = details.userInfo.id || details.id;
  } else {
    details.userInfo = {id: details.id};
  }

  if (Platform.OS === 'ios') {
    let soundName = details.soundName ? details.soundName : 'default'; // play sound (and vibrate) as default behaviour

    if (details.hasOwnProperty('playSound') && !details.playSound) {
      soundName = ''; // empty string results in no sound (and no vibration)
    }

    const iosDetails = {
      fireDate: details.date.toISOString(),
      alertTitle: details.title,
      alertBody: details.message,
      category: details.category,
      soundName: soundName,
      userInfo: details.userInfo,
      repeatInterval: details.repeatType,
      category: details.category,
    };

    if (details.number) {
      iosDetails.applicationIconBadgeNumber = parseInt(details.number, 10);
    }

    // ignore Android only repeatType
    if (!details.repeatType || details.repeatType === 'time') {
      delete iosDetails.repeatInterval;
    }
    this.handler.scheduleLocalNotification(iosDetails);
  } else {
    if (details && typeof details.number === 'number') {
      if (isNaN(details.number)) {
        console.warn('NaN value has been passed as number');
        delete details.number;
      }
      else {
        details.number = '' + details.number;
      }
    }

    if (details && typeof details.shortcutId === 'number') {
      if (isNaN(details.shortcutId)) {
        console.warn('NaN value has been passed as shortcutId');
        delete details.shortcutId;
      }
      else {
        details.shortcutId = '' + details.shortcutId;
      }
    }
  
    if(details && Array.isArray(details.actions)) {
      details.actions = JSON.stringify(details.actions);
    }

    details.fireDate = details.date.getTime();
    delete details.date;
    // ignore iOS only repeatType
    if (['year'].includes(details.repeatType)) {
      delete details.repeatType;
    }
    this.handler.scheduleLocalNotification(details);
  }
};

/* Internal Functions */
Notifications._onRegister = function(token) {
  if ( this.onRegister !== false ) {
    this.onRegister({
      token: token,
      os: Platform.OS
    });
  }
};

Notifications._onRegistrationError = function(err) {
  if ( this.onRegistrationError !== false ) {
    this.onRegistrationError(err);
  }
};

Notifications._onRemoteFetch = function(notificationData) {
  if ( this.onRemoteFetch !== false ) {
    this.onRemoteFetch(notificationData)
  }
};

Notifications._onAction = function(notification) {
  if ( typeof notification.data === 'string' ) {
    try {
      notification.data = JSON.parse(notificationData.data);
    } catch(e) {
      /* void */
    }
  }

  this.onAction(notification);
}

Notifications._transformNotificationObject = function(data, isFromBackground = null) {
  if(!data) {
    return;
  }

  if ( isFromBackground === null ) {
    isFromBackground = (
      data.foreground === false ||
      AppState.currentState === 'background'
    );
  }

  let _notification;

  if ( Platform.OS === 'ios' ) {
    const notifData = data.getData();

    _notification = {
      id: notifData?.id,
      foreground: !isFromBackground,
      userInteraction: notifData?.userInteraction === 1 || false,
      message: data.getMessage(),
      data: notifData,
      badge: data.getBadgeCount(),
      title: data.getTitle(),
      soundName: data.getSound(),
      fireDate: Date.parse(data._fireDate),
      finish: (res) => data.finish(res)
    };

    if(isNaN(_notification.fireDate)) {
      delete _notification.fireDate;
    }

  } else {
    _notification = {
      foreground: !isFromBackground,
      finish: () => {},
      ...data,
    };

    if ( typeof _notification.data === 'string' ) {
      try {
        _notification.data = JSON.parse(_notification.data);
      } catch(e) {
        /* void */
      }
    }

    _notification.data = {
      ...(typeof _notification.userInfo === 'object' ? _notification.userInfo : {}),
      ...(typeof _notification.data === 'object' ? _notification.data : {}),
    };

    delete _notification.userInfo;
    delete _notification.notificationId;
  }

  return _notification;
}

Notifications._onNotification = function(data, initialNotification = false) {
  if ( this.onNotification !== false ) {
    let notification = data;

    if(!initialNotification) {
      notification = this._transformNotificationObject(data);
    }

    this.onNotification(notification);
  }
};

/* onResultPermissionResult */
Notifications._onPermissionResult = function() {
  this.isPermissionsRequestPending = false;
};

// Prevent requestPermissions called twice if ios result is pending
Notifications._requestPermissions = function() {
  if ( Platform.OS === 'ios' ) {
    if ( this.isPermissionsRequestPending === false ) {
      this.isPermissionsRequestPending = true;
      return this.callNative( 'requestPermissions', [ this.permissions ])
              .then(this._onPermissionResult.bind(this))
              .catch(this._onPermissionResult.bind(this));
    }
  } else if (Platform.OS === 'android') {
    return this.callNative( 'requestPermissions', []);
  }
};

// Stock requestPermissions function
Notifications.requestPermissions = function() {
  if ( Platform.OS === 'ios' ) {
    return this.callNative( 'requestPermissions', [ this.permissions ]);
  } else if (Platform.OS === 'android') {
    return this.callNative( 'requestPermissions', []);
  }
};

/* Fallback functions */
Notifications.subscribeToTopic = function() {
  return this.callNative('subscribeToTopic', arguments);
};

Notifications.unsubscribeFromTopic = function () {
  return this.callNative('unsubscribeFromTopic', arguments);
};

Notifications.presentLocalNotification = function() {
  return this.callNative('presentLocalNotification', arguments);
};

Notifications.scheduleLocalNotification = function() {
  return this.callNative('scheduleLocalNotification', arguments);
};

Notifications.cancelLocalNotifications = function() {
  return this.callNative('cancelLocalNotifications', arguments);
};

Notifications.clearLocalNotification = function() {
    return this.callNative('clearLocalNotification', arguments);
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

Notifications.popInitialNotification = function(handler) {
  this.callNative('getInitialNotification').then((result) => {
    handler(
      this._transformNotificationObject(result, true)
    );
  });
};

Notifications.checkPermissions = function() {
  return this.callNative('checkPermissions', arguments);
};

/* Abandon Permissions */
Notifications.abandonPermissions = function() {
  return this.callNative('abandonPermissions', arguments);
}

Notifications.clearAllNotifications = function() {
  // Only available for Android
  return this.callNative('clearAllNotifications', arguments)
}

Notifications.removeAllDeliveredNotifications = function() {
  return this.callNative('removeAllDeliveredNotifications', arguments);
}

Notifications.getDeliveredNotifications = function() {
  return this.callNative('getDeliveredNotifications', arguments);
}

Notifications.getScheduledLocalNotifications = function(callback) {
	const mapNotifications = (notifications) => {
		let mappedNotifications = [];
		if(notifications?.length > 0) {
			if(Platform.OS === 'ios'){
				mappedNotifications = notifications.map(notif => {
					return ({
						soundName: notif.soundName,
						repeatInterval: notif.repeatInterval,
						id: notif.userInfo?.id,
            date: new Date(notif.fireDate),
						number: notif?.applicationIconBadgeNumber,
						message: notif?.alertBody,
						title: notif?.alertTitle,
					})
				})
			} else if(Platform.OS === 'android') {
				mappedNotifications = notifications.map(notif => {
					return ({
						soundName: notif.soundName,
						repeatInterval: notif.repeatInterval,
						id: notif.id,
						date: new Date(notif.date),
						number: notif.number,
						message: notif.message,
						title: notif.title,
					})
				})
			}
		}
		callback(mappedNotifications);
	}

	return this.callNative('getScheduledLocalNotifications', [mapNotifications]);
}

Notifications.removeDeliveredNotifications = function() {
  return this.callNative('removeDeliveredNotifications', arguments);
}

Notifications.invokeApp = function() {
  return this.callNative('invokeApp', arguments);
};

Notifications.getChannels = function() {
  return this.callNative('getChannels', arguments);
};

Notifications.channelExists = function() {
  return this.callNative('channelExists', arguments);
};

Notifications.createChannel = function() {
  return this.callNative('createChannel', arguments);
};

Notifications.channelBlocked = function() {
  return this.callNative('channelBlocked', arguments);
};

Notifications.deleteChannel = function() {
  return this.callNative('deleteChannel', arguments);
};

module.exports = Notifications;
