package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("lat", 0);
        longitude = intent.getDoubleExtra("long", 0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            Log.e("MapActivity", "SupportMapFragment is null");
        } else {
            Log.d("MapActivity", "SupportMapFragment found");
        }

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng eventLocation = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(eventLocation).title("Event Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15f));
    }
}
