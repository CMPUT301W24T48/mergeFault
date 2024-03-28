package com.example.mergefault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Objects;


/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeViewEventDetailsActivity extends AppCompatActivity {

    // Event ID
    private String eventId;
    private FirebaseFirestore db;
    private CollectionReference events;
    private CollectionReference attendeeRef;
    private TextView location;
    private TextView description;
    private Button withdrawButton;
    private Button cancelButton;
    private SharedPreferences sharedPreferences;

    /**
     * This Activity displays event details and allows users to sign up for notifications or withdraw
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_event_details);

        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        withdrawButton = findViewById(R.id.withdrawButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Get the intent that started this activity
        Intent intent = getIntent();
        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        Bundle bundle = intent.getExtras();
        eventId = bundle.getString("0");
        attendeeRef = db.collection("events").document(eventId).collection("attendees");

        events.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null){
                    for(QueryDocumentSnapshot doc: value) {
                        if(Objects.equals(doc.getString("EventID"), eventId)){
                            location.setText(doc.getString("Location"));
                            description.setText(doc.getString("Description"));
                        }
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
            }
        });
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendeeRef.document(sharedPreferences.getString("phonenumber", "")).delete();
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
            }
        });

    }
}

