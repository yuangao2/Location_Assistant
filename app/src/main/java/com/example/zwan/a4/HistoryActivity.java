package com.example.zwan.a4;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HistoryActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener {
    public static final int SHOW_RESPONSE = 0;
    public static final int SHOW_7DAYS_RESPONSE = 1;
    private GoogleMap mMap;
    private int uid;
    private List<LatLng> locationList = new ArrayList<LatLng>();
    private LatLng lastLocation = new LatLng(0,0);
    private static final String TAG = HistoryActivity.class.getSimpleName();

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    Log.e("history", response);
                    if(response.equals("fail")){
                        Toast.makeText(getApplicationContext(), "Update faileds.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //mMap.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int thisuid = jsonObject.getInt("UId");
                                double latitude = jsonObject.getDouble("Latitude");
                                double longitude = jsonObject.getDouble("Longitude");
                                long time = jsonObject.getLong("Time");
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
                                Date resultdate = new Date(time);
                                //Log.e("la", String.valueOf(latitude));
                                //Log.e("long", String.valueOf(longitude));
                                LatLng location = new LatLng(latitude,longitude);
                                float[] results = new float[1];
                                Location.distanceBetween(
                                        location.latitude,
                                        location.longitude,
                                        lastLocation.latitude,
                                        lastLocation.longitude,
                                        results);
                                if(results[0]>50){
                                    //Log.e("DDDD", String.valueOf(results[0]));
                                    mMap.addMarker(new MarkerOptions().position(location).title(sdf.format(resultdate)).icon(BitmapDescriptorFactory.fromResource(R.drawable.spot)));
                                    lastLocation = location;
                                }
                                if(i==0) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                                }
                                //mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.spot)));
                                locationList.add(location);
                            }
                            Polyline polyline = mMap.addPolyline(new PolylineOptions().width(15).color(Color.RED));
                            polyline.setPoints(locationList);
                            //mMap.addPolyline(new PolylineOptions().width(5).add(locationList.get(0)).add(locationList.get(5)).color(Color.RED));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case SHOW_7DAYS_RESPONSE:
                    response = (String) msg.obj;
                    Log.e("history", response);
                    if(response.equals("fail")){
                        Toast.makeText(getApplicationContext(), "Update faileds.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //mMap.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int thisuid = jsonObject.getInt("UId");
                                double latitude = jsonObject.getDouble("Latitude");
                                double longitude = jsonObject.getDouble("Longitude");
                                long time = jsonObject.getLong("Time");
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
                                Date resultdate = new Date(time);
                                //Log.e("la", String.valueOf(latitude));
                                //Log.e("long", String.valueOf(longitude));
                                LatLng location = new LatLng(latitude,longitude);
                                float[] results = new float[1];
                                Location.distanceBetween(
                                        location.latitude,
                                        location.longitude,
                                        lastLocation.latitude,
                                        lastLocation.longitude,
                                        results);
                                if(results[0]>100){
                                    //Log.e("DDDD", String.valueOf(results[0]));
                                    mMap.addMarker(new MarkerOptions().position(location).title(sdf.format(resultdate)).icon(BitmapDescriptorFactory.fromResource(R.drawable.spot)));
                                    lastLocation = location;
                                }
                                if(i==0) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                                }
                                //mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.spot)));
                                locationList.add(location);
                            }
                            Polyline polyline = mMap.addPolyline(new PolylineOptions().width(15).color(Color.RED));
                            polyline.setPoints(locationList);
                            //mMap.addPolyline(new PolylineOptions().width(5).add(locationList.get(0)).add(locationList.get(5)).color(Color.RED));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        uid = intent.getIntExtra("uid", 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String requestURL = "https://people.cs.clemson.edu/~zwan/android/history.php";
                String charset = "UTF-8";
                try {
                    long currentTime = System.currentTimeMillis();
                    FileUploader multipart = new FileUploader(requestURL, charset);
                    multipart.addHeaderField("User-Agent", "CodeJava");
                    multipart.addHeaderField("Test-Header", "Header-Value");
                    multipart.addFormField("uid", String.valueOf(uid));
                    multipart.addFormField("time", String.valueOf(currentTime));

                    List<String> response = multipart.finish();

                    System.out.println("SERVER REPLIED:");
                    System.out.println(response);
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    message.obj = response.get(1);
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Show 7 days history");
                mMap.clear();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MapsActivity.HttpRequest httpRequest = new MapsActivity.HttpRequest();
                        long currentTime = System.currentTimeMillis();
                        Uri.Builder builder = new Uri.Builder()
                                .appendQueryParameter("UId", String.valueOf(uid))
                                .appendQueryParameter("Time", String.valueOf(currentTime));
                        String query = builder.build().getEncodedQuery();
                        String response;

                        try {
                            response = httpRequest.execute("https://people.cs.clemson.edu/~yuang/cpsc6820/Project/get_7days_history.php", query).get();
                            Log.d(TAG, "7 Days Response: " + response);
                            Message message = new Message();
                            message.what = SHOW_7DAYS_RESPONSE;
                            message.obj = response;
                            handler.sendMessage(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
