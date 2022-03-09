import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef SelectNotificationCallback = Future<dynamic> Function(String payload);

class OcsMessageNotification {
  factory OcsMessageNotification() => _instance;

  OcsMessageNotification._(MethodChannel channel) : _channel = channel;

  static final OcsMessageNotification _instance =
      OcsMessageNotification._(
          const MethodChannel('ocs.notification.channel'));

  SelectNotificationCallback selectNotificationCallback;

  final MethodChannel _channel;

  Future<void> initialize(
      {SelectNotificationCallback onSelectNotification}) async {
    selectNotificationCallback = onSelectNotification;
    _channel.setMethodCallHandler(_handleMethod);
  }

  Future<void> _handleMethod(MethodCall call) {
    switch (call.method) {
      case 'selectNotification':
        return selectNotificationCallback?.call(call.arguments);
      default:
        return Future.error('method not defined');
    }
  }

  /// 显示一个通知
  /// [iconName] 通知图标
  /// [contentTitle] 通知标题
  /// [contentText] 通知内容
  /// [count] 通知数量
  /// [payload] 点击通知后传递的内容
  /// 显示一个通知
  /// [iconName] 通知图标
  /// [contentTitle] 通知标题
  /// [contentText] 通知内容
  /// [count] 单个通知数字
  /// [sumCount] api26以下使用，显示角标数字，api26以上使用count数字（如果有两个通知，则显示两个通知count的和）
  /// [payload] 点击通知后传递的内容
  Future<void> show(String iconName, String contentTitle, String contentText, String className,
      {int count = 0, int sumCount = 0, String payload = '', int notificationId = 0, Uint8List largeIcon}) async {
    await _channel.invokeMethod('show', <String, dynamic>{
      'notificationId': notificationId,
      'count': count,
      'sumCount': sumCount,
      'iconName': iconName,
      'contentTitle': contentTitle,
      'contentText': contentText,
      'payload': payload,
      'largeIcon': largeIcon,
      'className': className,
    });
  }

  /// 清空所有通知
  Future<void> cancel() async {
    await _channel.invokeMethod('cancel');
  }
}
