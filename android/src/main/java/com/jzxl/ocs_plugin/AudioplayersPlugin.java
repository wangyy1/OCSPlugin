package com.jzxl.ocs_plugin;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.jzxl.ocs_plugin.audioplayer.HeadsetReceiver;
import com.jzxl.ocs_plugin.audioplayer.Player;
import com.jzxl.ocs_plugin.audioplayer.PlayerModel;
import com.jzxl.ocs_plugin.audioplayer.ReleaseMode;
import com.jzxl.ocs_plugin.audioplayer.WrappedMediaPlayer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;

public class AudioplayersPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {
    private static final String TAG = "AudioplayersPlugin";
    
    
    private static final Logger LOGGER = Logger.getLogger(AudioplayersPlugin.class.getCanonicalName());

    private FlutterPluginBinding mFlutterPluginBinding;
    private MethodChannel channel;
    private final Map<String, Player> mediaPlayers = new HashMap<>();
    private final Handler handler = new Handler();
    private Runnable positionUpdates;
    private Activity activity;

    @Override
    public void onMethodCall(final MethodCall call, final MethodChannel.Result response) {
        try {
            handleMethodCall(call, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error!", e);
            response.error("Unexpected error!", e.getMessage(), e);
        }
    }

    private void handleMethodCall(final MethodCall call, final MethodChannel.Result response) {
        final String playerId = call.argument("playerId");
//        final String mode = call.argument("mode");
        final Player player = getPlayer(playerId);
        switch (call.method) {
            case "play": {
                final String url = call.argument("url");
                final double volume = call.argument("volume");
                final Integer position = call.argument("position");
                final boolean proximityEnable = call.argument("proximityEnable");
                final boolean isLocal = call.argument("isLocal");
                final boolean stayAwake = call.argument("stayAwake");
                if (proximityEnable) {
                    Log.e(TAG, "handleMethodCall: SPEAKER" );
                    player.configAttributes(PlayerModel.values()[0], stayAwake, activity.getApplicationContext());
                } else  {
                    Log.e(TAG, "handleMethodCall: STETHOSCOPE" );
                    player.configAttributes(PlayerModel.values()[1], stayAwake, activity.getApplicationContext());
                }
                player.setVolume(volume);
                player.setUrl(url, isLocal);
                if (position != null) {
                    player.seek(position);
                }
                player.play();
                break;
            }
            case "resume": {
                player.play();
                break;
            }
            case "pause": {
                player.pause();
                break;
            }
            case "stop": {
                player.stop();
                break;
            }
            case "release": {
                player.release();
                break;
            }
            case "seek": {
                final Integer position = call.argument("position");
                player.seek(position);
                break;
            }
            case "setVolume": {
                final double volume = call.argument("volume");
                player.setVolume(volume);
                break;
            }
            case "setUrl": {
                final String url = call.argument("url");
                final boolean isLocal = call.argument("isLocal");
                player.setUrl(url, isLocal);
                break;
            }
            case "getDuration": {

                response.success(player.getDuration());
                return;
            }
            case "getCurrentPosition": {
                response.success(player.getCurrentPosition());
                return;
            }
            case "setReleaseMode": {
                final String releaseModeName = call.argument("releaseMode");
                final ReleaseMode releaseMode = ReleaseMode.valueOf(releaseModeName.substring("ReleaseMode.".length()));
                player.setReleaseMode(releaseMode);
                break;
            }
            default: {
                response.notImplemented();
                return;
            }
        }
        response.success(1);
    }

//    @Deprecated
//    private Player getPlayer(String playerId, String mode) {
//        if (!mediaPlayers.containsKey(playerId)) {
//            Player player =
//                    mode.equalsIgnoreCase("PlayerMode.MEDIA_PLAYER") ?
//                            new WrappedMediaPlayer(this, playerId) :
//                            new WrappedSoundPool(this, playerId);
//            mediaPlayers.put(playerId, player);
//        }
//        return mediaPlayers.get(playerId);
//    }

    private Player getPlayer(String playerId) {
        if (!mediaPlayers.containsKey(playerId)) {
            Player player = new WrappedMediaPlayer(this, playerId, activity);
            mediaPlayers.put(playerId, player);
        }
        return mediaPlayers.get(playerId);
    }

    public void handleIsPlaying(Player player) {
        startPositionUpdates();
    }

    public void handleDuration(Player player) {
        channel.invokeMethod("audio.onDuration", buildArguments(player.getPlayerId(), player.getDuration()));
    }

    public void handleCompletion(Player player) {
        channel.invokeMethod("audio.onComplete", buildArguments(player.getPlayerId(), true));
    }

    private void startPositionUpdates() {
        if (positionUpdates != null) {
            return;
        }
        positionUpdates = new UpdateCallback(mediaPlayers, channel, handler, this);
        handler.post(positionUpdates);
    }

    private void stopPositionUpdates() {
        positionUpdates = null;
        handler.removeCallbacksAndMessages(null);
    }

    private static Map<String, Object> buildArguments(String playerId, Object value) {
        Map<String, Object> result = new HashMap<>();
        result.put("playerId", playerId);
        result.put("value", value);
        return result;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        this.mFlutterPluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {

    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        channel = new MethodChannel(mFlutterPluginBinding.getBinaryMessenger(), "ocs.audioPlayer.channel");
        channel.setMethodCallHandler(this);

        this.activity = binding.getActivity();
        HeadsetReceiver headsetPlugReceiver = new HeadsetReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        this.activity.registerReceiver(headsetPlugReceiver, intentFilter);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        channel.setMethodCallHandler(null);
    }

    private static final class UpdateCallback implements Runnable {

        private final WeakReference<Map<String, Player>> mediaPlayers;
        private final WeakReference<MethodChannel> channel;
        private final WeakReference<Handler> handler;
        private final WeakReference<AudioplayersPlugin> audioplayersPlugin;

        private UpdateCallback(final Map<String, Player> mediaPlayers,
                               final MethodChannel channel,
                               final Handler handler,
                               final AudioplayersPlugin audioplayersPlugin) {
            this.mediaPlayers = new WeakReference<>(mediaPlayers);
            this.channel = new WeakReference<>(channel);
            this.handler = new WeakReference<>(handler);
            this.audioplayersPlugin = new WeakReference<>(audioplayersPlugin);
        }

        @Override
        public void run() {
            final Map<String, Player> mediaPlayers = this.mediaPlayers.get();
            final MethodChannel channel = this.channel.get();
            final Handler handler = this.handler.get();
            final AudioplayersPlugin audioplayersPlugin = this.audioplayersPlugin.get();

            if (mediaPlayers == null || channel == null || handler == null || audioplayersPlugin == null) {
                if (audioplayersPlugin != null) {
                    audioplayersPlugin.stopPositionUpdates();
                }
                return;
            }

            boolean nonePlaying = true;
            for (Player player : mediaPlayers.values()) {
                if (!player.isActuallyPlaying()) {
                    continue;
                }
                try {
                    nonePlaying = false;
                    final String key = player.getPlayerId();
                    final int duration = player.getDuration();
                    final int time = player.getCurrentPosition();
                    channel.invokeMethod("audio.onDuration", buildArguments(key, duration));
                    channel.invokeMethod("audio.onCurrentPosition", buildArguments(key, time));
                } catch(UnsupportedOperationException e) {

                }
            }

            if (nonePlaying) {
                audioplayersPlugin.stopPositionUpdates();
            } else {
                handler.postDelayed(this, 200);
            }
        }
    }
}
