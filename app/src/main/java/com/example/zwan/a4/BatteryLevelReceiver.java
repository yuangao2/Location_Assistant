package com.example.zwan.a4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Yuan on 4/5/2017.
 */

public class BatteryLevelReceiver extends BroadcastReceiver {
    private static final String TAG = BatteryLevelReceiver.class.getSimpleName();
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;

    public BatteryLevelReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = "";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.cast_ic_notification_small_icon);
        builder.setContentTitle("Battery Notification");
        if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
            builder.setContentText("Battery Low");
            msg = "Battery Low";
        }
        if (intent.getAction().equals("android.intent.action.BATTERY_OKAY")) {
            builder.setContentText("Battery Okay");
            msg = "Battery Okay";
        }
        NotificationManagerCompat.from(context).notify(0, builder.build());
        final PendingResult result = goAsync();
        final String finalMsg = msg;
        Thread thread = new Thread() {
            public void run() {
                // int i;
                // Do processing
                sendGCM(finalMsg);
                // result.setResultCode(i);
                result.finish();
            }
        };
        thread.start();
    }

    private void sendGCM(String msg) {
        Log.i(TAG, "Send Battery GCM: " + msg);
        HttpURLConnection conn = null;
        URL url = null;
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("title", "Battery Notification")
                .appendQueryParameter("body", msg);
        String query = builder.build().getEncodedQuery();

        try {
            url = new URL("https://people.cs.clemson.edu/~yuang/cpsc6820/Test/gcm.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            PrintWriter pw = new PrintWriter(conn.getOutputStream());
            pw.print(query);
            pw.flush();
            pw.close();
            conn.connect();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            int response_code = conn.getResponseCode();

            // Check if successful connection made
            if (response_code == HttpURLConnection.HTTP_OK) {

                // Read data sent from server
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                // Pass data to onPostExecute method
                Log.d(TAG, "GCM response: " + result.toString());

            } else {
                Log.e(TAG, "unsuccessful");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

    }
}