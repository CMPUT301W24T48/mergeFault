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
import com.squareup.picasso.Picasso;

import java.util.Objects;


/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeViewEventDetailsActivity extends AppCompatActivity {

    // Event ID
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

    /**
     * This Activity displays event details and allows users to sign up for notifications or withdraw
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_event_details);

        location = findViewById(R.id.EventDetailsLocationText);
        description = findViewById(R.id.EventDetailsDescriptionText);
        time = findViewById(R.id.EventDetailsTimeText);
        withdrawButton = findViewById(R.id.withdrawButton);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);
        eventPosterImageView = findViewById(R.id.eventPoster);
        notifySwitch = findViewById(R.id.notifSwitch);

        // Get the intent that started this activity
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        eventAttendeeRef = db.collection("events").document(eventId).collection("attendees");

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
        notifySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implement notif stuff
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AttendeeViewEventDetailsActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventAttendeeRef.document(sharedPreferences.getString("attendeeId", "")).delete();
                attendeeRef.document(sharedPreferences.getString("attendeeId", "")).update("signedInEvents", FieldValue.arrayRemove(eventId));
                Toast.makeText(getApplicationContext(), "Withdrew sign up", Toast.LENGTH_SHORT);
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });
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


