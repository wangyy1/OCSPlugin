#import "OcsPlugin.h"
#import <BaiduMapAPI_Base/BMKMapManager.h>
#import "WLLocationViewController.h"
#import "WLMyPositionViewController.h"

@interface OcsPlugin() <BMKGeneralDelegate>

@end

@implementation OcsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"ocs_plugin"
                                     binaryMessenger:[registrar messenger]];
    OcsPlugin* instance = [[OcsPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];

    UINavigationBar.appearance.barTintColor = [UIColor colorWithRed:42.0/255 green:151.0/255 blue:240.0/255 alpha:1];
    UINavigationBar.appearance.tintColor = [UIColor whiteColor];
    UINavigationBar.appearance.titleTextAttributes = @{NSForegroundColorAttributeName: [UIColor whiteColor]};
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"getPlatformVersion" isEqualToString:call.method]) {
        result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
    } else if([@"registerKey" isEqualToString:call.method]) {
        // 注册百度key
        NSString* key = call.arguments;
        NSAssert([key isKindOfClass:[NSString class]] && key != nil && key.length > 0, @"百度key必须是字符串");
        NSAssert(key != nil && key.length > 0, @"百度key不能为空");
        BMKMapManager *mapManager = [[BMKMapManager alloc] init];
        bool success = [mapManager start:key generalDelegate:self];
        result([NSNumber numberWithBool:success]);
    } else if ([@"sendLocation" isEqualToString:call.method]) {
        // 选择位置
        WLLocationViewController* locationVC = [[WLLocationViewController alloc] init];
        UINavigationController* locationNavi = [[UINavigationController alloc] initWithRootViewController:locationVC];
        locationNavi.navigationBar.translucent = false;
        locationVC.callBack = ^(NSString *latitude, NSString *longitude, NSString *locationName) {
            if(latitude == nil || longitude == nil || locationName == nil) {
                result(nil);
            } else {
                NSDictionary *dict = @{@"latitude": latitude, @"longitude": longitude, @"address": locationName};
                result(dict);
            }
        };
        [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:locationNavi animated:YES completion:nil];
    } else if ([@"lookLocation" isEqualToString:call.method]) {

        NSDictionary* dict = call.arguments;
        NSAssert([dict isKindOfClass:[NSDictionary class]], @"必须传送Map类型的数据");
        // 选择位置
        WLMyPositionViewController* locationVC = [[WLMyPositionViewController alloc] init];
        UINavigationController* locationNavi = [[UINavigationController alloc] initWithRootViewController:locationVC];
        locationVC.latitude = dict[@"latitude"];
        locationVC.longitude = dict[@"longitude"];
        locationVC.address = dict[@"address"];
        locationNavi.navigationBar.translucent = false;
        locationVC.backBlock = ^{
            result(nil);
        };
        [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:locationNavi animated:YES completion:nil];
    }

    else {
        result(FlutterMethodNotImplemented);
    }
}

/**
 *返回网络错误
 *@param iError 错误号
 */
- (void)onGetNetworkState:(int)iError{
    NSLog(@"onGetNetworkState: %@", @(iError));
}

/**
 *返回授权验证错误
 *@param iError 错误号 : 为0时验证通过，具体参加BMKPermissionCheckResultCode
 */
- (void)onGetPermissionState:(int)iError{
    NSLog(@"onGetPermissionState:%@", @(iError));
}

@end
