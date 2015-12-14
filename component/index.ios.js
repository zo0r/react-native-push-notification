'use strict';


var React = require('react-native');

var {
	AppStateIOS,
	PushNotificationIOS
} = React;

module.exports = {
	state: AppStateIOS,
	component: PushNotificationIOS
};

