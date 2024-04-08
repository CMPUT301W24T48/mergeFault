package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity serves as the home screen for organizers.
 * Organizers can create new events or view their existing events from this screen.
 */
public class OrganizerHomeActivity extends AppCompatActivity {
    private Button createNewEventButton;
    private Button viewMyEvents;
    private ImageView homeButton;
    private String organizerIDString;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_home);

        // Get the necessary objects from the UI
        createNewEventButton = findViewById(R.id.createNewEventButton);
        viewMyEvents = findViewById(R.id.viewMyEventsButton);
        homeButton = findViewById(R.id.imageView);

        // Get the unique identifier for the organizer's device
        organizerIDString = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);

        // Set click listener for "Create New Event" button
        createNewEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerAddEventActivity to create a new event
                Intent intent = new Intent(OrganizerHomeActivity.this, OrganizerAddEventActivity.class);
                // Passing unique unique identifier for the organizer's device through intent
                intent.putExtra("OrganizerID", organizerIDString);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for Logo
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for "View My Events" button
        viewMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerViewEvents activity to view existing events
                Intent intent = new Intent(OrganizerHomeActivity.this, OrganizerViewEventsActivity.class);
                // Passing unique unique identifier for the organizer's device through intent
                intent.putExtra("OrganizerID", organizerIDString);
                startActivity(intent);
                finish();
            }
        });
    }
}
