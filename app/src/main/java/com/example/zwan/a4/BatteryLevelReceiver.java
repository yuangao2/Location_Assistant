package com.example.zwan.a4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

/**
 * Created by Yuan on 4/5/2017.
 */

public class BatteryLevelReceiver extends BroadcastReceiver {
    public BatteryLevelReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentText( "Battery Low" );
            builder.setSmallIcon( R.mipmap.ic_launcher );
            builder.setContentTitle( "Google Maps Demo" );
            NotificationManagerCompat.from(context).notify(0, builder.build());
        }
        if (intent.getAction().equals("android.intent.action.BATTERY_OK")) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentText( "Battery OK" );
            builder.setSmallIcon( R.mipmap.ic_launcher );
            builder.setContentTitle( "Google Maps Demo" );
            NotificationManagerCompat.from(context).notify(0, builder.build());
        }
    }
}
