package com.example.mergefault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String placeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        placeId = intent.getStringExtra("placeID");
        String eventPosterUri = intent.getStringExtra("eventPosterUri");


        String apiKey = BuildConfig.PLACES_API_KEY;

        // Initialize Places SDK

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
        PlacesClient placesClient = Places.createClient(this);

        // Define fields to fetch
        List<Field> placeFields = Arrays.asList(Field.LAT_LNG);

        // Construct a request object, passing the place ID and fields array
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        // Perform the request
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            // Extract latitude and longitude from the place
            LatLng location = place.getLatLng();
            if (location != null) {
                // Load the custom marker icon as a Bitmap
                Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.event_location);

                // Define the desired width and height of the marker icon (e.g., 50x50 pixels)
                int width = 150;
                int height = 150;

                // Resize the bitmap to the desired dimensions
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

                // Create a MarkerOptions object and set the resized bitmap as the marker icon
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(place.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

                // Place a marker at the location
                googleMap.addMarker(markerOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            }
        }).addOnFailureListener((exception) -> {
            Log.e("MapActivity", "Place not found: " + exception.getMessage());
        });
    }


}
