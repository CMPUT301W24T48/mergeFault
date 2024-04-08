package com.example.mergefault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.Objects;


/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeViewEventDetailsActivity extends AppCompatActivity {
    private String eventId;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference eventAttendeeRef;
    private CollectionReference attendeeRef;
    private TextView location;
    private TextView description;
    private TextView time;
    private Button withdrawButton;
    private Button cancelButton;
    private SwitchCompat notifySwitch;
    private ImageView homeButton;
    private ImageView eventPosterImageView;
    private SharedPreferences sharedPreferences;
    private ImageView notificationButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_event_details);

        // Get the necessary objects from the UI
        location = findViewById(R.id.EventDetailsLocationText);
        description = findViewById(R.id.EventDetailsDescriptionText);
        time = findViewById(R.id.EventDetailsTimeText);
        withdrawButton = findViewById(R.id.withdrawButton);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);
        eventPosterImageView = findViewById(R.id.eventPoster);
        notifySwitch = findViewById(R.id.notifSwitch);
        notificationButton = findViewById(R.id.notifBellImageView);

        // Get the intent that started this activity
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        // Get shared preferences from device
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // Receive eventId and parentActivity from the previous activity
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");
        eventAttendeeRef = db.collection("events").document(eventId).collection("attendees");

        // Set up snapshot listener to listen to changes in the event collection on firestore
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        if (Objects.equals(doc.getString("EventID"), eventId)) {
                            location.setText(doc.getString("Location"));
                            description.setText(doc.getString("Description"));
                            time.setText(doc.getDate("DateTime").toString());
                            if (doc.getString("EventPoster") != null) {
                                Picasso.get().load(doc.getString("EventPoster")).into(eventPosterImageView);
                            }
                        }
                    }
                }
            }
        });

        // Set click listener for the notification icon
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeNotifications.class);
                startActivity(intent);
            }
        });

        // Set click listener for the notification switch
        notifySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implement notification stuff
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Cancel" button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set what happens when the back button is pressed
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AttendeeViewEventDetailsActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Set click listener for the "Withdraw" button
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(eventId);
                eventAttendeeRef.document(sharedPreferences.getString("attendeeId", "")).delete();
                attendeeRef.document(sharedPreferences.getString("attendeeId", "")).update("signedInEvents", FieldValue.arrayRemove(eventId));
                Toast.makeText(getApplicationContext(), "Withdrew sign up", Toast.LENGTH_SHORT);
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the Logo
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}


