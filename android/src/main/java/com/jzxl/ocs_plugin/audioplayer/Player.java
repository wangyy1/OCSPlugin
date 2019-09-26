package com.jzxl.ocs_plugin.audioplayer;

import android.content.Context;

public abstract class Player {

    protected static boolean objectEquals(Object o1, Object o2) {
        return o1 == null && o2 == null || o1 != null && o1.equals(o2);
    }

    public abstract String getPlayerId();

    public abstract void play();

    public abstract void stop();

    public abstract void release();

    public abstract void pause();

    public abstract void setUrl(String url, boolean isLocal);

    public abstract void setVolume(double volume);

    // TODO 废弃
//    abstract void configAttributes(boolean respectSilence, boolean stayAwake, Context context);

    public abstract void configAttributes(PlayerModel playerModel, boolean stayAwake, Context context);

    public abstract void setReleaseMode(ReleaseMode releaseMode);

    public abstract int getDuration();

    public abstract int getCurrentPosition();

    public abstract boolean isActuallyPlaying();

    /**
     * Seek operations cannot be called until after the player is ready.
     */
    public abstract void seek(int position);
}
