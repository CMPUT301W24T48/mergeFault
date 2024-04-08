package com.example.mergefault;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @see  AttendeeViewEventDetailsActivity
 * Activity for attendee check-in at an event.
 *
 */
public class AttendeeCheckInScreenActivity extends AppCompatActivity {
    // Request code for location permission
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private String eventId;
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
    private Boolean attendeeCheckedIn = false;
    private String locationInfo = null;
    private String eventName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_check_in_screen);

        // Get the necessary objects from the UI
        checkInButton = findViewById(R.id.checkInButton);
        cancelButton = findViewById(R.id.cancelButton);
        locationText = findViewById(R.id.CheckInLocationText);
        descriptionText = findViewById(R.id.CheckInDescriptionText);
        timeText = findViewById(R.id.CheckInTimeText);
        eventPoster = findViewById(R.id.eventPoster);

        // Receive eventId from the previous activity
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        // Get shared preferences from device
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // Get instance and reference to the firebase firestore
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        eventAttendeeRef = eventRef.document(eventId).collection("attendees");

        // Get instance to the firebase storage
        firebaseStorage = FirebaseStorage.getInstance();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Getting the selected event's details from firestore
        eventRef.document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Date currentTime = Calendar.getInstance().getTime();
                        if (currentTime.before(doc.getDate("DateTime"))) {
                            eventName = doc.getString("EventName");
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

        // Set click listener for the "Check In" button
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        // Set click listener for the "Cancel" button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeCheckInScreenActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set what happens when back button is pressed
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

    /**
     * This method is used to handle permission request result
     * @param requestCode The request code passed in {@link #requestPermissions(
     * android.app.Activity, String[], int)}
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
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

    /**
     * This method retrieves the device's location and records it using recordLocation method
     */
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

    /**
     * This method take the current location of the device and turns it into a string of longitude and latitude then calls then CheckInAttendee method
     * @param location the current location of device
     */
    private void recordLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        locationInfo = "Latitude: " + latitude + ", Longitude: " + longitude;
        CheckInAttendee();

    }

    /**
     * This method checks in the attendee onto firebase and logs its check-in count and location
     */
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
                                SendNotificationToOrganizer();
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
    /**
     * This method takes a document snapshot, a instance of firestore and an instance of storage to delete all associated data with the event like attendee sub-collections and event poster
     * @param doc This is the document snapshot of the event from firestore
     * @param db This is an the instance of the firebase firestore
     * @param firebaseStorage This is an instance of the firebase storage
     */
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
                    StorageReference eventPosterRef = firebaseStorage.getReference().child( "eventPosters/" + doc.getId() + ".jpg");
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
                    List<DocumentSnapshot> attendeesThatSignedUp =  querySnapshot.getDocuments();
                    for (int i = 0; i < attendeesThatSignedUp.size(); i++) {
                        DocumentSnapshot attendee = attendeesThatSignedUp.get(i);
                        attendeeRef.document(attendee.getId()).update("signedInEvents", FieldValue.arrayRemove(doc.getId()));
                    }
                }
            }
        });
    }

    public void SendNotificationToOrganizer(){
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        String topic = eventId + "_organizer";
        try {
            json.put("to", "/topics/" + topic);
            JSONObject notification = new JSONObject();
            notification.put("title", eventName);
            notification.put("body", "You have a new check-in for your event!");
            json.put("notification", notification);
        } catch (JSONException e) {
            return;

        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .addHeader("Authorization", "key=AAAAJKAW9vA:APA91bG2WW61c9h2OVwu4A4eg6wLiHfPGLNTA517lEj-s66ywb6VxLcAGv0jHRKWMy3XLf0oE9vdZUBG7hnqjNZuukAs6FNCkdU8Pj6afTLGPPAKh3wH6aC54ev5OkG0rpqMUVI2Dhr2")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("FCM_RESPONSE", "Response: " + responseBody);


                } else {
                    String errorResponse = response.body().string();
                    String status = response.code() + " " + response.message();
                    Log.e("FCM_RESPONSE", "Unsuccessful response: " + status);
                    Log.e("FCM_RESPONSE", "Error Body: " + errorResponse);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("FCM_RESPONSE", "Request failed: " + e.getMessage());
            }
        });


    }
}

