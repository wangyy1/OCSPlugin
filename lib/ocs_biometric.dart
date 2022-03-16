import 'package:flutter/services.dart';

typedef SelectNotificationCallback = Future<dynamic> Function(String payload);

enum BiometricResult{
  SUCCESS, /// 生物认证可用
  HW_UNAVAILABLE, /// 硬件不可用，稍后再试
  NONE_ENROLLED, /// 用户没有注册任何生物识别系统
  NO_HARDWARE /// 没有生物识别硬件
}

class AuthenticateResult {
  bool result;
  String msg;
}


class OcsBiometric {
  static final MethodChannel _channel =
      const MethodChannel('ocs.biometric.channel')
        ..setMethodCallHandler(platformCallHandler);

  /// 是否支持生物识别
  static Future<BiometricResult> canAuthenticate() async {
    return BiometricResult.values[await _channel.invokeMethod('canAuthenticate')];
  }

  /// 进行身份认证
  static Future<AuthenticateResult> authenticate() async {
    Map<dynamic, dynamic> result = await _channel.invokeMapMethod('authenticate');
    AuthenticateResult authenticateResult = new AuthenticateResult();
    authenticateResult.result = result['result'];
    authenticateResult.msg = result['msg'];
    return authenticateResult;
  }

  static Future<void> platformCallHandler(MethodCall call) async {
    try {
     // _doHandlePlatformCall(call);
    } catch (ex) {}
  }

}
