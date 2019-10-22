//
//  WLAppJump.m
//  ocs_plugin
//
//  Created by NicoRobine on 2019/10/21.
//

#import "WLAppJump.h"

@implementation WLAppJump

+ (void)handleChannelWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel* appSkip_channel = [FlutterMethodChannel methodChannelWithName:@"ocs.appJump.channel" binaryMessenger:[registrar messenger]];
    [appSkip_channel setMethodCallHandler:^(FlutterMethodCall * _Nonnull call, FlutterResult  _Nonnull result) {
        if ([@"appJump.jump" isEqualToString:call.method]) {
            NSDictionary* paramDict = call.arguments;
            NSString *urlScheme = paramDict[@"identify"];
            NSAssert(urlScheme != nil, @"URLScheme不能为空");
            BOOL rs = [self jumpToAppWithUrlScheme:urlScheme];
            result([NSNumber numberWithBool:rs]);
        }
    }];
}

+ (BOOL)jumpToAppWithUrlScheme:(NSString *)scheme {
    return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@://", scheme]]];
}

@end
