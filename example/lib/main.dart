import 'dart:convert';

import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:ocs_plugin/ocs_plugin.dart';
import 'package:ocs_plugin/ocs_audio_player.dart';
import 'package:ocs_plugin/location_info.dart';
import 'package:ocs_plugin/ocs_message_notification.dart';

const kUrl1 = 'https://luan.xyz/files/audio/ambient_c_motion.mp3';
const kUrl2 = 'https://luan.xyz/files/audio/nasa_on_a_mission.mp3';
const kUrl3 = 'http://bbcmedia.ic.llnwd.net/stream/bbcmedia_radio1xtra_mf_p';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  double _latitude = 39.844879;
  double _longitude = 116.100201;
  String _address = '';

  String pic = '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';

  @override
  void initState() {
    super.initState();
    initPlatformState();

    // 注册百度key
    // 这里输入自己注册的百度key
    if (Platform.isIOS) {
      OcsPlugin.registerKey('1QnYI8z9TTWkwWX5iHBPrUPd').then((success) {
        print('register Baidu key: $success');
      });
    }
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await OcsPlugin.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Text('Running on: $_platformVersion\n'),
            Text('$_longitude-$_latitude-$_address'),
            RaisedButton(
              child: Text('选择位置'),
              onPressed: () async {
                LocationInfo locationInfo = await OcsPlugin.sendLocation();
                if (locationInfo != null) {
                  setState(() {
                    _latitude = locationInfo.latitude;
                    _longitude = locationInfo.longitude;
                    _address = locationInfo.address;
                  });
                }
              },
            ),
            RaisedButton(
              child: Text('查看位置'),
              onPressed: () {
                OcsPlugin.lookLocation(
                    '$_latitude', '$_longitude', '$_address');
              },
            ),
            RaisedButton(
              child: Text('播放语音'),
              onPressed: () {
                OCSAudioPlayer player = OCSAudioPlayer();
                player.play(kUrl1, proximityMonitoringEnabled: false);
              },
            ),
            RaisedButton(
              child: Text('显示一个通知+角标'),
              onPressed: () {
                OcsMessageNotification ocsMessageNotification =
                    OcsMessageNotification();
                ocsMessageNotification.initialize(
                    onSelectNotification: selectNotificationCallback);
                Future.delayed(Duration(milliseconds: 3000), () async {
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 1, payload: '哈哈', notificationId: 1,
                  largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 2, payload: '哈哈', notificationId: 1,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 3, payload: '哈哈', notificationId: 1,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 4, payload: '哈哈', notificationId: 1,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 5, payload: '哈哈', notificationId: 1,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 6, payload: '哈哈', notificationId: 1,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 7, payload: '哈哈', notificationId: 2,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 8, payload: '哈哈', notificationId: 2,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 9, payload: '哈哈', notificationId: 3,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 10, payload: '哈哈', notificationId: 3,
                      largeIcon: base64.decode(pic)
                  );
                  await ocsMessageNotification.show('ic_launcher', '测试1（1条新消息）', '消息内容',
                      count: 11, payload: '哈哈', notificationId: 1,
                      largeIcon: base64.decode(pic)
                  );
                });
              },
            ),
            RaisedButton(
              child: Text('清楚通知+角标'),
              onPressed: () {
                OcsMessageNotification ocsMessageNotification =
                    OcsMessageNotification();
                ocsMessageNotification.cancel();
              },
            ),
          ],
        ),
      ),
    );
  }

  Future<dynamic> selectNotificationCallback(String payload) async {
    print('$payload');
  }
}
