package com.sp.harvesthub.nav_fragment;

<<<<<<< HEAD
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
=======
>>>>>>> renzo
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
<<<<<<< HEAD
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sp.harvesthub.R;
import com.sp.harvesthub.activities.Location1Activity;
import com.sp.harvesthub.activities.Location2Activity;
import com.sp.harvesthub.activities.Location3Activity;
import com.sp.harvesthub.activities.Location4Activity;
import java.util.HashMap;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    
    private GoogleMap mMap;
    private HashMap<String, Class<?>> markerActivities;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        
        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Initialize the marker-to-activity map
        markerActivities = new HashMap<>();
        return rootView;
    }

    private BitmapDescriptor getBitmapFromVector() {
        Drawable vectorDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.arrow);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void addMarkers() {
        // Get bitmap descriptor for the marker icon
        BitmapDescriptor markerIcon = getBitmapFromVector();

        // Define locations with correct coordinates
        LatLng bedok = new LatLng(1.3375, 103.9188);     // Bedok Block 702 (Southeast)
        LatLng punggol = new LatLng(1.4022, 103.9111);   // Punggol
        LatLng woodlands = new LatLng(1.4418, 103.8012); // Woodlands
        LatLng teckWhye = new LatLng(1.3900, 103.7551);  // Teck Whye

        // Add marker for Bedok (Location1)
        Marker marker1 = mMap.addMarker(new MarkerOptions()
                .position(bedok)
                .title("Bedok Block 702 Community Fridge")
                .icon(markerIcon));
        if (marker1 != null) {
            markerActivities.put(marker1.getId(), Location1Activity.class);
        }

        // Add marker for Punggol (Location2)
        Marker marker2 = mMap.addMarker(new MarkerOptions()
                .position(punggol)
                .title("Punggol Community Fridge")
                .icon(markerIcon));
        if (marker2 != null) {
            markerActivities.put(marker2.getId(), Location2Activity.class);
        }

        // Add marker for Woodlands (Location3)
        Marker marker3 = mMap.addMarker(new MarkerOptions()
                .position(woodlands)
                .title("Woodlands Community Fridge")
                .icon(markerIcon));
        if (marker3 != null) {
            markerActivities.put(marker3.getId(), Location3Activity.class);
        }

        // Add marker for Teck Whye (Location4)
        Marker marker4 = mMap.addMarker(new MarkerOptions()
                .position(teckWhye)
                .title("Teck Whye Community Fridge")
                .icon(markerIcon));
        if (marker4 != null) {
            markerActivities.put(marker4.getId(), Location4Activity.class);
        }

        // Center the camera on Singapore with appropriate zoom level
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(1.3521, 103.8198), 11));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permissions
        if (ActivityCompat.checkSelfPermission(requireContext(), 
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        
        mMap.setMyLocationEnabled(true);

        // Add markers with icons and set click listeners
        addMarkers();
        mMap.setOnMarkerClickListener(marker -> {
            // Navigate to the corresponding activity when a marker is clicked
            Class<?> activityClass = markerActivities.get(marker.getId());
            if (activityClass != null) {
                Intent intent = new Intent(requireContext(), activityClass);
                startActivity(intent);
            }
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
        @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
=======
import androidx.fragment.app.Fragment;
import com.sp.harvesthub.R;

public class MapFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
>>>>>>> renzo
    }
} 