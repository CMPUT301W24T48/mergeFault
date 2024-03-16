package com.example.mergefault;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

/**
 * This activity serves as the home screen for attendees.
 * Attendees can view their profile image, view their events, and browse all events from this screen.
 */
public class AttendeeHomeActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private ImageView homeIcon;

    private Button viewMyEvents;
    private Button browseAllEvents;
    private SharedPreferences sharedPreferences;

    private FirebaseFirestore db;
    private CollectionReference attendeeImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_home);

        viewMyEvents = findViewById(R.id.viewMyEventsButton);
        browseAllEvents = findViewById(R.id.browseEventsButton);
        homeIcon = findViewById(R.id.imageView);

        db = FirebaseFirestore.getInstance();
        attendeeImageRef = db.collection("attendees");

        // Start recording user information
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        profileImageView = findViewById(R.id.profileImageView);

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
                Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // Set click listener for "Browse All Events" button
        browseAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeBrowsePostedEventsActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Loads the profile image from the saved user profile.
     * If no image is found in the user profile, a default image is set.
     */
    private void loadProfileImage() {
        attendeeImageRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for(QueryDocumentSnapshot doc: value){
                    if (doc.getId().equals(sharedPreferences.getString("phonenumber", ""))){
                        if (!doc.getString("AttendeeProfile").isEmpty()) {
                            Picasso.get().load(doc.getString("AttendeeProfile")).into(profileImageView);
                        }
                        break;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Reload the profile image if changes were made
            loadProfileImage();
        }
    }
}
