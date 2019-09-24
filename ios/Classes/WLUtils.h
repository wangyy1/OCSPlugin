//
//  WLUtils.h
//  ocs_plugin
//
//  Created by NicoRobine on 2019/9/23.
//

#import <Foundation/Foundation.h>

#define MainScreenWidth [UIScreen mainScreen].bounds.size.width
#define MainScreenHeight [UIScreen mainScreen].bounds.size.height

// status bar height.
#define  kStatusBarHeight      (IS_iPhoneX ? 44.f : 20.f)

// Navigation bar height.
#define  kNavigationBarHeight  44.f

// Tabbar height.
#define  kTabbarHeight        (IS_iPhoneX ? (49.f+34.f) : 49.f)

// Tabbar safe bottom margin.
#define  kTabbarSafeBottomMargin        (IS_iPhoneX ? 34.f : 0.f)

// Status bar & navigation bar height.
#define  kStatusBarAndNavigationBarHeight  (IS_iPhoneX ? 88.f : 64.f)

#define IS_iPhoneX ([UIScreen mainScreen].bounds.size.height == 812 || [UIScreen mainScreen].bounds.size.height == 896)

NS_ASSUME_NONNULL_BEGIN

@interface WLUtils : NSObject

+ (NSString *)getFixedImageName:(NSString *)name;

@end

NS_ASSUME_NONNULL_END
