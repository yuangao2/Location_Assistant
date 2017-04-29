package com.example.zwan.a4;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Person>,
        ClusterManager.OnClusterInfoWindowClickListener<Person>,
        ClusterManager.OnClusterItemClickListener<Person>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Person>,
        LocationListener,
        ResultCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    public static final int SHOW_RESPONSE = 0;
    public static final int SHOW_PIC = 1;
    private GoogleMap mMap;
    private int uid;
    private int fid;
    private String invitation;
    private TextView uname;
    private ImageView profile;
    private int flag=0;
    private Map<Integer, Bitmap> pics = new HashMap<Integer, Bitmap>();
    private Map<Integer, String> names = new HashMap<Integer, String>();
    //private int currentUId;
    private GoogleApiClient googleApiClient;
    //private ArrayList<Target> targetArrayList = new ArrayList<Target>();
    private static final String TAG = MapsActivity.class.getSimpleName();
    private Marker geoFenceMarker;


    private ClusterManager<Person> mClusterManager;

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
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        //Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
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

    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {

    }

    @Override
    public boolean onClusterItemClick(Person item) {

        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        intent.putExtra("uid", item.getUId());
        startActivity(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Result result) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            mImageView.setImageBitmap(person.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = new BitmapDrawable(getResources(), p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    Log.e("response", response);
                    if(response.equals("fail")){
                        Toast.makeText(getApplicationContext(), "Update faileds.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //mMap.clear();
                        mClusterManager.clearItems();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int thisuid = jsonObject.getInt("UId");
                                double latitude = jsonObject.getDouble("Latitude");
                                double longitude = jsonObject.getDouble("Longitude");
                                Log.e("uid", String.valueOf(thisuid));
                                Log.e("latitude", String.valueOf(latitude));
                                Log.e("longitude", String.valueOf(longitude));
                                LatLng location = new LatLng(latitude,longitude);
                                if(thisuid == uid) {
                                    if(flag==0) {
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                                        uname.setText(names.get(uid));
                                        profile.setImageBitmap(pics.get(uid));
                                        flag++;
                                    }
                                    mClusterManager.addItem(new Person(thisuid, location, "Me", pics.get(thisuid)));
                                    //mMap.addMarker(new MarkerOptions().position(location).title("Me").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                }
                                else{
                                    mClusterManager.addItem(new Person(thisuid, location, names.get(thisuid), pics.get(thisuid)));
                                    //mMap.addMarker(new MarkerOptions().position(location).title("UId:"+String.valueOf(thisuid)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                }
                            }
                            mClusterManager.cluster();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case SHOW_PIC:
                    //Bitmap pic = (Bitmap) msg.obj;
                    //profile.setImageBitmap(pic);
                    String response1 = (String) msg.obj;
                    Log.e("response1", response1);
                    try {
                        JSONArray jsonArray = new JSONArray(response1);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            final int thisuid = jsonObject.getInt("UId");
                            final String pic = jsonObject.getString("Picture");
                            String name = jsonObject.getString("Username");
                            names.put(thisuid, name);
                            //Log.e(String.valueOf(currentUId), pic);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        pics.put(thisuid, Picasso.with(getApplicationContext()).load("https://people.cs.clemson.edu/~zwan/android/"+pic).resize(200,200).get());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }
    };


    private boolean runtime_permission(){
        if(Build.VERSION.SDK_INT>=23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        else{
            return false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get login user information
        Intent intent = getIntent();
        //uid = intent.getIntExtra("uid", 0);
        //fid = intent.getIntExtra("fid", 0);
        //invitation = intent.getStringExtra("invitation");
        SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
        uid = pref.getInt("uid", 0);
        fid = pref.getInt("fid", 0);
        invitation = pref.getString("invitation", "");

        Log.e("uid", String.valueOf(uid));
        Log.e("fid", String.valueOf(fid));
        Log.e("invitation", invitation);

        //start upload service
        if(!runtime_permission()) {
            Intent serviceIntent = new Intent(this, UploadService.class);
            serviceIntent.putExtra("uid", uid);
            startService(serviceIntent);
        }

        //download family pictures
        new Thread(new Runnable() {
            @Override
            public void run() {
                String requestURL1 = "https://people.cs.clemson.edu/~zwan/android/downloadpics.php";
                String charset = "UTF-8";
                try {
                    FileUploader multipart1 = new FileUploader(requestURL1, charset);
                    multipart1.addHeaderField("User-Agent", "CodeJava");
                    multipart1.addHeaderField("Test-Header", "Header-Value");
                    multipart1.addFormField("fid", String.valueOf(fid));

                    List<String> response1 = multipart1.finish();

                    System.out.println("SERVER REPLIED:");
                    Message message1 = new Message();
                    message1.what = SHOW_PIC;
                    message1.obj = response1.get(1);
                    handler.sendMessage(message1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createGoogleApi();
        Intent intent1 = new Intent(this, RegistrationService.class);
        startService(intent1);

        final Handler handlerdelay=new Handler();
        handlerdelay.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String requestURL = "https://people.cs.clemson.edu/~zwan/android/getlocation.php";
                        String charset = "UTF-8";
                        try {
                            FileUploader multipart = new FileUploader(requestURL, charset);
                            multipart.addHeaderField("User-Agent", "CodeJava");
                            multipart.addHeaderField("Test-Header", "Header-Value");
                            multipart.addFormField("fid", String.valueOf(fid));

                            List<String> response = multipart.finish();

                            System.out.println("SERVER REPLIED:");
                            /*
                            for (String line : response) {
                                Log.e("response", line);
                            }*/
                            //System.out.println(response.get(1));
                            //Log.e("response", response.get(1));
                            Message message = new Message();
                            message.what = SHOW_RESPONSE;
                            message.obj = response.get(1);
                            handler.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Log.e("delay", "delay");
                handlerdelay.postDelayed(this, 10000);
            }
        }, 1500);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView =  navigationView.getHeaderView(0);
        uname = (TextView) hView.findViewById(R.id.usernameTextView);
        profile = (ImageView) hView.findViewById(R.id.imageView);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_copy) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("invitation", invitation);
            cm.setPrimaryClip(mClipData);
            Toast.makeText(getApplicationContext(), "Copied invitation code to clipboard.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_map) {

        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_geofence) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_message) {
            Intent intent = new Intent(MainActivity.this, GroupChat.class);
            startActivity(intent);

        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(MainActivity.this, LocationInfoActivity.class);
            startActivity(intent);

        }
        /*else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                Intent serviceIntent = new Intent(this, UploadService.class);
                startService(serviceIntent);
            }
            else{
                runtime_permission();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mClusterManager = new ClusterManager<Person>(this, mMap);
        mClusterManager.setRenderer(new PersonRenderer());
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        // Add a marker in Sydney and move the camera

        //LatLng clemson = new LatLng(34.6711669,-82.8387079);
        //mMap.addMarker(new MarkerOptions().position(clemson).title("Clemson University").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(clemson, 13));

    }
}
