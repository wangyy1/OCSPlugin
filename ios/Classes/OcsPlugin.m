#import "OcsPlugin.h"
#import <BaiduMapAPI_Base/BMKMapManager.h>
#import "WLLocationViewController.h"
#import "WLMyPositionViewController.h"
#import <UIKit/UIKit.h>
#import <AVKit/AVKit.h>
#import <AVFoundation/AVFoundation.h>

@interface BMKRegisterDelegate : NSObject <BMKGeneralDelegate>

@end

@implementation BMKRegisterDelegate

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

NSString *const AudioplayersPluginStop = @"OCSAudioplayersPluginStop";
typedef void (^OCSVoidCallback)(NSString * playerId);
/// 播放时间的一些监听器
NSMutableSet *ocs_timeobservers;
/// 一些硬件的监听，距离传感器，耳机插拔
NSMutableSet *ocs_deviceObservers;
/// 音频通道
FlutterMethodChannel *_ocs_channel_audioplayer;
/// 是否已经被释放
bool _ocs_is_dealloc = false;
/// 是否启动距离传感器，自动切换听筒和扬声器
bool proximityMonitoringEnabled = false;
/// 播放缓存
static NSMutableDictionary * ocsPlayers;

@interface OcsPlugin()
/// 暂停
-(void) pause: (NSString *) playerId;
/// 停止
-(void) stop: (NSString *) playerId;
/// 到指定位置
-(void) seek: (NSString *) playerId time: (CMTime) time;
/// 完成回调
-(void) onSoundComplete: (NSString *) playerId;
/// 更新进度
-(void) updateDuration: (NSString *) playerId;
-(void) onTimeInterval: (NSString *) playerId time: (CMTime) time;

@end

@implementation OcsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    
    OcsPlugin* instance = [[OcsPlugin alloc] init];
    
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"ocs_plugin"
                                     binaryMessenger:[registrar messenger]];
    // 设置handler没有用
    [channel setMethodCallHandler:^(FlutterMethodCall * _Nonnull call, FlutterResult  _Nonnull result) {
        if ([@"getPlatformVersion" isEqualToString:call.method]) {
            result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
        } else if([@"registerKey" isEqualToString:call.method]) {
            // 注册百度key
            NSString* key = call.arguments;
            NSAssert([key isKindOfClass:[NSString class]] && key != nil && key.length > 0, @"百度key必须是字符串");
            NSAssert(key != nil && key.length > 0, @"百度key不能为空");
            BMKMapManager *mapManager = [[BMKMapManager alloc] init];
            bool success = [mapManager start:key generalDelegate:[BMKRegisterDelegate new]];
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
    }];
    
    FlutterMethodChannel* audioPlayer_channel = [FlutterMethodChannel
                                                 methodChannelWithName:@"ocs.audioPlayer.channel"
                                                 binaryMessenger:[registrar messenger]];
    _ocs_channel_audioplayer = audioPlayer_channel;
    
    [audioPlayer_channel setMethodCallHandler:^(FlutterMethodCall * _Nonnull call, FlutterResult  _Nonnull result) {
        
        
        NSString * playerId = call.arguments[@"playerId"];
        NSLog(@"iOS => call %@, playerId %@", call.method, playerId);
        
        typedef void (^CaseBlock)(void);
        
        // Squint and this looks like a proper switch!
        NSDictionary *methods = @{
                                  @"play":
                                      ^{
                                          NSLog(@"play!");
                                          NSString *url = call.arguments[@"url"];
                                          if (url == nil)
                                              result(0);
                                          if (call.arguments[@"isLocal"] == nil)
                                              result(0);
                                          if (call.arguments[@"volume"] == nil)
                                              result(0);
                                          if (call.arguments[@"position"] == nil)
                                              result(0);
                                          int isLocal = [call.arguments[@"isLocal"]intValue] ;
                                          float volume = (float)[call.arguments[@"volume"] doubleValue] ;
                                          int milliseconds = call.arguments[@"position"] == [NSNull null] ? 0.0 : [call.arguments[@"position"] intValue] ;
                                          BOOL proximityMonitoringEnabled = [call.arguments[@"proximityEnable"] boolValue];
                                          CMTime time = CMTimeMakeWithSeconds(milliseconds / 1000,NSEC_PER_SEC);
                                          [instance play:playerId url:url isLocal:isLocal volume:volume time:time proximityMonitoringEnabled:proximityMonitoringEnabled];
                                      },
                                  @"pause":
                                      ^{
                                          NSLog(@"pause");
                                          [instance pause:playerId];
                                      },
                                  @"resume":
                                      ^{
                                          NSLog(@"resume");
                                          [instance resume:playerId];
                                      },
                                  @"stop":
                                      ^{
                                          NSLog(@"stop");
                                          [instance stop:playerId];
                                      },
                                  @"release":
                                      ^{
                                          NSLog(@"release");
                                          [instance stop:playerId];
                                      },
                                  @"seek":
                                      ^{
                                          NSLog(@"seek");
                                          if (!call.arguments[@"position"]) {
                                              result(0);
                                          } else {
                                              int milliseconds = [call.arguments[@"position"] intValue];
                                              NSLog(@"Seeking to: %d milliseconds", milliseconds);
                                              [instance seek:playerId time:CMTimeMakeWithSeconds(milliseconds / 1000,NSEC_PER_SEC)];
                                          }
                                      },
                                  @"setUrl":
                                      ^{
                                          NSLog(@"setUrl");
                                          NSString *url = call.arguments[@"url"];
                                          int isLocal = [call.arguments[@"isLocal"]intValue];
                                          [instance setUrl:url
                                                isLocal:isLocal
                                               playerId:playerId
                                           proximityMonitoringEnabled:false
                                                onReady:^(NSString * playerId) {
                                                    result(@(1));
                                                }
                                           ];
                                      },
                                  @"getDuration":
                                      ^{
                                          
                                          int duration = [instance getDuration:playerId];
                                          NSLog(@"getDuration: %i ", duration);
                                          result(@(duration));
                                      },
                                  @"setVolume":
                                      ^{
                                          NSLog(@"setVolume");
                                          float volume = (float)[call.arguments[@"volume"] doubleValue];
                                          [instance setVolume:volume playerId:playerId];
                                      },
                                  @"setReleaseMode":
                                      ^{
                                          NSLog(@"setReleaseMode");
                                          NSString *releaseMode = call.arguments[@"releaseMode"];
                                          bool looping = [releaseMode hasSuffix:@"LOOP"];
                                          [instance setLooping:looping playerId:playerId];
                                      },
                                  @"setProximityMonitoring":
                                      ^{
                                          bool proximityMonitoring = call.arguments;
                                          if (proximityMonitoring) {
                                              [instance monitorProximityMonitor];
                                          } else {
                                              [instance removeMonitorProximityMonitor];
                                          }
                                      }
                                  };
        
        // 根据playerId初始化播放信息
        [ instance initPlayerInfo:playerId ];
        // 根据不同的通道方法获取对应的执行block，然后执行
        // 如果找不到返回方法没有实现
        CaseBlock c = methods[call.method];
        if (c) c(); else {
            NSLog(@"not implemented");
            result(FlutterMethodNotImplemented);
        }
        // 如果不是通道调用的不是setUrl方法给通道返回成功
        if(![call.method isEqualToString:@"setUrl"]) {
            result(@(1));
        }
        
    }];
    
    
//    OcsPlugin* instance = [[OcsPlugin alloc] init];
//    [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)init
{
    if (self = [super init]) {
        // 设置导航和flutter的默认导航颜色一致
        UINavigationBar.appearance.barTintColor = [UIColor colorWithRed:42.0/255 green:151.0/255 blue:240.0/255 alpha:1];
        UINavigationBar.appearance.tintColor = [UIColor whiteColor];
        UINavigationBar.appearance.titleTextAttributes = @{NSForegroundColorAttributeName: [UIColor whiteColor]};
        
        // 设置音频播放的一些默认设置
        _ocs_is_dealloc = false;
        ocsPlayers = [[NSMutableDictionary alloc] init];
        // 监听插件暂停，但是这个通知不知道在什么位置发送的
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(needStop) name:AudioplayersPluginStop object:nil];
        // 监听距离感应器的变化，用来切换听筒还是扬声器
        id observer = [[NSNotificationCenter defaultCenter] addObserverForName:UIDeviceProximityStateDidChangeNotification object:nil queue:[NSOperationQueue mainQueue] usingBlock:^(NSNotification * _Nonnull note) {
            if (![self isHeadSetPlugging]) {
                if ([[UIDevice currentDevice] proximityState]) {
                    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:nil];
                
                    for (NSString* playerId in ocsPlayers) {
                        NSMutableDictionary* playInfo = ocsPlayers[playerId];
                        if (playInfo[@"ProximityMonitor"]) {
                            [self seek:playerId time:CMTimeMake(0, 1)];
                        }
                    }
                } else {
                    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
                }
            }
        }];
        
        id observer1 = [[NSNotificationCenter defaultCenter] addObserverForName:AVAudioSessionRouteChangeNotification object:nil queue:[NSOperationQueue mainQueue] usingBlock:^(NSNotification * _Nonnull note) {
            //          AVAudioSessionRouteDescription *routeDescription=note.userInfo[AVAudioSessionRouteChangePreviousRouteKey];
            //          AVAudioSessionRouteChangeReason reason = [note.userInfo[AVAudioSessionRouteChangeReasonKey] intValue];
            //          AVAudioSessionPortDescription *portDescription= [routeDescription.outputs firstObject];
        }];
        
        if (ocs_deviceObservers == nil) {
            ocs_deviceObservers = [NSMutableSet set];
        }
        
        [ocs_deviceObservers addObject:observer];
        [ocs_deviceObservers addObject:observer1];
    }
    return self;
}

/// 判断耳机是否插入了
- (BOOL)isHeadSetPlugging {
    AVAudioSessionRouteDescription* route = [[AVAudioSession sharedInstance] currentRoute];
    for (AVAudioSessionPortDescription* desc in [route outputs]) {
        if ([[desc portType] isEqualToString:AVAudioSessionPortHeadphones])
            return YES;
    }
    return NO;
}

/// 监听到插件停止的时候destory播放器
- (void)needStop {
    _ocs_is_dealloc = true;
    [self destory];
}

/// 初始化播放信息，混存中没有则新建，然后加入缓存
-(void) initPlayerInfo: (NSString *) playerId {
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    if (!playerInfo) {
        ocsPlayers[playerId] = [@{@"isPlaying": @false, @"volume": @(1.0), @"looping": @(false)} mutableCopy];
    }
}

/**
 设置播放的URL和一些信息
 @param url 要播放的URL，可以是文件URL或者远程URL
 @param isLocal 是否是文件URL
 @param playerId 自定义的palyerId
 @param onReady 播放准备工作完成后的回调BLOC
 */
-(void) setUrl: (NSString*) url
       isLocal: (bool) isLocal
      playerId: (NSString*) playerId
proximityMonitoringEnabled:(BOOL)proximityMonitoringEnabled
       onReady:(OCSVoidCallback)onReady
{
    // 获取缓存的播放信息
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    // 获取缓存的player
    AVPlayer *player = playerInfo[@"player"];
    // 获取缓存的播放进度监听器
    NSMutableSet *observers = playerInfo[@"observers"];
    // 用来播放的item
    AVPlayerItem *playerItem;
    
    NSLog(@"setUrl %@", url);
    
    // 如果还没有播放信息，或者url不一致，重新生成playerItem，添加或者替换掉原来的item
    if (!playerInfo || ![url isEqualToString:playerInfo[@"url"]]) {
        if (isLocal) {
            playerItem = [ [ AVPlayerItem alloc ] initWithURL:[ NSURL fileURLWithPath:url ]];
        } else {
            playerItem = [ [ AVPlayerItem alloc ] initWithURL:[ NSURL URLWithString:url ]];
        }
        
        if (playerInfo[@"url"]) {
            // 移除播放状态监听
            [[player currentItem] removeObserver:self forKeyPath:@"player.currentItem.status" ];
            // 重新设置播放信息的url
            [ playerInfo setObject:url forKey:@"url" ];
            // 所有监听者从通知中心注销
            for (id ob in observers) {
                [ [ NSNotificationCenter defaultCenter ] removeObserver:ob ];
            }
            // 移除所有监听者
            [ observers removeAllObjects ];
            // 替换掉当前player的item
            [ player replaceCurrentItemWithPlayerItem: playerItem ];
        } else {
            // 创建player
            player = [[ AVPlayer alloc ] initWithPlayerItem: playerItem ];
            observers = [[NSMutableSet alloc] init];
            
            // 为播放信息添加播放器，url，和观察者缓存
            [ playerInfo setObject:player forKey:@"player" ];
            [ playerInfo setObject:url forKey:@"url" ];
            [ playerInfo setObject:observers forKey:@"observers" ];
            [ playerInfo setObject:@(proximityMonitoringEnabled) forKey:@"ProximityMonitor" ];
            
            // stream player position
            // 在每秒60帧的情况下，0.2秒的间隔帧
            CMTime interval = CMTimeMakeWithSeconds(0.2, NSEC_PER_SEC);
            // 根据间隔监听播放进度
            id timeObserver = [ player  addPeriodicTimeObserverForInterval: interval queue: nil usingBlock:^(CMTime time){
                // 监听播放时间改变的时候，回调给fluter通道
                [self onTimeInterval:playerId time:time];
            }];
            // 混存监听对象
            [ocs_timeobservers addObject:@{@"player":player, @"observer":timeObserver}];
        }
        
        // 在通知中心监听播放播放完成，完成后执行对应的回调
        id anobserver = [[ NSNotificationCenter defaultCenter ] addObserverForName: AVPlayerItemDidPlayToEndTimeNotification
                                                                            object: playerItem
                                                                             queue: nil
                                                                        usingBlock:^(NSNotification* note){
                                                                            [self onSoundComplete:playerId];
                                                                        }];
        // 将播放完成监听器加入混存
        [observers addObject:anobserver];
        
        // is sound ready
        // 设置播放信息的状态为准备完成状态
        [playerInfo setObject:onReady forKey:@"onReady"];
        // 使用key-Value监听播放状态
        [playerItem addObserver:self
                     forKeyPath:@"player.currentItem.status"
                        options:0
                        context:(void*)playerId];
        
    } else {
        // 如果缓存的player是准备好状态，则完成准备
        if ([[player currentItem] status ] == AVPlayerItemStatusReadyToPlay) {
            onReady(playerId);
        }
    }
}

/**
 播放音频
 @param playerId 播放id
 @param url 播放的URL，可以是文件URL或者普通URL
 @param isLocal 是否是文件URL
 @param volume 音量
 @param time 起始播放位置
 @param proximityMonitoringEnabled 是否根据距离感应器切换听筒和扬声器
 */
-(void) play: (NSString*) playerId
         url: (NSString*) url
     isLocal: (int) isLocal
      volume: (float) volume
        time: (CMTime) time
proximityMonitoringEnabled:(BOOL)proximityMonitoringEnabled
{
    NSError *error = nil;
    AVAudioSessionCategory category = AVAudioSessionCategoryAmbient;
    
    // 设置播放类别
    BOOL success = [[AVAudioSession sharedInstance]
                    setCategory: category
                    error:&error];
    if (!success) {
        NSLog(@"Error setting speaker: %@", error);
    }
    
    // 设置公用的播放session
    [[AVAudioSession sharedInstance] setActive:YES error:&error];
    
    // 初始化播放状态，在完成回调进行播放
    typeof(self) __weak weakSelf = self;
    [ self setUrl:url
          isLocal:isLocal
         playerId:playerId
     proximityMonitoringEnabled:proximityMonitoringEnabled
          onReady:^(NSString * playerId) {
              NSMutableDictionary * playerInfo = ocsPlayers[playerId];
              if(proximityMonitoringEnabled) {
                  [weakSelf monitorProximityMonitor];
              }
              AVPlayer *player = playerInfo[@"player"];
              [ player setVolume:volume ];
              [ player seekToTime:time ];
              [ player play];
              [ playerInfo setObject:@true forKey:@"isPlaying" ];
          }
     ];
}

/// 监听距离感应器感应器
- (void)monitorProximityMonitor {
    if (![[UIDevice currentDevice] proximityState]) {
        [[UIDevice currentDevice] setProximityMonitoringEnabled:true];
    }
}

/// 移除举例感应器监听
- (void)removeMonitorProximityMonitor {
    if ([[UIDevice currentDevice] proximityState]) {
        [[UIDevice currentDevice] setProximityMonitoringEnabled:false];
    }
}

/// 更新播放进度
-(void) updateDuration: (NSString *) playerId
{
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    AVPlayer *player = playerInfo[@"player"];
    
    CMTime duration = [[[player currentItem]  asset] duration];
    NSLog(@"ios -> updateDuration...%f", CMTimeGetSeconds(duration));
    if(CMTimeGetSeconds(duration)>0){
        NSLog(@"ios -> invokechannel");
        int mseconds= CMTimeGetSeconds(duration)*1000;
        [_ocs_channel_audioplayer invokeMethod:@"audio.onDuration" arguments:@{@"playerId": playerId, @"value": @(mseconds)}];
    }
}

/// 获取播放进度，毫秒级
-(int) getDuration: (NSString *) playerId {
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    AVPlayer *player = playerInfo[@"player"];
    
    CMTime duration = [[[player currentItem]  asset] duration];
    int mseconds= CMTimeGetSeconds(duration)*1000;
    return mseconds;
}

// No need to spam the logs with every time interval update
/// 播放进度改变的回调
-(void) onTimeInterval: (NSString *) playerId
                  time: (CMTime) time {
    // NSLog(@"ios -> onTimeInterval...");
    if (_ocs_is_dealloc) {
        return;
    }
    int mseconds =  CMTimeGetSeconds(time)*1000;
    // NSLog(@"asdff %@ - %d", playerId, mseconds);
    [_ocs_channel_audioplayer invokeMethod:@"audio.onCurrentPosition" arguments:@{@"playerId": playerId, @"value": @(mseconds)}];
    
    //    NSLog(@"asdff end");
}

/// 暂停播放
-(void) pause: (NSString *) playerId {
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    AVPlayer *player = playerInfo[@"player"];
    
    [ player pause ];
    [playerInfo setObject:@false forKey:@"isPlaying"];
}

/// 继续播放
-(void) resume: (NSString *) playerId {
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    AVPlayer *player = playerInfo[@"player"];
    [player play];
    [playerInfo setObject:@true forKey:@"isPlaying"];
}

/// 设置声音
-(void) setVolume: (float) volume
         playerId:  (NSString *) playerId {
    NSMutableDictionary *playerInfo = ocsPlayers[playerId];
    AVPlayer *player = playerInfo[@"player"];
    playerInfo[@"volume"] = @(volume);
    [ player setVolume:volume ];
}

/// 设置循环
-(void) setLooping: (bool) looping
          playerId:  (NSString *) playerId {
    NSMutableDictionary *playerInfo = ocsPlayers[playerId];
    [playerInfo setObject:@(looping) forKey:@"looping"];
}

/// 停止播放
-(void) stop: (NSString *) playerId {
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    
    if ([playerInfo[@"isPlaying"] boolValue]) {
        [ self pause:playerId ];
        [ self seek:playerId time:CMTimeMake(0, 1) ];
        [playerInfo setObject:@false forKey:@"isPlaying"];
    }
}

/// 跳转到指定时间
-(void) seek: (NSString *) playerId
        time: (CMTime) time {
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    AVPlayer *player = playerInfo[@"player"];
    [[player currentItem] seekToTime:time];
}

/// 播放完成
-(void) onSoundComplete: (NSString *) playerId {
    NSLog(@"ios -> onSoundComplete...");
    NSMutableDictionary * playerInfo = ocsPlayers[playerId];
    
    if (![playerInfo[@"isPlaying"] boolValue]) {
        return;
    }
    /// 关闭距离监听器
    if ([self _shouldDisableProximityMonitoring:playerId]) {
        [self removeMonitorProximityMonitor];
    }
    
    [ self pause:playerId ];
    [ self seek:playerId time:CMTimeMakeWithSeconds(0,1) ];
    
    if ([ playerInfo[@"looping"] boolValue]) {
        [ self resume:playerId ];
    }
    
    [ _ocs_channel_audioplayer invokeMethod:@"audio.onComplete" arguments:@{@"playerId": playerId}];
}

/// 是否应该结束监听距离感应器
- (BOOL) _shouldDisableProximityMonitoring:(NSString *) playerId {
    BOOL shouldDisable = true;
    NSMutableDictionary* playInfo = ocsPlayers[playerId];
    if (playInfo && playInfo[@"ProximityMonitor"]) {
        [playInfo removeObjectForKey:@"ProximityMonitor"];
    }
    for (NSDictionary* playInfo in ocsPlayers.allValues) {
        if (playInfo[@"ProximityMonitor"]) {
            shouldDisable = false;
        }
    }
    return shouldDisable;
}

/// 监听播放状态
-(void)observeValueForKeyPath:(NSString *)keyPath
                     ofObject:(id)object
                       change:(NSDictionary *)change
                      context:(void *)context {
    if ([keyPath isEqualToString: @"player.currentItem.status"]) {
        NSString *playerId = (__bridge NSString*)context;
        NSMutableDictionary * playerInfo = ocsPlayers[playerId];
        AVPlayer *player = playerInfo[@"player"];
        
        NSLog(@"player status: %ld",(long)[[player currentItem] status ]);
        
        // Do something with the status...
        if ([[player currentItem] status ] == AVPlayerItemStatusReadyToPlay) {
            [self updateDuration:playerId];
            
            OCSVoidCallback onReady = playerInfo[@"onReady"];
            if (onReady != nil) {
                [playerInfo removeObjectForKey:@"onReady"];
                onReady(playerId);
            }
        } else if ([[player currentItem] status ] == AVPlayerItemStatusFailed) {
            [_ocs_channel_audioplayer invokeMethod:@"audio.onError" arguments:@{@"playerId": playerId, @"value": @"AVPlayerItemStatus.failed"}];
        }
    } else {
        // Any unrecognized context must belong to super
        [super observeValueForKeyPath:keyPath
                             ofObject:object
                               change:change
                              context:context];
    }
}

- (void)destory {
    for (id value in ocs_timeobservers)
        [value[@"player"] removeTimeObserver:value[@"observer"]];
    ocs_timeobservers = nil;
    
    for (NSString* playerId in ocsPlayers) {
        NSMutableDictionary * playerInfo = ocsPlayers[playerId];
        NSMutableSet * observers = playerInfo[@"observers"];
        for (id ob in observers)
            [[NSNotificationCenter defaultCenter] removeObserver:ob];
    }
    
    for (id value in ocs_deviceObservers) {
        [[NSNotificationCenter defaultCenter] removeObserver:value];
    }
    
    ocsPlayers = nil;
}

- (void)dealloc {
    [self destory];
}


@end




