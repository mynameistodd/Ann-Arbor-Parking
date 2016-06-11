package com.sliverbit.annarborparking;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

    private TextView locationName;
    private TextView locationAddress;
    private TextView locationTotalSpaces;
    private TextView locationHandicap;
    private TextView locationWeekend;
    private TextView locationQuickPay;
    private TextView locationValidation;
    private TextView locationHeightRestriction;
    private TextView locationPublic;
    private TextView locationMonthly;
    private TextView locationElectricCharging;
    private TextView locationBicycle;
    private TextView locationMoped;
    private TextView locationNote;

    private BottomSheetBehavior bottomSheetBehavior;
    private GoogleMap mMap;
    private MapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private RequestQueue queue;
    private Gson gson;

    private HashMap<String, Location> locations;
    private Availability[] availability;
    private HashMap<String, Marker> locationToMarkerHashMap;
    private HashMap<String, Location> markerToLocationHashMap;
    private IconGenerator iconGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        View bottomSheet = findViewById(R.id.locationDetailBottomSheet);
        locationName = (TextView) findViewById(R.id.locationName);
        locationAddress = (TextView) findViewById(R.id.locationAddress);
        locationTotalSpaces = (TextView) findViewById(R.id.locationTotalSpaces);
        locationHandicap = (TextView) findViewById(R.id.locationHandicap);
        locationWeekend = (TextView) findViewById(R.id.locationWeekend);
        locationQuickPay = (TextView) findViewById(R.id.locationQuickPay);
        locationValidation = (TextView) findViewById(R.id.locationValidation);
        locationHeightRestriction = (TextView) findViewById(R.id.locationHeightRestriction);
        locationPublic = (TextView) findViewById(R.id.locationPublic);
        locationMonthly = (TextView) findViewById(R.id.locationMonthly);
        locationElectricCharging = (TextView) findViewById(R.id.locationElectricCharging);
        locationBicycle = (TextView) findViewById(R.id.locationBicycle);
        locationMoped = (TextView) findViewById(R.id.locationMoped);
        locationNote = (TextView) findViewById(R.id.locationNote);

        if (savedInstanceState == null) {
            mapFragment = MapFragment.newInstance();

            getFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, mapFragment, TAG_MAP_FRAGMENT)
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
        locationToMarkerHashMap = new HashMap<>();
        markerToLocationHashMap = new HashMap<>();
        iconGenerator = new IconGenerator(this);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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
                        .replace(R.id.mapContainer, new SettingsFragment())
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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                String id = marker.getId();
                Location location = markerToLocationHashMap.get(id);

                locationName.setText(location.getLocation());
                locationAddress.setText(location.getAddress());
                locationTotalSpaces.setText(getString(R.string.total_spaces, location.getNumberOfSpaces()));
                locationHandicap.setText(getString(R.string.handicap_spaces, location.getHandicapSpaces()));
                locationWeekend.setText(getString(R.string.weekend_parking, location.getEveningsWkndParking()));
                locationQuickPay.setText(getString(R.string.quick_pay, location.getQuickPay()));
                locationValidation.setText(getString(R.string.validation, location.getDiscountCouponsAccepted()));
                locationHeightRestriction.setText(getString(R.string.height_restriction, location.getHgtRestriction()));
                locationPublic.setText(getString(R.string.public_parking, location.getPublicParking()));
                locationMonthly.setText(getString(R.string.monthly_parking, location.getMonthlyParking()));
                locationElectricCharging.setText(getString(R.string.electric_charging, location.geteVCharging()));
                locationBicycle.setText(getString(R.string.bicycle_parking, location.getBicycleParking()));
                locationMoped.setText(getString(R.string.moped_parking, location.getMopedParking()));
                locationNote.setText(getString(R.string.note, location.getNote()));

                bottomSheetBehavior.setPeekHeight(160);
                return true;
            }
        });

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
//        locationToMarkerHashMap.clear();
//
//        for (Location location : locations) {
//            LatLng lotLatLng = new LatLng(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
//            Marker locationMarker = mMap.addMarker(new MarkerOptions().position(lotLatLng).title(location.getLocation()));
//
//            locationToMarkerHashMap.put(location.getLocationCode(), locationMarker);
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
                        Marker marker;

                        if (locationToMarkerHashMap.containsKey(location.getLocationCode())) {
                            marker = locationToMarkerHashMap.get(location.getLocationCode());
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
                        } else {
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude())))
                                    .title(location.getLocation())
                                    .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));

                            locationToMarkerHashMap.put(location.getLocationCode(), marker);
                            markerToLocationHashMap.put(marker.getId(), location);
                        }
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
