package com.jzxl.ocs_plugin.audioplayer;

/**
 * 播放类型
 * 0语音电话（听筒）
 * 1手机音乐
 * 2系统提示的通知
 * 3电话铃声
 */
public enum PlayerModel {
    VOICE_CALL(0),
    MUSIC(1);

    private int value;

    PlayerModel(int value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
