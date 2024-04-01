package com.example.mergefault;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

/**
 * Activity for attendee check-in at an event.
 */
public class AttendeeCheckInScreenActivity extends AppCompatActivity {
    // Request code for location permission
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Event ID
    private String eventId;

    // TextView to display location information
    private TextView locationText;

    // FusedLocationProviderClient for accessing device location
    private FusedLocationProviderClient fusedLocationClient;

    private Button checkInButton;
    private Button cancelButton;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference attendeeRef;
    private Integer checkedInCount;
    private Boolean attendeeCheckedIn = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_check_in_screen);

        checkInButton = findViewById(R.id.checkInButton);
        cancelButton = findViewById(R.id.cancelButton);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // Initialize location text view
        locationText = findViewById(R.id.location);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Handle incoming intent data
        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null && "myapp".equals(uri.getScheme()) && "www.lotuseventscheckin.com".equals(uri.getHost())) {
            eventId = uri.getQueryParameter("eventId");

            // Change later
            TextView descriptionText = findViewById(R.id.description);
            descriptionText.setText(eventId);
        }
        attendeeRef = eventsRef.document(eventId).collection("attendees");

        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore", error.toString());
                            return;
                        }
                        if (value != null){
                            for(QueryDocumentSnapshot doc: value) {
                                if(doc.getId() == sharedPreferences.getString("phonenumber", "")) {
                                    onCheckInButtonClick(v);
                                    CheckInAttendee();
                                    attendeeCheckedIn = true;
                                    break;
                                }
                            }
                        }
                    }
                });
                if (!attendeeCheckedIn) {
                    Toast.makeText(getApplicationContext(), "Error: You have not signed up to this event", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AttendeeCheckInScreenActivity.this, AttendeeHomeActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Method to handle the button click event for check-in.
     *
     * @param view The View that was clicked.
     */
    public void onCheckInButtonClick(View view) {
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, start location retrieval
            getLocation();

        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location retrieval
                getLocation();
            } else {
                Toast.makeText(this, "Do not have permission to access location. Please check in settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to retrieve the location
    private void getLocation() {
        // Check for either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Location retrieved, now record it
                            recordLocation(location);
                        } else {
                            Toast.makeText(this, "No last known location", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        // Handle failure to get location
                        Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Method to record the location
    private void recordLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        String locationInfo = "Latitude: " + latitude + ", Longitude: " + longitude;

        // Update the location text view
        if (locationText != null) {
            locationText.setText(locationInfo);
        } else {
            // Log an error if the TextView is not found
            Toast.makeText(this, "This view doesn't exist", Toast.LENGTH_SHORT).show();
        }
    }

    private void CheckInAttendee() {
        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null){
                    for(QueryDocumentSnapshot doc: value) {
                        if(doc.getId() == sharedPreferences.getString("phonenumber", "")) {
                            checkedInCount = Integer.parseInt(doc.getString("CheckedInCount"));
                            checkedInCount += 1;
                            break;
                        }
                    }
                }
            }
        });
        HashMap<String, Object> data = new HashMap<>();
        data.put("AttendeeName", sharedPreferences.getString("name", ""));
        data.put("AttendeePhoneNumber", sharedPreferences.getString("phonenumber", ""));;
        data.put("AttendeeEmail", sharedPreferences.getString("email", ""));
        data.put("AttendeeProfile", sharedPreferences.getString("imageUri", ""));
        data.put("CheckedIn", true);
        data.put("CheckedInCount", checkedInCount.toString());
        data.put("CheckInLocation", locationText.getText().toString());
        attendeeRef.document(sharedPreferences.getString("phonenumber","")).set(data);
        Toast.makeText(getApplicationContext(), "Successfully Checked In", Toast.LENGTH_SHORT).show();
    }
}
