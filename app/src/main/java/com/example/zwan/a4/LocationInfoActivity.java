package com.example.zwan.a4;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationInfoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    LocationManager locationManager;
    LocationListener locationListener;

    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;

    public GoogleApiClient mApiClient;
    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    TextView activityTextView;

    class MyPhoneStateListener extends PhoneStateListener {
        public int signalStrengthValue;

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99)
                    signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    signalStrengthValue = signalStrength.getGsmSignalStrength();
            } else {
                signalStrengthValue = signalStrength.getCdmaDbm();
            }
            TextView signalTextView = (TextView) findViewById(R.id.signalTextView);
            signalTextView.setText("Signal Strength: " + Integer.toString(signalStrengthValue));
            // Toast.makeText(LocationInfoActivity.this, "Signal Strength: " + signalStrengthValue, Toast.LENGTH_SHORT).show();
        }
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra("STRING_EXTRA");
            String activityString = "Activity: ";
            for(DetectedActivity activity: detectedActivities){
                if( activity.getConfidence() > 50 )
                    // activityString +=  "Activity: " + getDetectedActivity(activity.getType()) + ", Confidence: " + activity.getConfidence() + "%\n";
                    activityString += getDetectedActivity(activity.getType()) + ", Confidence: " + activity.getConfidence() + "% ";
            }
            activityTextView.setText(activityString);
            // Toast.makeText(context, activityString, Toast.LENGTH_SHORT).show();
        }
    }

    public String getDetectedActivity(int detectedActivityType) {
        // Resources resources = this.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                // return resources.getString(R.string.in_vehicle);
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                // return resources.getString(R.string.on_bicycle);
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                // return resources.getString(R.string.on_foot);
                return "On foot";
            case DetectedActivity.RUNNING:
                // return resources.getString(R.string.running);
                return "Running";
            case DetectedActivity.WALKING:
                // return resources.getString(R.string.walking);
                return "Walking";
            case DetectedActivity.STILL:
                // return resources.getString(R.string.still);
                return "Still";
            case DetectedActivity.TILTING:
                // return resources.getString(R.string.tilting);
                return "Tilting";
            case DetectedActivity.UNKNOWN:
                // return resources.getString(R.string.unknown);
                return "Unknown";
            default:
                // return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
                return "In detection";
        }
    }

    public void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateLocation(lastKnownLocation);
        }
    }

    public void updateLocation(Location location) {
        Log.d("Location", location.toString());
        // Toast.makeText(this, "Location" + location.toString(), Toast.LENGTH_SHORT).show();
        TextView latText = (TextView) findViewById(R.id.latText);
        TextView lngText = (TextView) findViewById(R.id.lngText);
        TextView accText = (TextView) findViewById(R.id.accText);
        TextView altText = (TextView) findViewById(R.id.altText);
        TextView speedTextView = (TextView) findViewById(R.id.speedTextView);

        latText.setText("Latitude: " + location.getLatitude());
        lngText.setText("Longitude: " + location.getLongitude());
        accText.setText("Accuracy: " + location.getAccuracy());
        altText.setText("Altitude: " + location.getAltitude());
        speedTextView.setText("Speed: " + location.getSpeed() * 2.237 + " mph");

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "Could not find an address.";
        try {
            List<Address> listAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (listAddress != null && listAddress.size() > 0) {
                Log.d("Address", listAddress.get(0).toString());
                address = "Address: \n";
                if (listAddress.get(0).getAddressLine(0) != null)
                    address += listAddress.get(0).getAddressLine(0) + "\n";
                if (listAddress.get(0).getAddressLine(1) != null)
                    address += listAddress.get(0).getAddressLine(1) + "\n";
                if (listAddress.get(0).getAddressLine(2) != null)
                    address += listAddress.get(0).getAddressLine(2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView addressText = (TextView) findViewById(R.id.addressText);
        addressText.setText(address);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;
        TextView batteryTextView = (TextView) findViewById(R.id.batteryTextView);
        batteryTextView.setText("Battery Level: " + Float.toString(batteryPct * 100) + "%");
        // Toast.makeText(this, "Battery Level: " + batteryPct, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        // Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 0, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Google Play Status: ", "Connection Suspended");
        mApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("Google Play Status: ", "Connection Failed, result: " + connectionResult.getErrorCode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info);

        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocation(location);
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
        if (Build.VERSION.SDK_INT < 23) {
            startListening();
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            else {
                startListening();
            }

        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();

        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();
        activityTextView = (TextView) findViewById(R.id.activityTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("STRING_ACTION"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mApiClient.isConnected()) {
            mApiClient.disconnect();
        }
    }

}

