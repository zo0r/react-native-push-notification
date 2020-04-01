const commands = require('./plugin.js');
module.exports = {
    dependency: {
        platforms: {
            android: {
                "packageInstance": "new ReactNativePushNotificationPackage()"
            } 
        }
    } ,commands
};
