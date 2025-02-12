package com.sp.harvesthubmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;


public class googleMaps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap<String, Class<?>> markerActivities; // Map markers to activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        // Initialize the marker-to-activity map
        markerActivities = new HashMap<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Add markers with icons and set click listeners
        addMarkers();
        mMap.setOnMarkerClickListener(marker -> {
            // Navigate to the corresponding activity when a marker is clicked
            Class<?> activityClass = markerActivities.get(marker.getId());
            if (activityClass != null) {
                Intent intent = new Intent(googleMaps.this, activityClass);
                startActivity(intent);
            }
            return true;
        });
    }

    private void addMarkers() {

        // Scale the drawable image
        Bitmap smallMarker = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.arrow),
                200, // Width in pixels
                100, // Height in pixels
                false // Filter for smoother scaling
        );

        // Define locations
        LatLng location1 = new LatLng(1.4022, 103.9111); // Punggol
        LatLng location2 = new LatLng(1.3375, 103.9188); // Bedok block 702
        LatLng location3 = new LatLng(1.4418, 103.8012); // WOODLANDS
        LatLng location4 = new LatLng(1.3900, 103.7551); // TECK WHYE

        // Add marker for location 1
        Marker marker1 = mMap.addMarker(new MarkerOptions()
                .position(location1)
                .title("Location 1")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))); // Use the scaled bitmap
        markerActivities.put(marker1.getId(), Location1Activity.class); // Map marker to activity

        // Add marker for location 2
        Marker marker2 = mMap.addMarker(new MarkerOptions()
                .position(location2)
                .title("Location 2")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))); // Use the scaled bitmap
        markerActivities.put(marker2.getId(), Location2Activity.class); // Map marker to activity

        // Add marker for location 3
        Marker marker3 = mMap.addMarker(new MarkerOptions()
                .position(location3)
                .title("Location 3")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))); // Use the scaled bitmap
        markerActivities.put(marker3.getId(), Location3Activity.class); // Map marker to activity

        // Add marker for location 4
        Marker marker4 = mMap.addMarker(new MarkerOptions()
                .position(location4)
                .title("Location 4")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))); // Use the scaled bitmap
        markerActivities.put(marker4.getId(), Location4Activity.class); // Map marker to activity

        // Zoom the camera to show all markers
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 12));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // This is required!

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }
}