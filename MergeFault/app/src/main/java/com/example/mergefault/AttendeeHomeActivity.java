package com.example.mergefault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Objects;

/**
 * This activity serves as the home screen for attendees.
 * Attendees can view their profile image, view their events, and browse all events from this screen.
 */
public class AttendeeHomeActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private ImageView homeIcon;

    private Button viewMyEvents;
    private Button browseAllEvents;
    private Button openCamera;
    private SharedPreferences sharedPreferences;

    private FirebaseFirestore db;
    private CollectionReference attendeeRef;
    private Boolean hasProfile = false;
    ActivityResultLauncher<Intent> profileEditLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            (result) -> {

            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_home);

        viewMyEvents = findViewById(R.id.viewMyEventsButton);
        browseAllEvents = findViewById(R.id.browseEventsButton);
        homeIcon = findViewById(R.id.imageView);
        openCamera = findViewById(R.id.openCamera);
        profileImageView = findViewById(R.id.profileImageView);

        db = FirebaseFirestore.getInstance();
        attendeeRef = db.collection("attendees");

        // Start recording user information
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        if (sharedPreferences.getString("attendeeId", null) != null) {
            Log.d("Shared preferences", "containts id");
            attendeeRef.document(sharedPreferences.getString("attendeeId", null)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (!doc.exists()) {
                            Log.d("clear", "cleared preferences");
                            sharedPreferences.edit().clear().apply();
                            hasProfile = false;
                        } else {
                            hasProfile = true;
                        }
                    }
                }
            });
        }

        // Load the profile image at the top of the screen

        loadProfileImage();

        // Set click listener for the profile image to navigate to the edit/view profile screen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // Set click listener for "View My Events" button
        viewMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasProfile) {
                    Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeSignedUpEventsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),"You have no profile", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for "Browse All Events" button
        browseAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasProfile) {
                    Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeBrowsePostedEventsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),"You have no profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasProfile) {
                    Intent intent = new Intent(AttendeeHomeActivity.this, QRCodeScannerActivity.class);
                    intent.putExtra("parentActivity", "AttendeeHome");
                    startActivityForResult(intent, 0);
                } else {
                    Toast.makeText(getApplicationContext(),"You have no profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadProfileImage() {
        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getId().equals(sharedPreferences.getString("attendeeId", null))) {
                        if (doc.getString("AttendeeProfile") != null) {
                            Picasso.get().load(doc.getString("AttendeeProfile")).into(profileImageView);
                        }
                    }
                }
            }
        });
    }
    /**
     * Handles the result when returning from another activity.
     * If changes were made to the profile image, reload it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                String action = data.getStringExtra("action");
                if (Objects.equals(action, "CheckIn")) {
                    String eventId = data.getStringExtra("eventId");
                    Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeCheckInScreenActivity.class);
                    intent.putExtra("eventId", eventId);
                    Log.d("Scanned stuff", eventId);
                    startActivity(intent);
                    finish();
                } else if (Objects.equals(action, "Promotion")) {
                    String eventId = data.getStringExtra("eventId");
                    Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeSignUpActivity.class);
                    intent.putExtra("eventId", eventId);
                    intent.putExtra("parentActivity", "AttendeeHome");
                    Log.d("Scanned stuff", eventId);
                    startActivity(intent);
                    finish();
                }
            } else if (requestCode == 1) {
                Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeHomeActivity.class);
            startActivity(intent);
            finish();
        } else if (resultCode == 2) {
            Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeHomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Scan failed", Toast.LENGTH_SHORT);
            Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeHomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
