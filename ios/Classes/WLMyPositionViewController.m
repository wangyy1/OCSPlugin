//
//  WLMyPositionViewController.m
//  OOC
//
//  Created by jzxl on 15/12/25.
//  Copyright (c) 2015年 lazy. All rights reserved.
//

#import "WLMyPositionViewController.h"

#import <BaiduMapAPI_Search/BMKSearchComponent.h>//引入检索功能所有的头文件

#import <BaiduMapAPI_Search/BMKGeocodeSearchOption.h>

#import <BaiduMapAPI_Location/BMKLocationComponent.h>//引入定位功能所有的头文件

#import <BaiduMapAPI_Map/BMKMapView.h>//只引入所需的单个头文件

#import <BaiduMapAPI_Map/BMKPointAnnotation.h>

@interface WLMyPositionViewController ()<BMKMapViewDelegate,BMKGeoCodeSearchDelegate,BMKLocationServiceDelegate>

@property (nonatomic, strong) BMKMapView* mapView;
@property (nonatomic, strong) BMKGeoCodeSearch *search;
@property (nonatomic, strong) BMKLocationService *locService;

@end

@implementation WLMyPositionViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = @"位置";
    
    [self.view addSubview:self.mapView];
    
    // 增加自动布局
    [self _addLayout];
    
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"返回" style:UIBarButtonItemStylePlain target:self action:@selector(goBack)];
    
    _locService = [[BMKLocationService alloc] init];
    _locService.delegate = self;
    [_locService startUserLocationService]; //打开定位服务
    
    [_mapView setShowsUserLocation:YES];  //设定是否显示定位图层
    
    CLLocationCoordinate2D myPosition = CLLocationCoordinate2DMake(self.latitude.doubleValue, self.longitude.doubleValue);
    [_mapView setCenterCoordinate:myPosition animated:YES];
    
    BMKLocationViewDisplayParam* param = [[BMKLocationViewDisplayParam alloc] init];
    param.locationViewOffsetY = 0;//偏移量
    param.locationViewOffsetX = 0;
    param.isAccuracyCircleShow =YES;//设置是否显示定位的那个精度圈
    param.isRotateAngleValid = YES;
    [_mapView updateLocationViewWithParam:param];
    
    //BMKAnnotationView *annotationView = [[BMKAnnotationView alloc] init];
    
    BMKPointAnnotation *annotation = [[BMKPointAnnotation alloc] init];
    annotation.coordinate = myPosition;
    [_mapView addAnnotation:annotation];
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:YES];
    _mapView.delegate = nil;
    _search.delegate = nil;
}

#pragma - layout
// 使用自动布局
- (void)_addLayout {
    self.mapView.translatesAutoresizingMaskIntoConstraints = false;
    
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-0-[_mapView]-0-|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(_mapView)]];
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-0-[_mapView]-0-|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(_mapView)]];
}

#pragma - Actions

-(void)goBack {
    [self dismissViewControllerAnimated:YES completion:^{
        if (self.backBlock) {
            self.backBlock();
        }
    }];
}

#pragma mark - Orientations
- (BOOL)shouldAutorotate{
    return NO;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationPortrait;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

#pragma mark - BMKMapViewDelegate
-(BMKAnnotationView *)mapView:(BMKMapView *)mapView viewForAnnotation:(id<BMKAnnotation>)annotation
{
    
    return nil;
}
#pragma mark - BMKLocationServiceDelegate定位服务
//用户方向更新后，会调用此函数
- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation
{
    [_mapView updateLocationData:nil];
    
}
#pragma mark - Propertys
-(BMKMapView *)mapView
{
    if (!_mapView)
    {
        self.mapView = [[BMKMapView alloc]initWithFrame:self.view.bounds];
        _mapView.delegate = self;
        _mapView.rotateEnabled = NO;       //设置是否可以旋转
        _mapView.zoomLevel = 18;            //地图比例尺级别，在手机上当前可使用的级别为3-20级
        
    }
    return _mapView;
}

@end
