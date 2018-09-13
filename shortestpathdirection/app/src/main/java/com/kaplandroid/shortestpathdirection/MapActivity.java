package com.kaplandroid.shortestpathdirection;

import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kaplandroid.shortestpathdirection.GMapV2Direction;
import com.kaplandroid.shortestpathdirection.GMapV2Direction.DirecitonReceivedListener;
import com.kaplandroid.shortestpathdirection.GetRotueListTask;
import com.google.android.gms.maps.SupportMapFragment;

import com.kaplandroid.shortestpathdirection.AutocompleteActivity;

public class MapActivity extends android.support.v4.app.FragmentActivity
        implements OnMapReadyCallback,OnClickListener, OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks,
        DirecitonReceivedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Button btnDirection;
    private LocationRequest mLocationRequest;
    private PendingResult<LocationSettingsResult> result;
    private LocationSettingsRequest.Builder builder;
    private Location mLastLocation;
    private final int REQUEST_LOCATION = 200;
    private final int REQUEST_CHECK_SETTINGS = 300;
    private final int REQUEST_GOOGLE_PLAY_SERVICE = 400;
    LatLng startPosition;
    String startPositionTitle;
    String startPositionSnippet;

    LatLng destinationPosition;
    String destinationPositionTitle;
    String destinationPositionSnippet;

    private final int[] TYPESOfMAPS = { GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_HYBRID, GoogleMap.MAP_TYPE_TERRAIN, GoogleMap.MAP_TYPE_NONE };
    ToggleButton tbMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startPosition = new LatLng(41.036896, 28.985490);
        startPositionTitle = "Taksim Square";
        startPositionSnippet = "Istanbul / Turkey";

        destinationPosition = new LatLng(41.005921, 28.977737);
        destinationPositionTitle = "Sultanahmet Mosque, Istanbul";
        destinationPositionSnippet = "Istanbul / Turkey";

        btnDirection = (Button) findViewById(R.id.btnDirection);
        btnDirection.setOnClickListener(this);

        tbMode = (ToggleButton) findViewById(R.id.tbMode);

        tbMode.setChecked(true);

        setUpMapIfNeeded();

    }

    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        mMap.setOnInfoWindowClickListener(this);

    }

    public void clearMap() {
        mMap.clear();
    }

    @Override
    public void onClick(View v) {
        if (v == btnDirection) {
            clearMap();
            Intent activityChangeIntent = new Intent(MapActivity.this, AutocompleteActivity.class);
            MapActivity.this.startActivity(activityChangeIntent);
            MarkerOptions mDestination = new MarkerOptions()
                    .position(destinationPosition)
                    .title(destinationPositionTitle)
                    .snippet(destinationPositionSnippet)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin1));

            MarkerOptions mStart = new MarkerOptions()
                    .position(startPosition)
                    .title(startPositionTitle)
                    .snippet(startPositionSnippet)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin2));

            mMap.addMarker(mDestination);
            mMap.addMarker(mStart);

            if (tbMode.isChecked()) {
                new GetRotueListTask(MapActivity.this, startPosition,
                        destinationPosition, GMapV2Direction.MODE_DRIVING, this)
                        .execute();
            } else {
                new GetRotueListTask(MapActivity.this, startPosition,
                        destinationPosition, GMapV2Direction.MODE_WALKING, this)
                        .execute();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates mState = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
// All location settings are satisfied. The client can
// initialize location requests here.
                        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                        } else {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            if (mMap != null) {
                                LatLng locationMarker = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(locationMarker).title("Current Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(locationMarker));
                            }
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
// Location settings are not satisfied, but this can be fixed
// by showing the user a dialog.
                        try {
// Show the dialog by calling startResolutionForResult(),
// and check the result in onActivityResult().
                            status.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
// Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
// Location settings are not satisfied. However, we have no way
// to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }
    @Override
    public void OnDirectionListReceived(List<LatLng> mPointList) {
        if (mPointList != null) {
            PolylineOptions rectLine = new PolylineOptions().width(10).color(
                    Color.RED);
            for (int i = 0; i < mPointList.size(); i++) {
                rectLine.add(mPointList.get(i));
            }
            mMap.addPolyline(rectLine);

            CameraPosition mCPFrom = new CameraPosition.Builder()
                    .target(startPosition).zoom(15.5f).bearing(0).tilt(25)
                    .build();
            final CameraPosition mCPTo = new CameraPosition.Builder()
                    .target(destinationPosition).zoom(15.5f).bearing(0)
                    .tilt(50).build();

            changeCamera(CameraUpdateFactory.newCameraPosition(mCPFrom),
                    new CancelableCallback() {
                        @Override
                        public void onFinish() {
                            changeCamera(CameraUpdateFactory
                                            .newCameraPosition(mCPTo),
                                    new CancelableCallback() {

                                        @Override
                                        public void onFinish() {

                                            LatLngBounds bounds = new LatLngBounds.Builder()
                                                    .include(startPosition)
                                                    .include(
                                                            destinationPosition)
                                                    .build();
                                            changeCamera(
                                                    CameraUpdateFactory
                                                            .newLatLngBounds(
                                                                    bounds, 50),
                                                    null, false);
                                        }

                                        @Override
                                        public void onCancel() {
                                        }
                                    }, false);
                        }

                        @Override
                        public void onCancel() {
                        }
                    }, true);
        }
    }

    /**
     * Change the camera position by moving or animating the camera depending on
     * input parameter.
     */
    private void changeCamera(CameraUpdate update, CancelableCallback callback,
                              boolean instant) {

        if (instant) {
            mMap.animateCamera(update, 1, callback);
        } else {
            mMap.animateCamera(update, 4000, callback);
        }
    }

    @Override
    public void onInfoWindowClick(Marker selectedMarker) {

        if (selectedMarker.getTitle().equals(startPositionTitle)) {
            Toast.makeText(this, "Marker Clicked: " + startPositionTitle,
                    Toast.LENGTH_LONG).show();
        } else if (selectedMarker.getTitle().equals(destinationPositionTitle)) {
            Toast.makeText(this, "Marker Clicked: " + destinationPositionTitle,
                    Toast.LENGTH_LONG).show();
        }
        selectedMarker.hideInfoWindow();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
