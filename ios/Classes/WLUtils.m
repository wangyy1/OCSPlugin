//
//  WLUtils.m
//  ocs_plugin
//
//  Created by NicoRobine on 2019/9/23.
//

#import "WLUtils.h"

@implementation WLUtils

+ (NSString *)getFixedImageName:(NSString *)name {
    assert(name != nil || ![name hasSuffix:@".png"]);
    int scale = (int)[UIScreen mainScreen].scale;
    if (scale > 1) {
        NSLog(@"----%@", [NSString stringWithFormat:@"%@@%dx", name,scale]);
        return [NSString stringWithFormat:@"%@@%dx", name,scale];
    }
    return name;
}

@end
