package com.etoitau.whereami;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * App to show your "home" location, your current location, and the furthest you've been from home
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    final long MIN_TIME = 10000; // milliseconds
    final float MIN_DIST = 200; // meters
    private Marker mFurthest, mCurrent, mHome;
    float maxDist = 0; // meters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define "home" location. This ought to be set by user, but for this experiment simply hardcoding
        LatLng home = new LatLng(37.770031, -122.466037);

        mHome = mMap.addMarker(new MarkerOptions().position(home).title("Home"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(home));

        // once map is loaded, start listening and updating
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                buildLocListener();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            }
        }
    }

    /**
     * if user moves, update now location and furthest location if needed
     */
    private void buildLocListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (mCurrent != null)
                    mCurrent.remove();
                Log.i("Location",location.toString());
                LatLng currentLL = locationToLatLng(location);
                float currentDist = markerLocation(mHome).distanceTo(location);
                mCurrent = mMap.addMarker(new MarkerOptions().position(currentLL).title("Now"));
                if (currentDist > maxDist) {
                    if (mFurthest != null) {
                        mFurthest.remove();
                    }
                    maxDist = currentDist;
                    mFurthest = mMap.addMarker(new MarkerOptions().position(currentLL).title("Farthest"));
                }
                goToMarkers();
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        }

    }

    // helper to convert Location object to LatLng
    private LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    // helper to convert LatLng object to Location
    private Location latLngToLocation(LatLng ll) {
        Location loc = new Location("");
        loc.setLatitude(ll.latitude);
        loc.setLongitude(ll.longitude);
        return loc;
    }

    // helper to convert Marker object to Location
    private Location markerLocation(Marker m) {
        return latLngToLocation(m.getPosition());
    }

    // adjust view to show all three markers
    private void goToMarkers() {
        int count = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker[] mArray = {mHome, mCurrent, mFurthest};
        for (Marker mk : mArray) {
            if (mk != null) {
                builder.include(mk.getPosition());
                count++;
            }
        }
        if (count > 1) {
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
            mMap.moveCamera(cu);
        }
    }
}
