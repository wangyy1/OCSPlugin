package com.jzxl.ocs_plugin.audioplayer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.jzxl.ocs_plugin.AudioplayersPlugin;

import java.io.IOException;

public class WrappedMediaPlayer extends Player implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, SensorEventListener {

    private String playerId;

    private String url;
    private double volume = 1.0;
    // 播放类型
    private PlayerModel playerModel = PlayerModel.MUSIC;
    private boolean stayAwake;
    private ReleaseMode releaseMode = ReleaseMode.RELEASE;

    private boolean released = true;
    private boolean prepared = false;
    private boolean playing = false;

    private int shouldSeekTo = -1;

    private SensorManager sensorManager;
    private MediaPlayer player;
    private AudioplayersPlugin ref;
    private Context context;
    private Sensor sensor;

    public WrappedMediaPlayer(AudioplayersPlugin ref, String playerId, Context context) {
        this.ref = ref;
        this.playerId = playerId;
        this.context = context;
    }

    private void addSensorListener() {
        if (playerModel != null && playerModel == PlayerModel.MUSIC) {
            this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            sensor = this.sensorManager.getDefaultSensor(8);
            this.sensorManager.registerListener(this, sensor, 3);
        }
    }


    /**
     * Setter methods
     */

    @Override
    public void setUrl(String url, boolean isLocal) {
        if (!objectEquals(this.url, url)) {
            this.url = url;
            if (this.released) {
                this.player = createPlayer();
                this.released = false;
            } else if (this.prepared) {
                this.player.reset();
                this.prepared = false;
            }

            this.setSource(url);
            this.player.setVolume((float) volume, (float) volume);
            this.player.setLooping(this.releaseMode == ReleaseMode.LOOP);
            this.player.prepareAsync();
        }
    }

    @Override
    public void setVolume(double volume) {
        if (this.volume != volume) {
            this.volume = volume;
            if (!this.released) {
                this.player.setVolume((float) volume, (float) volume);
            }
        }
    }

    @Override
    public void configAttributes(PlayerModel playerModel, boolean stayAwake, Context context) {
        if (this.playerModel != playerModel) {
            this.playerModel = playerModel;
            if (!this.released) {
                setAttributes(player);
            }
        }
        if (this.stayAwake != stayAwake) {
            this.stayAwake = stayAwake;
            if (!this.released && this.stayAwake) {
                this.player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            }
        }
    }

    @Override
    public void setReleaseMode(ReleaseMode releaseMode) {
        if (this.releaseMode != releaseMode) {
            this.releaseMode = releaseMode;
            if (!this.released) {
                this.player.setLooping(releaseMode == ReleaseMode.LOOP);
            }
        }
    }

    /**
     * Getter methods
     */

    @Override
    public int getDuration() {
        return this.player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return this.player.getCurrentPosition();
    }

    @Override
    public String getPlayerId() {
        return this.playerId;
    }

    @Override
    public boolean isActuallyPlaying() {
        return this.playing && this.prepared;
    }

    /**
     * Playback handling methods
     */

    @Override
    public void play() {
        if (!this.playing) {
            this.playing = true;
            if (this.released) {
                this.released = false;
                this.player = createPlayer();
                this.setSource(url);
                this.player.prepareAsync();
            } else if (this.prepared) {
                this.player.start();
                addSensorListener();
                this.ref.handleIsPlaying(this);
            }
        }
    }

    @Override
    public void stop() {
        if (this.released) {
            return;
        }

        if (releaseMode != ReleaseMode.RELEASE) {
            if (this.playing) {
                this.playing = false;
                this.player.pause();
                this.player.seekTo(0);
            }
        } else {
            this.release();
        }
    }

    @Override
    public void release() {
        if (this.released) {
            return;
        }

        if (this.playing) {
            this.player.stop();
        }
        this.player.reset();
        this.player.release();
        this.player = null;

        if (this.sensorManager != null) {
            this.sensorManager.unregisterListener(this);
        }

        this.sensorManager = null;

        this.prepared = false;
        this.released = true;
        this.playing = false;
    }

    @Override
    public void pause() {
        if (this.playing) {
            this.playing = false;
            this.player.pause();
        }
    }

    // seek operations cannot be called until after
    // the player is ready.
    @Override
    public void seek(int position) {
        if (this.prepared)
            this.player.seekTo(position);
        else
            this.shouldSeekTo = position;
    }

    /**
     * MediaPlayer callbacks
     */

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        this.prepared = true;
        ref.handleDuration(this);
        if (this.playing) {
            this.player.start();
            addSensorListener();
            ref.handleIsPlaying(this);
        }
        if (this.shouldSeekTo >= 0) {
            this.player.seekTo(this.shouldSeekTo);
            this.shouldSeekTo = -1;
        }
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        if (releaseMode != ReleaseMode.LOOP) {
            this.stop();
        }
        ref.handleCompletion(this);
    }

    /**
     * Internal logic. Private methods
     */

    private MediaPlayer createPlayer() {
        MediaPlayer player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        setAttributes(player);
        player.setVolume((float) volume, (float) volume);
        player.setLooping(this.releaseMode == ReleaseMode.LOOP);
        return player;
    }

    private void setSource(String url) {
        try {
            this.player.setDataSource(url);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to access resource", ex);
        }
    }

    @SuppressWarnings("deprecation")
    private void setAttributes(MediaPlayer player) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int usage;
            int contentType;
            switch (playerModel) {
                case VOICE_CALL:
                    usage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
                    contentType = AudioAttributes.CONTENT_TYPE_SPEECH;
                    break;
                case MUSIC:
                    usage = AudioAttributes.USAGE_MEDIA;
                    contentType = AudioAttributes.CONTENT_TYPE_MUSIC;
                    break;
//                case NOTIFICATION:
//                    usage = AudioAttributes.USAGE_NOTIFICATION;
//                    contentType = AudioAttributes.CONTENT_TYPE_SONIFICATION;
//                    break;
//                case RING:
//                    usage = AudioAttributes.USAGE_NOTIFICATION_RINGTONE;
//                    contentType = AudioAttributes.USAGE_NOTIFICATION_RINGTONE;
//                    break;
                default:
                    usage = AudioAttributes.USAGE_MEDIA;
                    contentType = AudioAttributes.CONTENT_TYPE_MUSIC;
                    break;
            }
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(contentType)
                    .build()
            );
        } else {
            // This method is deprecated but must be used on older devices
            int streamType;
            switch (playerModel) {
                case VOICE_CALL:
                    streamType = AudioManager.STREAM_VOICE_CALL;
                    break;
                case MUSIC:
                    streamType = AudioManager.STREAM_MUSIC;
                    break;
//                case NOTIFICATION:
//                    streamType = AudioManager.STREAM_NOTIFICATION;
//                    break;
//                case RING:
//                    streamType = AudioManager.STREAM_RING;
//                    break;
                default:
                    streamType = AudioManager.STREAM_MUSIC;
                    break;
            }
            player.setAudioStreamType(streamType);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float range = sensorEvent.values[0];
        Log.e("111111", "onAccuracyChanged: " + range);
        if (HeadsetReceiver.isWiredHeadsetOn) {
            return;
        }
        if (this.sensor != null && this.player != null) {
            if (playing) {
                if ((double) range > 0.0D) {
                    if (playerModel != PlayerModel.VOICE_CALL) {
                        return;
                    }
                    stop();
                    configAttributes(PlayerModel.MUSIC, stayAwake, context.getApplicationContext());
                    setVolume(volume);
                    setUrl(url, true);
                    seek(0);
                    play();
                } else {
                    stop();
                    configAttributes(PlayerModel.VOICE_CALL, stayAwake, context.getApplicationContext());
                    setVolume(volume);
                    setUrl(url, true);
                    seek(0);
                    play();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
