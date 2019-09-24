//
//  ViewController.h
//  OCS地图定位
//
//  Created by jzxl on 15/11/17.
//  Copyright (c) 2015年 jzxl. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol WLLocationViewControllerDelegate <NSObject>

- (void)wlLocationDidSelectedLocationWithLititude:(NSString *)litidute longitude:(NSString *)longitude address:(NSString *)address;

@end

typedef void(^CALL_BACK_BLOCK)(NSString *latitude, NSString *longitude, NSString *locationName);


@interface WLLocationViewController : UIViewController

@property (nonatomic, assign) id <WLLocationViewControllerDelegate>delegate;

@property (nonatomic, copy) CALL_BACK_BLOCK callBack;

@end

