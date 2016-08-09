package novitskyvitaly.geogroup;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import Connectivity.VolleySingleton;

public class MapMainActivity extends AppCompatActivity
        implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String MY_TAG = "geog_main_map_act";

    private static final int REQUEST_CODE_ASK_LOCATION_PERMISSION = 10;

    DrawerLayout drawer;
    Toolbar toolbar;

    boolean isSideMenuOpened;

    SupportMapFragment mapFragment;
    GoogleMap googleMap;
    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private int mLocationRequestInterval = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_main);

        InitToolbar();
        InitDrawerSideMenu();
        InitButtons();
        buildGoogleApiClient();

        if (googleMap == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
            if (mapFragment != null) mapFragment.getMapAsync(this);
        }

        GCMInstanceIDListenerService.StartRegisterToGCM(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_main, menu);
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

    private void InitToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void InitDrawerSideMenu() {
        drawer = (DrawerLayout) findViewById(R.id.drl_side_menu);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                isSideMenuOpened = false;
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                isSideMenuOpened = true;
            }
        };
        if (drawer != null) drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void InitButtons() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_geo_group);
        fab.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_geo_group:

                break;
        }
    }

    private void SendPush(String msg){
        JSONObject jsonObjectHeader = new JSONObject();
        try {
            jsonObjectHeader.put("to", "/topics/topic1");
            JSONObject jsonObjectData = new JSONObject();
            jsonObjectData.put("message", "hello");
            jsonObjectHeader.put("data", jsonObjectData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getString(R.string.google_gcm_server_url), jsonObjectHeader,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(MY_TAG, response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        VolleySingleton.getmInstance(this).addToRequestQueue(request);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_ASK_LOCATION_PERMISSION);
        } else SetMapProperties();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_ASK_LOCATION_PERMISSION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        SetMapProperties();
                        break;
                }
                break;
        }
    }

    private void SetMapProperties() {
        if (googleMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMarkerClickListener(this);
            googleMap.setOnInfoWindowClickListener(this);
            //googleMap.setOnMyLocationChangeListener(this);
            //googleMap.setInfoWindowAdapter(new MapMarkerInfoWindowAdapter(getLayoutInflater()));
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(mLocationRequestInterval);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(MY_TAG, "got location: lat: " + String.valueOf(location.getLatitude()) + ", long: " + String.valueOf(location.getLongitude()));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void sendUpstreamGCM() {
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action", "SAY_HELLO");
                    gcm.send(getString(R.string.sender_id) + "@gcm.googleapis.com", "msg1", data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute(null, null, null);
    }
}
