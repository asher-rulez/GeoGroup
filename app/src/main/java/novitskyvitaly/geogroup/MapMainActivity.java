package novitskyvitaly.geogroup;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import Connectivity.VolleySingleton;
import DataModel.LocationReport;
import DataModel.MyGeoGroup;
import Util.CommonUtil;
import Util.GeoGroupBroadcastReceiver;
import Util.IBroadcastReceiverCallback;

public class MapMainActivity extends AppCompatActivity
        implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        IBroadcastReceiverCallback {

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

    GeoGroupBroadcastReceiver broadcastReceiver;

    Firebase firebaseReference;
    String firebaseCurrentValue;

    LatLng myCurrentLocation = null;
    Long lastTimeSentUpdate = null;

    ProgressDialog progressDialog = null;

    FloatingActionButton fab_create;

//    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference("https://geogroup-5241a.firebaseio.com");
//    DatabaseReference groupNamesReference = mRootReference.child("GeoGroupNamesArray");

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

        firebaseReference = new Firebase(getString(R.string.firebase_connection_string) + "/GeoGroupNamesArray");
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
    protected void onResume() {
        CommonUtil.SetIsApplicationRunningInForeground(this, true);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        if(broadcastReceiver != null){
            broadcastReceiver = new GeoGroupBroadcastReceiver(this);
            IntentFilter filter = new IntentFilter(GeoGroupBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
            registerReceiver(broadcastReceiver, filter);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        CommonUtil.SetIsApplicationRunningInForeground(this, false);
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        super.onPause();
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
        fab_create = (FloatingActionButton)findViewById(R.id.fab_create_group);
        fab_create.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_geo_group:
                //firebaseReference.setValue("ok");


                break;
            case R.id.fab_create_group:
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.loading_checking_group_names_array));
                firebaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        firebaseCurrentValue = dataSnapshot.getValue().toString();
                        HashSet<String> namesSet = null;
                        if(firebaseCurrentValue.length() != 0) {
                            String namesArray[] = firebaseCurrentValue.split(",");
                            namesSet = new HashSet<String>();
                            for (String name : namesArray)
                                namesSet.add(name);
                        }
                        firebaseReference.removeEventListener(this);
                        OpenCreateGroupDialog(namesSet);
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        firebaseReference.removeEventListener(this);
                    }
                });
        }
    }

    private void OpenCreateGroupDialog(final HashSet<String> existingGroupNames){
        final Context ctx = this;
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.enter_new_group_name_dialog);
        final EditText et_group_name = (EditText)dialog.findViewById(R.id.et_new_group_name);
        final TextView tv_group_name_validation = (TextView)dialog.findViewById(R.id.tv_new_group_name_validation);
        final Button btn_ok = (Button)dialog.findViewById(R.id.btn_new_group_ok);
        final Button btn_cancel = (Button)dialog.findViewById(R.id.btn_new_group_cancel);

        et_group_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                CommonUtil.RemoveValidationFromEditText(ctx, et_group_name);
                tv_group_name_validation.setText(editable.length() > 2 ? "" : getString(R.string.group_name_validation_min_lenght));
                btn_ok.setEnabled(editable.length() > 2);
            }
        });

        btn_ok.setEnabled(false);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!et_group_name.getText().toString().matches(getString(R.string.regex_group_name))){
                    CommonUtil.SetEditTextIsValid(ctx, et_group_name, false);
                    tv_group_name_validation.setText(getString(R.string.group_name_validation_symbols));
                    return;
                }
                if(existingGroupNames != null && existingGroupNames.contains(et_group_name.getText().toString())){
                    CommonUtil.SetEditTextIsValid(ctx, et_group_name, false);
                    tv_group_name_validation.setText(getString(R.string.group_name_validation_already_exists));
                    return;
                }
                CommonUtil.SetEditTextIsValid(ctx, et_group_name, true);
                CreateNewGroup(et_group_name.getText().toString());
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(progressDialog != null)
                    progressDialog.dismiss();
            }
        });
        dialog.show();
    }

    private void CreateNewGroup(final String groupName){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                firebaseCurrentValue = (firebaseCurrentValue == null ? "" : firebaseCurrentValue + ",") + groupName;
                firebaseReference.setValue(firebaseCurrentValue);
                firebaseCurrentValue = null;
                MyGeoGroup group = new MyGeoGroup();
                group.set_name(groupName);
                group.set_is_active(true);
                group.set_is_mine(true);
                getContentResolver().insert(GeoGroupSQLiteContentProvider.URI_MY_GEOGROUP, group.GetContentValuesRow());
                GcmPubSub gcmPubSub = GcmPubSub.getInstance(getApplicationContext());
                try {
                    gcmPubSub.subscribe(CommonUtil.GetGCMToken(getApplicationContext()), "/topics/" + groupName, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                OnGroupCreatedOrJoined();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void OnGroupCreatedOrJoined(){
        SendLocationReportToGroups(myCurrentLocation);
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
        //Log.i(MY_TAG, "got location: lat: " + String.valueOf(location.getLatitude()) + ", long: " + String.valueOf(location.getLongitude()));
        CommonUtil.SaveLocationInSharedPreferences(this, location.getLatitude(), location.getLongitude(), new Date());
        if(myCurrentLocation == null)
            AnimateCameraFocusOnLatLng(new LatLng(location.getLatitude(), location.getLongitude()), null);
        myCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//        long a1 = new Date().getTime();
//        long a2  = CommonUtil.GetLastLocationSavedDateTimeInMillis(this);
//        long b = ;
        if(lastTimeSentUpdate == null || new Date().getTime() - lastTimeSentUpdate > CommonUtil.GetLocationRefreshFrequency(this)){
            lastTimeSentUpdate = new Date().getTime();
            SendLocationReportToGroups(myCurrentLocation);
        }
    }

    private void AnimateCameraFocusOnLatLng(LatLng latLng, @Nullable Integer zoomLevel){
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel == null ? 15 : zoomLevel);
        googleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //not in use
//    private void sendUpstreamGCM() {
//        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... params) {
//                String msg = "";
//                try {
//                    Bundle data = new Bundle();
//                    data.putString("my_message", "Hello World");
//                    data.putString("my_action", "SAY_HELLO");
//                    gcm.send(getString(R.string.sender_id) + "@gcm.googleapis.com", "msg1", data);
//                    msg = "Sent message";
//                } catch (IOException ex) {
//                    msg = "Error :" + ex.getMessage();
//                }
//                return msg;
//            }
//
//            @Override
//            protected void onPostExecute(String msg) {
//            }
//        }.execute(null, null, null);
//    }

    @Override
    public void onBroadcastReceived(Intent intent) {

    }

    private void SendLocationReportToGroups(final LatLng location){
        final Context ctx = this;
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                Cursor cursor = ctx.getContentResolver()
                        .query(GeoGroupSQLiteContentProvider.URI_MY_GEOGROUP,
                                MyGeoGroup.GetColumnNamesArray(),
                                MyGeoGroup.MY_GEOGROUP_IS_ACTIVE_KEY + " = 1", null, null);
                ArrayList<MyGeoGroup> myGroups = MyGeoGroup.GetMyGeoGroupsListFromCursor(cursor);
                if(myGroups == null || myGroups.size() == 0)
                    return null;
                for(MyGeoGroup group : myGroups){
                    JSONObject info
                            = LocationReport.GetJSONObjectForPush(CommonUtil.GetAndroidID(ctx),
                                                                    CommonUtil.GetMyNickname(ctx),
                                                                    group.get_name(),
                                                                    "",
                                                                    location.latitude,
                                                                    location.longitude,
                                                                    new Date().getTime());
                    SendPush(info.toString(), group.get_name());
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void SendPush(String msg, String groupName){
        JSONObject jsonObjectHeader = new JSONObject();
        try {
            jsonObjectHeader.put("to", "/topics/" + groupName);
            JSONObject jsonObjectData = new JSONObject();
            jsonObjectData.put("message", msg);
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

}
