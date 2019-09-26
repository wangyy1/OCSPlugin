import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';
import 'location_info.dart';

typedef StreamController CreateStreamController();
typedef void TimeChangeHandler(Duration duration);
typedef void ErrorHandler(String message);
typedef void AudioPlayerStateChangeHandler(AudioPlayerState state);

/// This enum is meant to be used as a parameter of [setReleaseMode] method.
///
/// It represents the behaviour of [OCSAudioPlayer] when an audio is finished or
/// stopped.
enum ReleaseMode {
  /// Releases all resources, just like calling [release] method.
  ///
  /// In Android, the media player is quite resource-intensive, and this will
  /// let it go. Data will be buffered again when needed (if it's a remote file,
  /// it will be downloaded again).
  /// In iOS, works just like [stop] method.
  ///
  /// This is the default behaviour.
  RELEASE,

  /// Keeps buffered data and plays again after completion, creating a loop.
  /// Notice that calling [stop] method is not enough to release the resources
  /// when this mode is being used.
  LOOP,

  /// Stops audio playback but keep all resources intact.
  /// Use this if you intend to play again later.
  STOP
}

/// Self explanatory. Indicates the state of the audio player.
enum AudioPlayerState {
  STOPPED,
  PLAYING,
  PAUSED,
  COMPLETED,
}


class OcsPlugin {
  static const MethodChannel _channel = const MethodChannel('ocs_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// 注册百度的key
  /// iOS需要注册，android在AndroidManifest.xml配置
  static Future<bool>  registerKey(String key) async {
    assert(Platform.isIOS, 'android在AndroidManifest.xml配置，不需要调用这个方法');
    if(!Platform.isIOS) return false;
    final bool result = await _channel.invokeMethod('registerKey', key);
    return result;
  }

  /// 发送位置/选择位置
  /// [baiDuKey] IOS传递百度Key
  static Future<LocationInfo> sendLocation() async {
    final Map map =
    await _channel.invokeMethod('sendLocation');
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


class OCSAudioPlayer {
  static final MethodChannel _channel =
  const MethodChannel('ocs.audioPlayer.channel')
    ..setMethodCallHandler(platformCallHandler);

  static final _uuid = Uuid();

  final StreamController<AudioPlayerState> _playerStateController =
  StreamController<AudioPlayerState>.broadcast();

  final StreamController<Duration> _positionController =
  StreamController<Duration>.broadcast();

  final StreamController<Duration> _durationController =
  StreamController<Duration>.broadcast();

  final StreamController<void> _completionController =
  StreamController<void>.broadcast();

  final StreamController<String> _errorController =
  StreamController<String>.broadcast();

  /// Reference [Map] with all the players created by the application.
  ///
  /// This is used to exchange messages with the [MethodChannel]
  /// (there is only one).
  static final players = Map<String, OCSAudioPlayer>();

  /// Enables more verbose logging.
  static bool logEnabled = false;

  AudioPlayerState _audioPlayerState;

  AudioPlayerState get state => _audioPlayerState;

  set state(AudioPlayerState state) {
    _playerStateController.add(state);
    // ignore: deprecated_member_use_from_same_package
    audioPlayerStateChangeHandler?.call(state);
    _audioPlayerState = state;
  }

  /// Stream of changes on player state.
  Stream<AudioPlayerState> get onPlayerStateChanged =>
      _playerStateController.stream;

  /// Stream of changes on audio position.
  ///
  /// Roughly fires every 200 milliseconds. Will continuously update the
  /// position of the playback if the status is [AudioPlayerState.PLAYING].
  ///
  /// You can use it on a progress bar, for instance.
  Stream<Duration> get onAudioPositionChanged => _positionController.stream;

  /// Stream of changes on audio duration.
  ///
  /// An event is going to be sent as soon as the audio duration is available
  /// (it might take a while to download or buffer it).
  Stream<Duration> get onDurationChanged => _durationController.stream;

  /// Stream of player completions.
  ///
  /// Events are sent every time an audio is finished, therefore no event is
  /// sent when an audio is paused or stopped.
  ///
  /// [ReleaseMode.LOOP] also sends events to this stream.
  Stream<void> get onPlayerCompletion => _completionController.stream;

  /// Stream of player errors.
  ///
  /// Events are sent when an unexpected error is thrown in the native code.
  Stream<String> get onPlayerError => _errorController.stream;

  /// Handler of changes on player state.
  @deprecated
  AudioPlayerStateChangeHandler audioPlayerStateChangeHandler;

  /// Handler of changes on player position.
  ///
  /// Will continuously update the position of the playback if the status is
  /// [AudioPlayerState.PLAYING].
  ///
  /// You can use it on a progress bar, for instance.
  ///
  /// This is deprecated. Use [onAudioPositionChanged] instead.
  @deprecated
  TimeChangeHandler positionHandler;

  /// Handler of changes on audio duration.
  ///
  /// An event is going to be sent as soon as the audio duration is available
  /// (it might take a while to download or buffer it).
  ///
  /// This is deprecated. Use [onDurationChanged] instead.
  @deprecated
  TimeChangeHandler durationHandler;

  /// Handler of player completions.
  ///
  /// Events are sent every time an audio is finished, therefore no event is
  /// sent when an audio is paused or stopped.
  ///
  /// [ReleaseMode.LOOP] also sends events to this stream.
  ///
  /// This is deprecated. Use [onPlayerCompletion] instead.
  @deprecated
  VoidCallback completionHandler;

  /// Handler of player errors.
  ///
  /// Events are sent when an unexpected error is thrown in the native code.
  ///
  /// This is deprecated. Use [onPlayerError] instead.
  @deprecated
  ErrorHandler errorHandler;

  /// An unique ID generated for this instance of [OCSAudioPlayer].
  ///
  /// This is used to properly exchange messages with the [MethodChannel].
  String playerId;


  /// Creates a new instance and assigns an unique id to it.
  OCSAudioPlayer() {
    playerId = _uuid.v4();
    players[playerId] = this;
  }

  Future<int> _invokeMethod(
      String method, [
        Map<String, dynamic> arguments,
      ]) {
    arguments ??= const {};

    final Map<String, dynamic> withPlayerId = Map.of(arguments)
      ..['playerId'] = playerId;

    return _channel
        .invokeMethod(method, withPlayerId)
        .then((result) => (result as int));
  }

  /// Plays an audio.
  ///
  /// If [isLocal] is true, [url] must be a local file system path.
  /// If [isLocal] is false, [url] must be a remote URL.
  Future<int> play(
      String url, {
        bool isLocal = false,
        double volume = 1.0,
        // position must be null by default to be compatible with radio streams
        Duration position,
        bool stayAwake = false,
        bool proximityMonitoringEnabled = true,// 播放时是否启动临近感应器
      }) async {

    final int result = await _invokeMethod('play', {
      'url': url,
      'isLocal': isLocal,
      'volume': volume,
      'position': position?.inMilliseconds,
      'stayAwake': stayAwake,
      'proximityEnable': proximityMonitoringEnabled,
    });

    if (result == 1) {
      state = AudioPlayerState.PLAYING;
    }

    return result;
  }

  /// Pauses the audio that is currently playing.
  ///
  /// If you call [resume] later, the audio will resume from the point that it
  /// has been paused.
  Future<int> pause() async {
    final int result = await _invokeMethod('pause');

    if (result == 1) {
      state = AudioPlayerState.PAUSED;
    }

    return result;
  }

  /// Stops the audio that is currently playing.
  ///
  /// The position is going to be reset and you will no longer be able to resume
  /// from the last point.
  Future<int> stop() async {
    final int result = await _invokeMethod('stop');

    if (result == 1) {
      state = AudioPlayerState.STOPPED;
    }

    return result;
  }

  /// Resumes the audio that has been paused or stopped, just like calling
  /// [play], but without changing the parameters.
  Future<int> resume() async {
    final int result = await _invokeMethod('resume');

    if (result == 1) {
      state = AudioPlayerState.PLAYING;
    }

    return result;
  }

  /// Releases the resources associated with this media player.
  ///
  /// The resources are going to be fetched or buffered again as soon as you
  /// call [play] or [setUrl].
  Future<int> release() async {
    final int result = await _invokeMethod('release');

    if (result == 1) {
      state = AudioPlayerState.STOPPED;
    }

    return result;
  }

  /// Moves the cursor to the desired position.
  Future<int> seek(Duration position) {
    _positionController.add(position);
    return _invokeMethod('seek', {'position': position.inMilliseconds});
  }

  /// Sets the volume (amplitude).
  ///
  /// 0 is mute and 1 is the max volume. The values between 0 and 1 are linearly
  /// interpolated.
  Future<int> setVolume(double volume) {
    return _invokeMethod('setVolume', {'volume': volume});
  }

  /// Sets the release mode.
  ///
  /// Check [ReleaseMode]'s doc to understand the difference between the modes.
  Future<int> setReleaseMode(ReleaseMode releaseMode) {
    return _invokeMethod(
      'setReleaseMode',
      {'releaseMode': releaseMode.toString()},
    );
  }

  /// Sets the URL.
  ///
  /// Unlike [play], the playback will not resume.
  ///
  /// The resources will start being fetched or buffered as soon as you call
  /// this method.
  Future<int> setUrl(String url, {bool isLocal: false}) {
    return _invokeMethod('setUrl', {'url': url, 'isLocal': isLocal});
  }

  /// Get audio duration after setting url.
  /// Use it in conjunction with setUrl.
  ///
  /// It will be available as soon as the audio duration is available
  /// (it might take a while to download or buffer it if file is not local).
  Future<int> getDuration() {
    return _invokeMethod('getDuration');
  }

  /// Gets audio current playing position
  Future<int> getCurrentPosition() async {
    return _invokeMethod('getCurrentPosition');
  }

  /// 设置举例感应器的状态
  /// 如果设置为true，则会启动距离感应器
  Future setProximityMonitoringEnabled({bool enabled = true}) {
    return _channel.invokeMethod('setProximityMonitoring', enabled);
  }

  static Future<void> platformCallHandler(MethodCall call) async {
    try {
      _doHandlePlatformCall(call);
    } catch (ex) {
      _log('Unexpected error: $ex');
    }
  }

  static Future<void> _doHandlePlatformCall(MethodCall call) async {
    final Map<dynamic, dynamic> callArgs = call.arguments as Map;
    _log('_platformCallHandler call ${call.method} $callArgs');

    final playerId = callArgs['playerId'] as String;
    final OCSAudioPlayer player = players[playerId];
    final value = callArgs['value'];

    switch (call.method) {
      case 'audio.onDuration':
        Duration newDuration = Duration(milliseconds: value);
        player._durationController.add(newDuration);
        // ignore: deprecated_member_use_from_same_package
        player.durationHandler?.call(newDuration);
        break;
      case 'audio.onCurrentPosition':
        Duration newDuration = Duration(milliseconds: value);
        player._positionController.add(newDuration);
        // ignore: deprecated_member_use_from_same_package
        player.positionHandler?.call(newDuration);
        break;
      case 'audio.onComplete':
        player.state = AudioPlayerState.COMPLETED;
        player._completionController.add(null);
        // ignore: deprecated_member_use_from_same_package
        player.completionHandler?.call();
        break;
      case 'audio.onError':
        player.state = AudioPlayerState.STOPPED;
        player._errorController.add(value);
        // ignore: deprecated_member_use_from_same_package
        player.errorHandler?.call(value);
        break;
      default:
        _log('Unknown method ${call.method} ');
    }
  }

  static void _log(String param) {
    if (logEnabled) {
      print(param);
    }
  }

  /// Closes all [StreamController]s.
  ///
  /// You must call this method when your [OCSAudioPlayer] instance is not going to
  /// be used anymore.
  Future<void> dispose() async {
    List<Future> futures = [];

    if (!_playerStateController.isClosed)
      futures.add(_playerStateController.close());
    if (!_positionController.isClosed) futures.add(_positionController.close());
    if (!_durationController.isClosed) futures.add(_durationController.close());
    if (!_completionController.isClosed)
      futures.add(_completionController.close());
    if (!_errorController.isClosed) futures.add(_errorController.close());

    await Future.wait(futures);
  }
}
