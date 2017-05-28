'use strict';

import 'react-native';

describe('Test popInitialNotification method', function() {
    let Index;
    const mockIOSNotification = '{"_data":{"msgType":"iosTest","smallIcon":"notification_icon","notificationId":"463eec63-0470-40a9-85aa-c4c3554375cb","remote":true,"largeIcon":"notification_icon","testUrl":"http://google.com","notificationType":"debug","timestamp":1495956902971},"_remoteNotificationCompleteCalllbackCalled":false,"_isRemote":true,"_notificationId":"463eec63-0470-40a9-85aa-c4c3554375cb","_alert":"Got Push!","_sound":"default","_badgeCount":1}';
    const mockAndroidNotification = '{"google.sent_time":1495978487418,"smallIcon":"notification_icon","userInteraction":true,"testUrl":"http://google.com","id":"-198084357","timestamp":"1495978487399","notId":"-534853452","title":"Got Push !","google.message_id":"0:1495978487425830%5336a3cef9fd7ecd","largeIcon":"notification_icon","notificationId":"0cd2ab63-2745-4d99-ab31-d12b9493db7c","message":"How awesome..","msgType":"androidTest","notificationType":"debug","foreground":false}';
    let mockNotification;

    function testWithNoCustomHandler(done){
        const mockOnNotification = jest.fn();
        Index._onNotification = mockOnNotification;
        Index.popInitialNotification();
        setTimeout(function () {
            expect(mockOnNotification).toBeCalledWith(JSON.parse(mockNotification), true);
            done();
        }, 300);

    };

    function testWithCustomHandler(done){
        const mockOnNotification = jest.fn();
        const mockCustomHandler = jest.fn();
        Index._onNotification = mockOnNotification;
        Index.popInitialNotification(mockCustomHandler);
        setTimeout(function () {
            expect(mockOnNotification).not.toBeCalled();
            expect(mockCustomHandler).toBeCalledWith(JSON.parse(mockNotification));
            done();
        }, 300);

    }

    function testWithPopInitialConfigurationTrue(done){
        const mockOnNotification = jest.fn();
        Index._onNotification = mockOnNotification;
        Index.configure({
            requestPermissions: false,
            popInitialNotification: true
        });

        setTimeout(function () {
            expect(mockOnNotification).toBeCalledWith(JSON.parse(mockNotification), true);
            done();
        }, 300);

    }

    describe('on iOS', function(){
        beforeAll(function(){
            jest.mock('PushNotificationIOS', function() {
                return {
                    getInitialNotification: function() {
                        return new Promise(function(resolve){
                            resolve(JSON.parse(mockIOSNotification));
                        });
                    }
                };
            });
            Index = require('../index');
            mockNotification = mockIOSNotification;
        });

        it('with no custom handler', testWithNoCustomHandler);

        it('with custom handler', testWithCustomHandler);

        it('with popInitialNotification configuration set to true', testWithPopInitialConfigurationTrue);
    });

    describe('on Android', function(){
        beforeAll(function(){
            jest.resetModules();
            jest.unmock('PushNotificationIOS');

            jest.mock('../component', function(){
                let RNNotificationsComponent = require('../component/index.android');
                RNNotificationsComponent.component.getInitialNotification = function() {
                    return new Promise(function (resolve) {
                        resolve(JSON.parse(mockAndroidNotification));
                    });
                };
                return RNNotificationsComponent;
            });

            Index = require('../index');

            mockNotification = mockAndroidNotification;
        });

        it('with no custom handler', testWithNoCustomHandler);

        it('with custom handler', testWithCustomHandler);

        it('with popInitialNotification configuration set to true', testWithPopInitialConfigurationTrue);
    });


});