import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:ocs_plugin/ocs_plugin.dart';
import 'package:ocs_plugin/ocs_audio_player.dart';
import 'package:ocs_plugin/location_info.dart';

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


  @override
  void initState() {
    super.initState();
    initPlatformState();

    // 注册百度key
    // 这里输入自己注册的百度key
    if(Platform.isIOS) {
      OcsPlugin.registerKey('1QnYI8z9TTWkwWX5iHBPrUPd').then((success){
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
                if(locationInfo != null) {
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
                OcsPlugin.lookLocation('$_latitude', '$_longitude', '$_address');
              },
            ),
            RaisedButton(
              child: Text('播放语音'),
              onPressed: () {
                OCSAudioPlayer player = OCSAudioPlayer();
                player.play(kUrl1, proximityMonitoringEnabled: false);
              },
            ),
          ],
        ),
      ),
    );
  }
}
