package com.example.mergefault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.grpc.InternalWithLogId;

public class OrganizerEventOptions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_edit_event);
        Event event = (Event) getIntent().getSerializableExtra("SelectedEvent");

        //set up buttons
        Button attendeeListButton = findViewById(R.id.attendeeListButton);
        Button eventDetailsButton = findViewById(R.id.eventDetailsButton);
        Button sendNotificationsButton = findViewById(R.id.sendNotifButton);
        Button cancelButton = findViewById(R.id.cancelButton);
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


