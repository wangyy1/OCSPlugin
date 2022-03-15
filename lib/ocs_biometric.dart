import 'package:flutter/services.dart';

typedef SelectNotificationCallback = Future<dynamic> Function(String payload);

enum AuthenticateResult {
  UNAVAILABLE, /// 设备不可用
  NONE_ENROLLED, /// 未添加，指纹或人脸
  SUCCEEDED, /// 认证成功
  ERROR, /// 认证出现异常
}



class OcsBiometric {
  static final MethodChannel _channel =
      const MethodChannel('ocs.biometric.channel')
        ..setMethodCallHandler(platformCallHandler);

  /// 是否支持生物识别
  static Future<bool> canAuthenticate() async {
    return await _channel.invokeMethod('canAuthenticate');
  }

  /// 进行身份认证
  static Future<AuthenticateResult> authenticate() async {
    Map<dynamic, dynamic> result = await _channel.invokeMapMethod('authenticate');
    switch(result['RESULT']) {
      case 'UNAVAILABLE':
        return AuthenticateResult.UNAVAILABLE;
        break;
      case 'NONE_ENROLLED':
        return AuthenticateResult.NONE_ENROLLED;
        break;
      case 'SUCCEEDED':
        return AuthenticateResult.SUCCEEDED;
        break;
      case 'ERROR':
        return AuthenticateResult.ERROR;
        break;
    }
    return AuthenticateResult.ERROR;
  }

  static Future<void> platformCallHandler(MethodCall call) async {
    try {
     // _doHandlePlatformCall(call);
    } catch (ex) {}
  }

}
