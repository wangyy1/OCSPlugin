import 'dart:async';

import 'package:flutter/services.dart';

import 'location_info.dart';

class OcsPlugin {
  static const MethodChannel _channel = const MethodChannel('ocs_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// 发送位置/选择位置
  /// [baiDuKey] IOS传递百度Key
  static Future<LocationInfo> sendLocation({String baiDuKey}) async {
    final Map map =
        await _channel.invokeMethod('sendLocation', <String, dynamic>{
      'baiDuKey': baiDuKey,
    });
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
