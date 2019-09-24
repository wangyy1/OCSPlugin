//
//  WLMyPositionViewController.h
//  OOC
//
//  Created by jzxl on 15/12/25.
//  Copyright (c) 2015年 lazy. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef void (^ON_BACK_BLOCK)();

@interface WLMyPositionViewController : UIViewController

//@property (nonatomic, copy) NSString *myPosition;

/*!
 纬度值
 */
@property(nonatomic, copy) NSString *latitude;


/*!
 经度值
 */
@property(nonatomic, copy) NSString *longitude;

/*!
 位置说明
 */
@property(nonatomic, copy) NSString *address;


@property(nonatomic, copy) ON_BACK_BLOCK backBlock;

@end
