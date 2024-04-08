package com.example.mergefault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String placeId;
    private String eventId;
    private String organizerId;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private LatLng checkedInLatLng;
    private GoogleMap googleMap;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        placesClient = Places.createClient(this);

        Intent intent = getIntent();
        placeId = intent.getStringExtra("placeID");
        eventId = intent.getStringExtra("EventId");
        organizerId = intent.getStringExtra("OrganizerID");
        String eventPosterUri = intent.getStringExtra("eventPosterUri");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            Log.e("MapActivity", "SupportMapFragment is null");
        } else {
            Log.d("MapActivity", "SupportMapFragment found");
        }

        mapFragment.getMapAsync(this);

        eventRef.document(eventId).collection("attendees").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        eventRef.document(eventId).collection("attendees").document(document.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.get("CheckInLocation") != null) {
                                    String checkedInLocation = documentSnapshot.getString("CheckInLocation");
                                    if (checkedInLocation != null) {
                                        String[] latlng = checkedInLocation.split(",");

                                        if (latlng.length == 2) {
                                            try {
                                                String[] latval = latlng[0].split(":");
                                                String[] longval = latlng[1].split(":");
                                                double latitude = Double.parseDouble(latval[1].trim());
                                                double longitude = Double.parseDouble(longval[1].trim());
                                                checkedInLatLng = new LatLng(latitude, longitude);
                                                addCheckedInMarkerToMap(checkedInLatLng, googleMap);
                                            } catch (NumberFormatException e) {
                                                Log.e("MapActivity", "Error parsing latitude and longitude: " + e.getMessage());
                                            }
                                        } else {
                                            Log.e("MapActivity", "Invalid checked-in location format");
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(MapActivity.this, OrganizerEventOptions.class);
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        };
        MapActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    /**
     * Callback method for when the map is ready to be used.
     *
     * @param googleMap The GoogleMap object representing the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

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


    /**
     * Method to add a checked-in marker to the map.
     *
     * @param checkedInLatLng The LatLng object representing the location of the checked-in user.
     * @param googleMap       The GoogleMap object representing the map.
     */
    private void addCheckedInMarkerToMap(LatLng checkedInLatLng, GoogleMap googleMap) {
        if (googleMap != null && checkedInLatLng != null) {
            // Create MarkerOptions for the checked-in location
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_location);

            // Define the desired width and height of the marker icon
            int width = 100;
            int height = 100;

            // Resize the bitmap to the desired dimensions
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);

            // Create MarkerOptions for the checked-in location with custom marker
            MarkerOptions checkedInMarkerOptions = new MarkerOptions()
                    .position(checkedInLatLng)
                    .title("Checked In Location")
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

            // Add marker to the map
            googleMap.addMarker(checkedInMarkerOptions);
        }
    }
}
