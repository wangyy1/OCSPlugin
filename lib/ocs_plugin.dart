import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'location_info.dart';

class OcsPlugin {
  static const MethodChannel _channel = const MethodChannel('ocs_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// 注册百度的key
  /// iOS需要注册，android在AndroidManifest.xml配置
  static Future<bool> registerKey(String key) async {
    assert(Platform.isIOS, 'android在AndroidManifest.xml配置，不需要调用这个方法');
    if (!Platform.isIOS) return false;
    final bool result = await _channel.invokeMethod('registerKey', key);
    return result;
  }

  /// 发送位置/选择位置
  /// [baiDuKey] IOS传递百度Key
  static Future<LocationInfo> sendLocation() async {
    final Map map = await _channel.invokeMethod('sendLocation');
    if (map == null) {
      return null;
    }
    LocationInfo locationInfo = LocationInfo.map(map);
    return locationInfo;
  }

  /// 查看位置
  /// [latitude] 纬度
  /// [longitude] 经度
  /// [address] 位置信息
  static Future<Null> lookLocation(
      String latitude, String longitude, String address) async {
    await _channel.invokeMethod('lookLocation', <String, dynamic>{
      'latitude': latitude,
      'longitude': longitude,
      'address': address,
    });
    return null;
  }
}
