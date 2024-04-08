package com.example.mergefault;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @see  AttendeeViewEventDetailsActivity
 * Activity for attendee check-in at an event.
 *
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
    private TextView timeText;
    private ImageView eventPoster;
    private Button checkInButton;
    private Button cancelButton;
    private TextView descriptionText;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference eventAttendeeRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventPosterRef;
    private Integer checkedInCount;
    private Boolean attendeeCheckedIn = false;
    private String locationInfo = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_check_in_screen);

        checkInButton = findViewById(R.id.checkInButton);
        cancelButton = findViewById(R.id.cancelButton);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        firebaseStorage = FirebaseStorage.getInstance();

        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // Initialize location text view
        locationText = findViewById(R.id.CheckInLocationText);
        descriptionText = findViewById(R.id.CheckInDescriptionText);
        timeText = findViewById(R.id.CheckInTimeText);
        eventPoster = findViewById(R.id.eventPoster);


        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Handle incoming intent data
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        Log.d("checkineventid", "eventId: " + eventId);


        eventAttendeeRef = eventRef.document(eventId).collection("attendees");

        eventRef.document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Date currentTime = Calendar.getInstance().getTime();
                        if (currentTime.before(doc.getDate("DateTime"))) {
                            locationText.setText(doc.getString("Location"));
                            descriptionText.setText(doc.getString("Description"));
                            timeText.setText(doc.getDate("DateTime").toString());
                            if (doc.getString("EventPoster") != null) {
                                Picasso.get().load(doc.getString("EventPoster")).into(eventPoster);
                            }
                        } else {
                            deleteEventAndAssociation(doc, db, firebaseStorage);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No event detected", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AttendeeCheckInScreenActivity.this, AttendeeHomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeCheckInScreenActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AttendeeCheckInScreenActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AttendeeCheckInScreenActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
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

        locationInfo = "Latitude: " + latitude + ", Longitude: " + longitude;
        CheckInAttendee();

    }

    private void CheckInAttendee() {
        eventAttendeeRef.document(sharedPreferences.getString("attendeeId", null)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("CheckedIn", true);
                        data.put("CheckedInCount", FieldValue.increment(1));
                        data.put("CheckInLocation", locationInfo);
                        eventAttendeeRef.document(sharedPreferences.getString("attendeeId", null)).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                   Toast.makeText(getApplicationContext(), "Successfully Checked In", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AttendeeCheckInScreenActivity.this, AttendeeHomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                    } else {
                        Toast.makeText(getApplicationContext(), "Error: You have not signed up to this event", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AttendeeCheckInScreenActivity.this, AttendeeHomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

    }
    private void deleteEventAndAssociation (DocumentSnapshot doc, FirebaseFirestore db, FirebaseStorage firebaseStorage) {
        CollectionReference eventRef = db.collection("events");
        CollectionReference attendeeRef = db.collection("attendees");
        CollectionReference eventAttendeeRef = eventRef.document(doc.getId()).collection("attendees");
        eventAttendeeRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        eventRef.document(doc.getId()).collection("attendees").document(document.getId()).delete();
                    }
                    StorageReference eventPosterRef = firebaseStorage.getReference().child("eventPosters/" + doc.getId() + ".jpg");
                    eventPosterRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            eventRef.document(doc.getId()).delete();
                        }
                    });
                }
            }
        });
        attendeeRef.whereArrayContains("signedInEvents", doc.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (!querySnapshot.isEmpty()) {
                    List<DocumentSnapshot> attendeesThatSignedUp = querySnapshot.getDocuments();
                    for (int i = 0; i < attendeesThatSignedUp.size(); i++) {
                        DocumentSnapshot attendee = attendeesThatSignedUp.get(i);
                        attendeeRef.document(attendee.getId()).update("signedInEvents", FieldValue.arrayRemove(doc.getId()));
                    }
                }
            }
        });
    }
}

