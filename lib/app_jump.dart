
import 'package:flutter/services.dart';

class AppJump {
  static final MethodChannel _channel =
  const MethodChannel('ocs.appJump.channel')
    ..setMethodCallHandler(platformCallHandler);

  /// [identify]iOS对应的是URLScheme，Android对应的是包名
  static Future jumpToApp(String identify) async {
    return await _channel.invokeMethod('appJump.jump', {'identify' : identify});
  }

  static Future<void> platformCallHandler(MethodCall call) async {
    try {
//      _doHandlePlatformCall(call);
    } catch (ex) {
    }
  }
}