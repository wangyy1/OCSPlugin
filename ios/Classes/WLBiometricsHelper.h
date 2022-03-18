//
//  WLBiometricsHelper.h
//  OOC
//
//  Created by NicoRobine on 2021/4/28.
//  Copyright © 2021 lazy. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <LocalAuthentication/LocalAuthentication.h>
#import <Flutter/Flutter.h>

//typedef NS_ENUM(NSUInteger, WLBiometricsType) {
//    WLBiometricsTypeNone,     // 设备不支持生物识别
//    WLBiometricsTypeTouchID,  // 使用Touch ID进行识别
//    WLBiometricsTypeFaceID,   // 使用Face ID进行识别
//};

NS_ASSUME_NONNULL_BEGIN

@interface WLBiometricsHelper : NSObject

+ (instancetype)shared;

/**
 * 注册插件
 */
- (void)handleChannelWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar;

/// 当前设备是否支持生物识别
- (BOOL)canUseBiometrics;

/**
 * 当前设置使用的生物识别的类型
 * @brief 判断设备支持Touch ID还是Face ID，或者都不支持
 */
- (LABiometryType)biometricsType API_AVAILABLE(ios(11.0));

/**
 * 开始生物识别
 * @param title 生物识别的标识
 */
- (void)startBiometricWithTitle:(NSString *)title reply:(void(^)(BOOL success, NSError * __nullable error))reply;

@end

NS_ASSUME_NONNULL_END
