package com.jzxl.ocs_plugin.audioplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadsetReceiver extends BroadcastReceiver {
    // 是否插入耳机
    public static boolean isWiredHeadsetOn;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("state")) {
            if (0 == intent.getIntExtra("state", 0)) {
                isWiredHeadsetOn = false;
            } else if (1 == intent.getIntExtra("state", 0)) {
                isWiredHeadsetOn = true;
            }
        }
    }
}
