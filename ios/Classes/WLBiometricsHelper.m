//
//  WLBiometricsHelper.m
//  OOC
//
//  Created by NicoRobine on 2021/4/28.
//  Copyright © 2021 lazy. All rights reserved.
//

#import "WLBiometricsHelper.h"

static NSString* const OCS_BIOMETRICS_CHANNEL_NAME = @"ocs.biometric.channel";
static NSString* const OCS_BIOMETRICS_METHOD_ENABLE = @"canAuthenticate";
static NSString* const OCS_BIOMETRICS_METHOD_AUTHENTICATE = @"authenticate";


@interface WLBiometricsHelper ()
{
    LAContext* _laContext;
}
@end

@implementation WLBiometricsHelper

static WLBiometricsHelper * _sharedBiometricsHelper;

#pragma mark - Life cycle

+ (instancetype)shared {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedBiometricsHelper = [[WLBiometricsHelper alloc] init];
    });
    return _sharedBiometricsHelper;
}

- (instancetype)init {
    if (self = [super init]) {
        [self wl_setup];
    }
    return self;
}

- (void)wl_setup {
    _laContext = [[LAContext alloc] init];
}

- (void)handleChannelWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel* appSkip_channel = [FlutterMethodChannel methodChannelWithName:OCS_BIOMETRICS_CHANNEL_NAME binaryMessenger:[registrar messenger]];
    [appSkip_channel setMethodCallHandler:^(FlutterMethodCall * _Nonnull call, FlutterResult  _Nonnull result) {
        if ([OCS_BIOMETRICS_METHOD_ENABLE isEqualToString:call.method]) {
            int rs = [self canUseBiometrics] ? 0 : 3;
            result([NSNumber numberWithInteger:rs]);
        } else if ([OCS_BIOMETRICS_METHOD_AUTHENTICATE isEqualToString:call.method]) {
            [self startBiometricWithTitle:@"登录" reply:^(BOOL success, NSError * _Nullable error) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    result(@{@"result": [NSNumber numberWithBool:success], @"msg": success ? @"生物验证成功" : @"生物验证失败"});
                });
            }];

        }
    }];
}

- (BOOL)canUseBiometrics {
    return [_laContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:nil];
}

- (LABiometryType)biometricsType  API_AVAILABLE(ios(11.0)) {
    if (@available(iOS 11.0, *)) {
        return _laContext.biometryType;
    } else {
        return LABiometryTypeTouchID;
    }
}

- (void)startBiometricWithTitle:(NSString *)title reply:(nonnull void (^)(BOOL, NSError * _Nullable))reply {
    [_laContext invalidate];
    _laContext = [[LAContext alloc] init];
    [_laContext evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:title reply:^(BOOL success, NSError * _Nullable error) {
        if (success) {
            NSLog(@"Biometric success");
        } else {
            // 6.根据用户授权状态进行下一步操作
            switch (error.code) {
                case LAErrorUserCancel:
                    NSLog(@"用户取消了授权 - %@", error.localizedDescription);
                    break;
                case LAErrorUserFallback:
                    NSLog(@"用户点击了“输入密码”按钮 - %@", error.localizedDescription);
                    break;
                case LAErrorAuthenticationFailed:
                    NSLog(@"您已授权失败3次 - %@", error.localizedDescription);
                    break;
                case LAErrorSystemCancel:
                    NSLog(@"应用程序进入后台 - %@", error.localizedDescription);
                    break;
                default:
                    NSLog(@"++%@--%zd", error.localizedDescription, error.code);
                    break;
            }
        }
        if (reply) {
            reply(success, error);
        }
    }];
}

- (void)dealloc {
    [_laContext invalidate];
}

@end
