package com.jzxl.ocs_plugin.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.jzxl.ocs_plugin.R;
import com.jzxl.ocs_plugin.base.BaseActivity;
import com.jzxl.ocs_plugin.base.adapter.LocationAdapter;
import com.jzxl.ocs_plugin.entity.LocationInfo;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.mapapi.search.core.SearchResult.ERRORNO.PERMISSION_UNFINISHED;
import static com.baidu.mapapi.search.core.SearchResult.ERRORNO.RESULT_NOT_FOUND;

public class BaiDuMapLocationActivity extends BaseActivity {
    private static final String TAG = "BaiDuMapLocationActivit";
    private static final String EXTRA_SELECTED_LOCATION = "EXTRA_SELECTED_LOCATION";
    public static final String SHOW_UI_TYPE = "SHOW_UI_TYPE";
    private static final String LOCATION_INFO = "LOCATION_INFO";

    public static final int SEND_MAP_MESSAGE_TYPE_VALUE = 1;
    private static final int LOOK_MAP_MESSAGE_SHOW_UI_TYPE_VALUE = 2;

    MapView mBaiduMapView;
    ListView mAddressList;
    ImageView mAnewLocation;
    ImageView mCenterImg;

    private BaiduMap mBaiduMap;

    private LocationClient mLocationClient;
    // 第一次定位
    private boolean mIsFirstLoc = true;

    private boolean mISTouchMap;
    // 检索
    private GeoCoder mGeoCoder;
    // 位置集合
    private List<LocationInfo> mLocationInfos;
    // 适配器
    private LocationAdapter mLocationAdapter;
    // 选中的集合下标
    private int currentSelectIndex;

    // 界面显示标记
    private int mShowUiType;

    private BitmapDescriptor mIconMaker;

    private LocationInfo mLocationInfo;

    private LatLng currentLatLng;// 当前位置信息

    /**
     * 获取当前
     *
     * @param intent
     * @return
     */
    public static LocationInfo getSelectedLocation(Intent intent) {
        return intent.getParcelableExtra(EXTRA_SELECTED_LOCATION);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mShowUiType = getIntent().getIntExtra(SHOW_UI_TYPE, 0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });
    }

    /**
     * 点击返回
     */
    private void back() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();//此处一定要调用finish()方法
    }

    @Override
    protected void initView() {
        super.initView();
        View view = View.inflate(this, R.layout.activity_baidumap_location, null);
        mBaiduMapView = view.findViewById(R.id.bmapView);
        mAddressList = view.findViewById(R.id.address_list);
        mAnewLocation = view.findViewById(R.id.anew_location);
        mCenterImg = view.findViewById(R.id.center_img);
        baseContent.addView(view);
        if (mShowUiType == 1) {
            toolbar.setTitle(getResources().getString(R.string.location));
//            mRightText.setText(getResources().getString(R.string.send));
//            mRightText.setVisibility(View.VISIBLE);
//            mRightText.setTextColor(Color.WHITE);
            mCenterImg.setVisibility(View.VISIBLE);
            mAddressList.setVisibility(View.VISIBLE);
        } else if (mShowUiType == 2) {
            mLocationInfo = getIntent().getParcelableExtra(LOCATION_INFO);
            toolbar.setTitle(mLocationInfo.getAddress());
//            mRightText.setVisibility(View.GONE);
            mCenterImg.setVisibility(View.GONE);
            mAddressList.setVisibility(View.GONE);
        }
    }


    @Override
    protected void initData() {
        super.initData();
        mIconMaker = BitmapDescriptorFactory.fromResource(R.drawable.location_overlay_icon);
        mBaiduMap = mBaiduMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mGeoCoder = GeoCoder.newInstance();
        mLocationInfos = new ArrayList<>();
        mLocationAdapter = new LocationAdapter(mLocationInfos);
        mAddressList.setAdapter(mLocationAdapter);

        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
//        showProgressDialog(getResources().getString(R.string.in_the_location));
        if (mShowUiType == 2) {
            addInfosOverlay(mLocationInfo);
        }
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                Log.e(TAG, "onTouch: " + motionEvent.getAction());
                mISTouchMap = true;
            }
        });

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                Log.e(TAG, "onMapStatusChangeStart: " + mapStatus);
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
                Log.e(TAG, "onMapStatusChangeStart: " + mapStatus);
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                Log.e(TAG, "onMapStatusChange: " + mapStatus);
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                Log.e(TAG, "onMapStatusChangeFinish: " + mapStatus);
                if (mISTouchMap) {
                    currentSelectIndex = 0;
                    if (mShowUiType == 1) {
                        mAddressList.setSelection(0);
                        currentLatLng = mapStatus.target;
                        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mapStatus.target));
                    }
                }
            }
        });

        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                Log.e(TAG, "onGetGeoCodeResult: ");
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult.error == RESULT_NOT_FOUND && currentLatLng != null) {
                    mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(currentLatLng));
                    return;
                }
                if (reverseGeoCodeResult.error == PERMISSION_UNFINISHED) {
                    Toast.makeText(mContext, getResources().getString(R.string.baidu_sdk_erro), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<LocationInfo> locationInfos = new ArrayList<>();
                LocationInfo locationInfo;
                for (int i = 0; i < reverseGeoCodeResult.getPoiList().size(); i++) {
                    if (i == 0) {
                        locationInfo = new LocationInfo(reverseGeoCodeResult.getPoiList().get(i).location.latitude, reverseGeoCodeResult.getPoiList().get(i).location.longitude, reverseGeoCodeResult.getPoiList().get(i).name, reverseGeoCodeResult.getPoiList().get(i).address);
                        locationInfo.setSelected(true);
                    } else {
                        locationInfo = new LocationInfo(reverseGeoCodeResult.getPoiList().get(i).location.latitude, reverseGeoCodeResult.getPoiList().get(i).location.longitude, reverseGeoCodeResult.getPoiList().get(i).name, reverseGeoCodeResult.getPoiList().get(i).address);
                        locationInfo.setSelected(false);
                    }
                    locationInfos.add(locationInfo);
                }
                mLocationAdapter.update(locationInfos);
            }
        });

//        mRightText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mLocationInfos != null && mLocationInfos.size() > 0) {
//                    Intent intent = new Intent();
//                    intent.putExtra(EXTRA_SELECTED_LOCATION, mLocationInfos.get(currentSelectIndex));
//                    setResult(RESULT_OK, intent);
//                    //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
//                    finish();//此处一定要调用finish()方法
//                }
//            }
//        });

        mAnewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddressList.setSelection(0);
                currentSelectIndex = 0;
                mISTouchMap = true;
                BDLocation bdLocation = mLocationClient.getLastKnownLocation();
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        .latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);

                LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                currentLatLng = latLng;
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
            }
        });

        mAddressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mISTouchMap = false;
                mLocationInfos.get(currentSelectIndex).setSelected(false); // 上一次选择还原
                mLocationInfos.get(position).setSelected(true); // 现在选中的
                mLocationAdapter.notifyDataSetChanged();
                currentSelectIndex = position;
                LatLng latLng = new LatLng(mLocationInfos.get(position).getLatitude(), mLocationInfos.get(position).getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mBaiduMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBaiduMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGeoCoder != null) {
            mGeoCoder.destroy();
        }
        mBaiduMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            List<Poi> poiList = bdLocation.getPoiList();
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
//            closeProgressDialog();
            if (mIsFirstLoc) {
                if (mShowUiType == 1) {
                    mIsFirstLoc = false;
                    LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(latLng).zoom(18f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    currentLatLng = latLng;
                    mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                }
            }
        }

        @Override
        public void onLocDiagnosticMessage(int i, int i1, String s) {
            super.onLocDiagnosticMessage(i, i1, s);
        }
    }

    /**
     * 添加附加物
     *
     * @param locationInfo
     */
    private void addInfosOverlay(LocationInfo locationInfo) {
        if (locationInfo == null) {
            return;
        }
        mBaiduMap.clear();
        // 位置
        LatLng latLng = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());
        // 图标
        OverlayOptions overlayOptions = new MarkerOptions().position(latLng).icon(mIconMaker).zIndex(5);
        mBaiduMap.addOverlay(overlayOptions);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLng).zoom(18f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    /**
     * 获取发送地图Intent
     *
     * @param context
     * @return
     */
    public static Intent getSendMapIntent(Context context) {
        Intent intent = new Intent(context, BaiDuMapLocationActivity.class);
        intent.putExtra(BaiDuMapLocationActivity.SHOW_UI_TYPE, BaiDuMapLocationActivity.SEND_MAP_MESSAGE_TYPE_VALUE);
        return intent;
    }

    /**
     * 获取查看地图Intent
     *
     * @param context
     * @param locationInfo 位置信息
     * @return
     */
    public static Intent getLookMapIntent(Context context, LocationInfo locationInfo) {
        Intent intent = new Intent(context, BaiDuMapLocationActivity.class);
        intent.putExtra(BaiDuMapLocationActivity.SHOW_UI_TYPE, BaiDuMapLocationActivity.LOOK_MAP_MESSAGE_SHOW_UI_TYPE_VALUE);
        intent.putExtra(BaiDuMapLocationActivity.LOCATION_INFO, locationInfo);
        return intent;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location_send_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mShowUiType == 1) {
            menu.findItem(R.id.action_send).setVisible(true);
        } else if (mShowUiType == 2) {
            menu.findItem(R.id.action_send).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            if (mLocationInfos != null && mLocationInfos.size() > 0) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECTED_LOCATION, mLocationInfos.get(currentSelectIndex));
                setResult(RESULT_OK, intent);
                //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
                finish();//此处一定要调用finish()方法
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
