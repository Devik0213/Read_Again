package com.devik.readagain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Harry on 2015. 2. 9..
 */
public class SystemBootCompletedReceiver extends BroadcastReceiver {
    public SystemBootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MonitorClipService.class));
//        context.startService(new Intent(context, AlarmRegisterService.class));
    }
}
