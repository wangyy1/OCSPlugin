//
//  WLAppJump.h
//  ocs_plugin
//
//  Created by NicoRobine on 2019/10/21.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface WLAppJump : NSObject

+ (void)handleChannelWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar;

+ (BOOL)jumpToAppWithUrlScheme:(NSString *)scheme;

@end

NS_ASSUME_NONNULL_END
