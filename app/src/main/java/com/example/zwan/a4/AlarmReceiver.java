package com.example.zwan.a4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by sersh on 4/8/17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        Intent i = new Intent(context, UploadService.class);
        context.startService(i);
    }
}
