import 'package:flutter/services.dart';

enum SystemAppJump {
  /// 邮件
  EMAIL,

  /// 图库
  GALLERY,

  /// 拨号
  CALL,

  /// 联系人
  CONTACTS,

  /// 短信
  SMS,

  /// 相机
  CAMERA,
}

class AppJump {
  static final MethodChannel _channel =
      const MethodChannel('ocs.appJump.channel')
        ..setMethodCallHandler(platformCallHandler);

  /// [identify]iOS对应的是URLScheme，Android对应的是包名
  static Future jumpToApp(String identify) async {
    return await _channel.invokeMethod('appJump.jump', {'identify': identify});
  }

  /// 使用WPS打开文件
  /// 目前只实现了android功能
  static Future<bool> openFileByWPS(String filePath) async {
    return await _channel.invokeMethod('Thirdparty.WPS.open.file', {'file_path': filePath});
  }

  List<String> systemAppType = ['', '', '', '', '', '', '', '', ''];

  /// 跳转系统应用
  static  Future<bool> jumpToSystemApp(SystemAppJump systemAppJump) async {
    return await _channel.invokeMethod(systemAppJump.toString());
  }

  static Future<void> platformCallHandler(MethodCall call) async {
    try {
//      _doHandlePlatformCall(call);
    } catch (ex) {}
  }
}
