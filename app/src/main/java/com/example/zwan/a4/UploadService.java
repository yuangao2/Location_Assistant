package com.example.zwan.a4;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class UploadService extends Service {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private int uid;


    public UploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(locationManager!=null){
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Family Locator is working.")
                .setSmallIcon(R.drawable.location)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
        uid = pref.getInt("uid", 0);
        Log.e("uid", String.valueOf(uid));
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
                Log.e("latitude", String.valueOf(latitude));
                Log.e("longitude", String.valueOf(longitude));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String requestURL = "https://people.cs.clemson.edu/~zwan/android/uploadrecord.php";
                        String charset = "UTF-8";
                        try {
                            FileUploader multipart = new FileUploader(requestURL, charset);
                            multipart.addHeaderField("User-Agent", "CodeJava");
                            multipart.addHeaderField("Test-Header", "Header-Value");
                            multipart.addFormField("uid", String.valueOf(uid));
                            multipart.addFormField("latitude", String.valueOf(latitude));
                            multipart.addFormField("longitude", String.valueOf(longitude));
                            multipart.addFormField("time", Long.toString(System.currentTimeMillis()));

                            List<String> response = multipart.finish();

                            System.out.println("SERVER REPLIED:");
                            //System.out.println(response.get(1));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();*/
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int aMinute = 5*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+aMinute;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

}
