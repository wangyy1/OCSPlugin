//
//  ViewController.m
//  OCS地图定位
//
//  Created by jzxl on 15/11/17.
//  Copyright (c) 2015年 jzxl. All rights reserved.
//

#import "WLLocationViewController.h"

#import <BaiduMapAPI_Search/BMKSearchComponent.h>//引入检索功能所有的头文件

#import <BaiduMapAPI_Location/BMKLocationComponent.h>//引入定位功能所有的头文件

#import <BaiduMapAPI_Map/BMKMapView.h>//只引入所需的单个头文件

#import "WLUtils.h"


@interface WLLocationViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate,BMKGeoCodeSearchDelegate,UITableViewDataSource,UITableViewDelegate>

@property (nonatomic, strong) BMKMapView* mapView;
@property (nonatomic, strong) BMKLocationService *locService;
@property (nonatomic, strong) BMKGeoCodeSearch *search;
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) NSMutableArray *nameArray;
@property (nonatomic, strong) NSMutableArray *addressArray;
@property (nonatomic, strong) UIImageView *imgView;
@property (nonatomic, strong) UIBarButtonItem *rightButtonItem;
@property (nonatomic, strong) UIButton *myposition;
@property (nonatomic, strong) BMKUserLocation *myLocation;
@property (nonatomic, strong) NSMutableArray *infoArray;

@end

@implementation WLLocationViewController

#pragma mark - Life cycle
- (void)viewDidLoad
{
    [super viewDidLoad];

    self.title = @"选择位置";
    
    [self _addSubViews];
    [self _addAutoLayout];

    ///geo搜索服务
    _search = [[BMKGeoCodeSearch alloc] init];
    _search.delegate = self;

    _locService = [[BMKLocationService alloc] init];
    _locService.delegate = self;
    [_locService startUserLocationService]; //打开定位服务
    _mapView.showsUserLocation = YES;  //设定是否显示定位图层

    self.navigationItem.rightBarButtonItem = self.rightButtonItem;
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"返回" style:UIBarButtonItemStylePlain target:self action:@selector(goBack)];}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:YES];
    _tableView.delegate = nil;
    _tableView.dataSource = nil;
    _mapView.delegate = nil;
    _search.delegate = nil;
}

#pragma mark - UI

- (void)_addSubViews {
    [self.view addSubview:self.mapView];
    [self.mapView addSubview:self.imgView];
    [self.mapView addSubview:self.myposition];
    [self.view addSubview:self.tableView];
}

- (void) _addAutoLayout {
    self.mapView.translatesAutoresizingMaskIntoConstraints = false;
    self.imgView.translatesAutoresizingMaskIntoConstraints = false;
    self.myposition.translatesAutoresizingMaskIntoConstraints = false;
    self.tableView.translatesAutoresizingMaskIntoConstraints = false;
    
    NSDictionary*bindings = NSDictionaryOfVariableBindings(_mapView,_imgView, _mapView, _tableView);
    NSString* format_1 = @"H:|-0-[_mapView]-0-|";
    NSString* format_2 = @"V:|-0-[_mapView(200)]-0-[_tableView]-0-|";
    NSString* format_3 = @"H:|-0-[_tableView]-0-|";
    
    NSArray* constraints1 = [NSLayoutConstraint constraintsWithVisualFormat:format_1 options:0 metrics:nil views:bindings];
    NSArray* constraints3 = [NSLayoutConstraint constraintsWithVisualFormat:format_3 options:0 metrics:nil views:bindings];
    NSArray* constraints2 = [NSLayoutConstraint constraintsWithVisualFormat:format_2 options:0 metrics:nil views:bindings];

    
    [self.view addConstraints:constraints1];
    [self.view addConstraints:constraints2];
    [self.view addConstraints:constraints3];

    [self.mapView addConstraint:[NSLayoutConstraint constraintWithItem:_imgView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:_mapView attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0]];
    [self.mapView addConstraint:[NSLayoutConstraint constraintWithItem:_imgView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:_mapView attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:-18]];
    [self.mapView addConstraint:[NSLayoutConstraint constraintWithItem:_myposition attribute:NSLayoutAttributeTrailing relatedBy:NSLayoutRelationEqual toItem:_mapView attribute:NSLayoutAttributeTrailing multiplier:1.0 constant:-8]];
    [self.mapView addConstraint:[NSLayoutConstraint constraintWithItem:_myposition attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:_mapView attribute:NSLayoutAttributeBottom multiplier:1.0 constant:-8]];
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


#pragma mark - UITableViewDelegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (self.infoArray.count > 0)
    {
        BMKPoiInfo *info = [self.infoArray objectAtIndex:indexPath.row];

        CLLocationCoordinate2D coordinate2D = info.pt;

        double latitude = coordinate2D.latitude;
        NSString *latitudeStr = [NSString stringWithFormat:@"%f",latitude];
        double longitude = coordinate2D.longitude;
        NSString *longitudeStr = [NSString stringWithFormat:@"%f",longitude];

        if ([self.delegate respondsToSelector:@selector(wlLocationDidSelectedLocationWithLititude:longitude:address:)]) {

            [self.delegate wlLocationDidSelectedLocationWithLititude:latitudeStr longitude:longitudeStr address:info.name];
        }
        
        if (self.callBack) {
            self.callBack(latitudeStr, longitudeStr, info.name);
        }
        [self dismissViewControllerAnimated:YES completion:^{
            
        }];
    }
}
#pragma mark -  UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (self.addressArray.count > 0)
    {
        return self.addressArray.count;
    }
    else
    {
        return 0;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell"];

    if (!cell)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:@"cell"];
    }
    if (_addressArray.count > 0)
    {
        cell.detailTextLabel.text = _addressArray[indexPath.row];
    }
    if (_nameArray.count > 0)
    {
        NSString *textLabelStr = _nameArray[indexPath.row];
        if (indexPath.row == 0)
        {
            cell.textLabel.text = [NSString stringWithFormat:@"[当前]%@",textLabelStr];
            cell.textLabel.textColor = [UIColor redColor];
        }
        else
        {
            cell.textLabel.text = textLabelStr;
            cell.textLabel.textColor = [UIColor blackColor];
        }
    }

    return cell;
}

#pragma mark - Event Response
-(void)goBack {
    [self dismissViewControllerAnimated:YES completion:^{
        if (self.callBack) {
            self.callBack(nil, nil, nil);
        }
    }];
}

-(void)rightButtonItemClickBackCall
{
    double latitude = _mapView.centerCoordinate.latitude;
    NSString *latitudeStr = [NSString stringWithFormat:@"%f",latitude];
    double longitude = _mapView.centerCoordinate.longitude;
    NSString *longitudeStr = [NSString stringWithFormat:@"%f",longitude];

    if (_nameArray.count > 0)
    {
        if ([self.delegate respondsToSelector:@selector(wlLocationDidSelectedLocationWithLititude:longitude:address:)]) {

            [self.delegate wlLocationDidSelectedLocationWithLititude:latitudeStr longitude:longitudeStr address:[_nameArray firstObject]];
        }
        
        if (self.callBack) {
            self.callBack(latitudeStr, longitudeStr, [_nameArray firstObject]);
        }

//        [self.navigationController popViewControllerAnimated:YES];
        [self dismissViewControllerAnimated:YES completion:^{
            
        }];
    }
}

-(void)backChatView
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void)mypositionAction
{
    [_mapView setCenterCoordinate:_myLocation.location.coordinate animated:YES];
}

#pragma mark - BMKMapViewDelegate
- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    BMKReverseGeoCodeSearchOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeSearchOption alloc]init];
    reverseGeocodeSearchOption.location =  mapView.region.center;
    BOOL flag = [_search reverseGeoCode:reverseGeocodeSearchOption];
    if(flag) NSLog(@"地图区域改变完成反geo检索发送成功");
}
#pragma mark - BMKLocationServiceDelegate定位服务
//用户方向更新后，会调用此函数
- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation
{
    [_mapView updateLocationData:userLocation];
    _mapView.showsUserLocation = YES;
}

//处理位置坐标更新
- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation
{
    NSLog(@"didUpdateUserLocation lat %f,long %f",userLocation.location.coordinate.latitude,userLocation.location.coordinate.longitude);
    if (!_myLocation)
    {
        _myLocation = userLocation;
    }
    _mapView.centerCoordinate = userLocation.location.coordinate;
    [_mapView updateLocationData:userLocation];
    _mapView.showsUserLocation = YES;
    CLLocationDegrees latitude = userLocation.location.coordinate.latitude;
    CLLocationDegrees longitude = userLocation.location.coordinate.longitude;
    CLLocationCoordinate2D pt = (CLLocationCoordinate2D){latitude,longitude};

    BMKReverseGeoCodeSearchOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeSearchOption alloc]init];
    reverseGeocodeSearchOption.location = pt;
    BOOL flag = [_search reverseGeoCode:reverseGeocodeSearchOption];
    if(flag) NSLog(@"反geo检索发送成功");
}

#pragma mark - BMKGeoCodeSearchDelegate搜索，用于获取搜索结果
/**
 *返回反地理编码搜索结果
 *@param searcher 搜索对象
 *@param result 搜索结果
 *@param error 错误号，@see BMKSearchErrorCode
 */
- (void)onGetGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKGeoCodeSearchResult *)result errorCode:(BMKSearchErrorCode)error
{
    NSLog(@"%@",result);

    if (self.nameArray.count > 0)
    {
        [self.nameArray removeAllObjects];
    }
    if (_addressArray.count > 0)
    {
        [self.addressArray removeAllObjects];
    }
    NSArray *poiArray = [result valueForKey:@"poiList"];//result.poiList;
    for (BMKPoiInfo *info in poiArray)
    {
        NSString *name = info.name;
        NSString *address = info.address;

        [self.nameArray addObject:name];
        [self.addressArray addObject:address];
        [self.infoArray addObject:info];
    }

    [_tableView reloadData];
}

- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeSearchResult *)result errorCode:(BMKSearchErrorCode)error
{
    NSLog(@"%@",result);

    if (self.nameArray.count > 0)
    {
        [self.nameArray removeAllObjects];
    }
    if (_addressArray.count > 0)
    {
        [self.addressArray removeAllObjects];
    }
    NSArray *poiArray = [result valueForKey:@"poiList"];//result.poiList;
    for (BMKPoiInfo *info in poiArray)
    {
        NSString *name = info.name;
        NSString *address = info.address;

        [self.nameArray addObject:name];
        [self.addressArray addObject:address];
        [self.infoArray addObject:info];
    }

    [_tableView reloadData];
}

#pragma mark - Propertys
-(NSMutableArray *)addressArray
{
    if (!_addressArray)
    {
        self.addressArray = [NSMutableArray array];
    }
    return _addressArray;
}

-(NSMutableArray *)nameArray
{
    if (!_nameArray)
    {
        self.nameArray = [NSMutableArray array];
    }
    return _nameArray;
}

-(BMKMapView *)mapView
{
    if (!_mapView)
    {
        _mapView = [[BMKMapView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 200)];
        _mapView.delegate = self;
        _mapView.rotateEnabled = NO;
        _mapView.zoomLevel = 16;
    }
    return _mapView;
}

-(UITableView *)tableView
{
    if (!_tableView)
    {
        //创建tableView
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, CGRectGetMaxY(_mapView.frame), MainScreenWidth, MainScreenHeight-CGRectGetHeight(_mapView.frame) - kStatusBarAndNavigationBarHeight) style:UITableViewStylePlain];
        if (IS_iPhoneX) {
            CGRect frame = _tableView.frame;
            _tableView.frame = CGRectMake(frame.origin.x, frame.origin.y, frame.size.width, frame.size.height - kTabbarSafeBottomMargin);
        }
        _tableView.delegate = self;
        _tableView.dataSource = self;
    }
    return _tableView;
}

-(UIImageView *)imgView
{
    if (!_imgView)
    {
//        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:[WLUtils getFixedImageName:@"ocs_location_red"]]];
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ocs_location_red"]];

        _imgView.frame = CGRectMake(CGRectGetMidX(_mapView.frame)-18, CGRectGetMidY(_mapView.frame)-36, 36, 36);
    }
    return _imgView;
}

-(UIBarButtonItem *)rightButtonItem
{
    if (!_rightButtonItem)
    {
        _rightButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"发送" style:UIBarButtonItemStylePlain target:self action:@selector(rightButtonItemClickBackCall)];

    }
    return _rightButtonItem;
}

-(UIButton *)myposition
{
    if (!_myposition)
    {
        _myposition = [UIButton buttonWithType:UIButtonTypeCustom];
        [_myposition setImage:[UIImage imageNamed:@"ocs_my_location_dark_gray"] forState:UIControlStateNormal];
        _myposition.frame = CGRectMake(15, 200-15-40, 40, 40);
        [_myposition addTarget:self action:@selector(mypositionAction) forControlEvents:UIControlEventTouchUpInside];
    }
    return _myposition;
}

-(NSMutableArray *)infoArray
{
    if (!_infoArray)
    {
        self.infoArray = [NSMutableArray array];
    }
    return _infoArray;
}

@end
