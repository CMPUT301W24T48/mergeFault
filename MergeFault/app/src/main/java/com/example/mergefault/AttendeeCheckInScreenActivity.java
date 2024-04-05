package com.example.mergefault;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.InputStream;
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
    private TextView timeText;
    private ImageView eventPoster;
    private Button checkInButton;
    private Button cancelButton;
    private TextView descriptionText;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference eventAttendeeRef;
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
        eventsRef = db.collection("events");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // Initialize location text view
        locationText = findViewById(R.id.CheckInLocationText);
        descriptionText = findViewById(R.id.CheckInDescriptionText);
        timeText = findViewById(R.id.CheckInTimeText);


        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Handle incoming intent data
        Intent intent = getIntent();
        eventId = intent.getExtras().get("eventId").toString();

        Log.d("checkineventid", "eventId: " + eventId);


        eventAttendeeRef = eventsRef.document(eventId).collection("attendees");

        eventsRef.document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        locationText.setText(doc.getString("Location"));
                        descriptionText.setText(doc.getString("Description"));
                        timeText.setText(doc.getDate("DateTime").toString());
                        new AttendeeCheckInScreenActivity.DownloadImageFromInternet((ImageView) findViewById(R.id.eventPoster)).execute(doc.getString("EventPoster"));
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

    class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
    }

