package com.example.zwan.a4;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.concurrent.ExecutionException;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        ResultCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Context mContext;
    private int uid;
    private int fid;
    private String userName;
    HttpRequest httpRequest;

    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
        uid = pref.getInt("uid", 0);
        fid = pref.getInt("fid", 0);
        userName = pref.getString("name", "y");
        Log.d(TAG, "uid:" + String.valueOf(uid) + "username: " + userName);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createGoogleApi();
        mContext = this;
        Intent intent = new Intent(this, RegistrationService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.i(TAG, "onResult: " + result);
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "result is successful");
            drawGeofence();
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        // if ( geoFenceLimits != null )
            // geoFenceLimits.remove();
        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius( GEOFENCE_RADIUS );
        geoFenceLimits = mMap.addCircle( circleOptions );
    }

    private List<Marker> geoFenceList = new ArrayList<Marker>();
    private void loadGeofence() {
        Log.d(TAG, "Load Geofence");
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("UId", String.valueOf(uid));
        String query = builder.build().getEncodedQuery();
        String response;
        httpRequest = new HttpRequest();
        try {
            response = httpRequest.execute("https://people.cs.clemson.edu/~yuang/cpsc6820/Project/get_place.php", query).get();
            Log.d(TAG, "Get Geofence Response: " + response);
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int pid = jsonObject.getInt("PId");
                String place = jsonObject.getString("Place");
                double latitude = jsonObject.getDouble("Latitude");
                double longitude = jsonObject.getDouble("Longitude");
                LatLng placeLatLng = new LatLng(latitude, longitude);
                markerForGeofence(placeLatLng, place);
                drawGeofence();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private Marker locationMarker;
    // Create a Location Marker
    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( mMap!=null ) {
            // Remove the anterior marker
            if ( locationMarker != null )
                locationMarker.remove();
            locationMarker = mMap.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mMap.moveCamera(cameraUpdate);
        }
    }

    private Marker geoFenceMarker;
    // Create a marker for the geofence creation
    private void markerForGeofence(LatLng latLng, String place) {
        Log.i(TAG, "markerForGeofence("+latLng+")");
        if (userName != null) {
            String title = userName + "'s " + place;
            // Define marker options
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title(title);
            if (mMap != null) {
                geoFenceMarker = mMap.addMarker(markerOptions);
                startGeofence(geoFenceMarker);
            }
        }
    }

    public void addGeofenceDB(LatLng latlng, String place) {
        Log.d(TAG, "Add Geofence Database: " + place);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("UId", String.valueOf(uid))
                .appendQueryParameter("Latitude", String.valueOf(latlng.latitude))
                .appendQueryParameter("Longitude", String.valueOf(latlng.longitude))
                .appendQueryParameter("Place", place);
        String query = builder.build().getEncodedQuery();
        String response;
        try {
            response = new HttpRequest().execute("https://people.cs.clemson.edu/~yuang/cpsc6820/Project/add_place.php", query).get();
            Log.d(TAG, "Add Geofence Response: " + response);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void removeGeofenceDB(LatLng latlng){
        Log.d(TAG, "Delete Geofence Database: " + String.valueOf(latlng));
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("UId", String.valueOf(uid))
                .appendQueryParameter("Latitude", String.valueOf(latlng.latitude))
                .appendQueryParameter("Longitude", String.valueOf(latlng.longitude));
        String query = builder.build().getEncodedQuery();
        String response;
        try {
            response = new HttpRequest().execute("https://people.cs.clemson.edu/~yuang/cpsc6820/Project/remove_place.php", query).get();
            Log.d(TAG, "Remove Geofence Response: " + response);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        final LatLng latlng = latLng;
        Log.d(TAG, "onMapClick("+latLng +")");
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        final EditText input = new EditText(this);
        input.setHint("New Place for Geofence");
        builder.setTitle("Add Geofence");
        builder.setMessage("Do you want to add this location?");
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(input.getText().toString().equals("")) {
                    Toast.makeText(mContext, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    String placeName = input.getText().toString();
                    markerForGeofence(latlng, placeName);
                    addGeofenceDB(latlng, placeName);
                    // startGeofence();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
//        markerForGeofence(latLng);
//        startGeofence();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        final Marker mMarker = marker;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Delete Geofence: " + marker.getTitle());
        builder.setMessage("Do you want delete to this location?");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LocationServices.GeofencingApi.removeGeofences(
                        googleApiClient,
                        // This is the same pending intent that was used in addGeofences().
                        geoFencePendingIntent
                ).setResultCallback(MapsActivity.this); // Result processed in onResult().
                mMarker.remove();
                LatLng latlng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                removeGeofenceDB(latlng);
                // Toast.makeText(mContext, ""+ marker.getId(), Toast.LENGTH_SHORT).show();
//                if(input.getText().toString().equals("")) {
//                    Toast.makeText(mContext, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    String placeName = input.getText().toString();
//                    markerForGeofence(latlng, placeName);
//                    addGeofenceDB(latlng, placeName);
//                    // startGeofence();
//                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    // Get last known location
    private Location lastLocation;
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " + "Long: " + lastLocation.getLongitude() + " | Lat: " + lastLocation.getLatitude());
                markerLocation(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }

    // Start location Updates
    private LocationRequest locationRequest;
    private final int UPDATE_INTERVAL =  60000;
    private final int FASTEST_INTERVAL = 10000;
    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        if ( checkPermission() ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            loadGeofence();
        }
    }


    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION }, 1);
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case 1: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
    }

    // Create a Geofence
    // private static final long GEO_DURATION = 60 * 60 * 1000;
    // private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters
    private Geofence createGeofence(LatLng latLng, float radius, String geofenceId ) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(  NEVER_EXPIRE )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceTransitionService.class);
        return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        geoFencePendingIntent = createGeofencePendingIntent();
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(googleApiClient, request, geoFencePendingIntent).setResultCallback(this);
    }
    // Start Geofence creation process
    private void startGeofence(Marker marker) {
        Log.i(TAG, "startGeofence()");
        if( marker != null ) {
            Geofence geofence = createGeofence( marker.getPosition(), GEOFENCE_RADIUS, marker.getTitle() );
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    static Intent makeNotificationIntent(Context geofenceService, String msg)
    {
        Log.d(TAG,msg);
        return new Intent(geofenceService, MapsActivity.class);
    }

    public static class HttpRequest extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                // Enter URL address where your php file resides
                url = new URL(params[0]);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
                return "MalformedURLException";
            }

            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                PrintWriter pw = new PrintWriter(conn.getOutputStream());
                pw.print(params[1]);
                pw.flush();
                pw.close();
                conn.connect();
            }
            catch (IOException e1) {
                e1.printStackTrace();
                return "IOException";
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
                    return(result.toString());

                }
                else{
                    return("unsuccessful");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                return "IOException";
            }
            finally {
                conn.disconnect();
            }
        }
    }
}
