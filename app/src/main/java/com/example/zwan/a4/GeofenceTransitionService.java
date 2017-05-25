package com.example.zwan.a4;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuan on 4/25/2017.
 */

public class GeofenceTransitionService extends IntentService {
    private static final String TAG = GeofenceTransitionService.class.getSimpleName();
    public static final int GEOFENCE_NOTIFICATION_ID = 0;
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public GeofenceTransitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        // Retrieve GeofenceTrasition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();

        // Check if the transition type
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Create a detail message with Geofences received
            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences );
            Log.d(TAG, geofenceTransitionDetails);
            // Send notification details as a String
            sendGCM(geofenceTransitionDetails);
            sendNotification( geofenceTransitionDetails );
        }
    }

    // Create a detail message with Geofences received
    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    // Send a notification
    private void sendNotification( String msg ) {
        Log.i(TAG, "send Geofencing Notification: " + msg );

        // Intent to start the main Activity
        Intent notificationIntent = MapsActivity.makeNotificationIntent(getApplicationContext(), msg);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MapsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent));
    }

    // Create a notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(0, notificationBuilder.build());
        return notificationBuilder.build();
    }

    private void sendGCM(String msg) {
        Log.i(TAG, "Send Geofencing GCM: " + msg);
        HttpURLConnection conn = null;
        URL url = null;
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("title", "Geofencing Notification")
                .appendQueryParameter("body", msg);
        String query = builder.build().getEncodedQuery();
        // Toast.makeText(this, "Add : "+ query, Toast.LENGTH_LONG).show();
        // Log.e("LatLng", query);
        // String response;

        try {
            url = new URL("https://people.cs.clemson.edu/~yuang/cpsc6820/Project/gcm.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Setup HttpURLConnection class to send and receive data from php and mysql
        try {
            // Setup HttpURLConnection class to send and receive data from php and mysql
            conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");

            // setDoInput and setDoOutput method depict handling of both send and receive
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Open connection for sending data
            PrintWriter pw = new PrintWriter(conn.getOutputStream());
            pw.print(query);
            pw.flush();
            pw.close();
            conn.connect();
        }
        catch (IOException e1) {
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

            }
            else{
                Log.e(TAG, "unsuccessful");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            conn.disconnect();
        }

    }

    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}



