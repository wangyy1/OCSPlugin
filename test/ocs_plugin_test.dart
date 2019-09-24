import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ocs_plugin/ocs_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('ocs_plugin');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await OcsPlugin.platformVersion, '42');
  });
}
