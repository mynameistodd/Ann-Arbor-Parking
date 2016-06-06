package com.sliverbit.annarborparking;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.maps.android.ui.IconGenerator;
import com.sliverbit.annarborparking.models.Availability;
import com.sliverbit.annarborparking.models.Location;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapsActivity";
    private static final String TAG_MAP_FRAGMENT = "mapFragment";
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private MapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private RequestQueue queue;
    private Gson gson;

    private HashMap<String, Location> locations;
    private Availability[] availability;
    private HashMap<String, Marker> markerHashMap;
    private IconGenerator iconGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (savedInstanceState == null) {
            mapFragment = MapFragment.newInstance();

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, mapFragment, TAG_MAP_FRAGMENT)
                    .commit();
        } else {
            mapFragment = (MapFragment) getFragmentManager().findFragmentByTag(TAG_MAP_FRAGMENT);
        }

        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        queue = Volley.newRequestQueue(this);
        gson = new Gson();
        locations = new HashMap<>();
        markerHashMap = new HashMap<>();
        iconGenerator = new IconGenerator(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        StringRequest locationsRequest = new StringRequest("http://payment.rpsa2.com/Location%20and%20Rates/Locations%20and%20Rates.csv", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String line = null;
                BufferedReader bufferedReader = new BufferedReader(new StringReader(response));
                try {
                    line = bufferedReader.readLine(); //read the header to advance the buffer.

                    while ((line = bufferedReader.readLine()) != null) {
                        Log.d(TAG, line);
                        String[] fields = line.split(",", -1);
                        Location location = new Location(fields);
                        locations.put(location.getLocationCode(), location);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                loadLocations();
                refresh();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(locationsRequest);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_settings:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new SettingsFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
            default:
                return super.onOptionsItemSelected(item);

        }
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        UiSettings settings = mMap.getUiSettings();

        settings.setAllGesturesEnabled(true);
        settings.setCompassEnabled(true);
        settings.setMapToolbarEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setZoomControlsEnabled(true);

//        loadLocations();
//        refresh();
    }

//    private void loadLocations() {
//        if (mMap == null || locations.size() < 1) return;
//
//        markerHashMap.clear();
//
//        for (Location location : locations) {
//            LatLng lotLatLng = new LatLng(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
//            Marker locationMarker = mMap.addMarker(new MarkerOptions().position(lotLatLng).title(location.getLocation()));
//
//            markerHashMap.put(location.getLocationCode(), locationMarker);
//        }
//    }

    private void refresh() {

        JsonArrayRequest availabilityRequest = new JsonArrayRequest("http://payment.rpsa2.com/LocationAndRate/GetAvailableSpaces", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                availability = gson.fromJson(response.toString(), Availability[].class);
                for (Availability avail : availability) {

                    Location location = locations.get(avail.getFacility());
                    if (location != null) {

                        double availSpaces = Double.parseDouble(avail.getSpacesavailable());
                        double totalSpaces = Double.parseDouble(location.getNumberOfSpaces());
                        double percentFree = availSpaces / totalSpaces;
                        Log.d(TAG, "avail:" + availSpaces + " total:" + totalSpaces + " ratio:" + percentFree);

                        //TODO: Make break points configurable
                        if (percentFree >= .80) {
                            iconGenerator.setStyle(IconGenerator.STYLE_GREEN);
                        } else if (percentFree <= .20) {
                            iconGenerator.setStyle(IconGenerator.STYLE_RED);
                        } else {
                            iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
                        }

                        Bitmap iconBitmap = iconGenerator.makeIcon(avail.getSpacesavailable());
                        //TODO: Keep track of this marker and reuse on refresh. Adding multiple over and over is bad.
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude())))
                                .title(location.getLocation())
                                .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });


        queue.add(availabilityRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    mMap.setMyLocationEnabled(true);
                    mGoogleApiClient.reconnect();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        android.location.Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
