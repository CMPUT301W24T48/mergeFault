package com.example.mergefault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class OrganizerEventOptions extends AppCompatActivity {

    private String eventId;
    private FirebaseFirestore db;
    private DocumentReference eventRef;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_edit_event);

        Intent receiverIntent = getIntent();
        eventId = receiverIntent.getStringExtra("EventId");

        Log.d("eventId", "eventid: " + eventId);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Calendar date = Calendar.getInstance();
                        Date dateTime = doc.getDate("DateTime");
                        date.setTime(dateTime);
                        event = new Event(doc.getString("EventName"),
                                doc.getString("OrganizerID"),
                                doc.getString("Location"),
                                date,
                                Integer.parseInt(doc.getString("AttendeeLimit")),
                                Uri.parse(doc.getString("EventPoster")),
                                doc.getString("Description"),
                                doc.getBoolean("GeoLocOn"),
                                doc.getId(),
                                doc.getString("PlaceID"));
                    }
                }
            }
        });

        //set up buttons
        Button attendeeListButton = findViewById(R.id.attendeeListButton);
        Button eventDetailsButton = findViewById(R.id.eventDetailsButton);
        Button sendNotificationsButton = findViewById(R.id.sendNotifButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button mapButton = findViewById(R.id.checkinMapButton);
        /// TODO: 20-03-2024 add map button

        // Attendee List button click
        attendeeListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerAttendeeList.class);
                startActivity(intent);
            }
        });

        // Event Details button click
        eventDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, event_details.class);
                startActivity(intent);
            }
        });

        // Send Notification button click
        sendNotificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerEventNotification.class);
                startActivity(intent);
            }
        });
        
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // implement map
            }
        });

        //Cancel button click
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerViewEventsActivity.class);
                startActivity(intent);
            }
        });


    }
}


