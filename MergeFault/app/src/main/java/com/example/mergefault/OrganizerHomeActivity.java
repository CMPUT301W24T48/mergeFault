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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_home);

        createNewEventButton = findViewById(R.id.createNewEventButton);
        viewMyEvents = findViewById(R.id.viewMyEventsButton);
        homeButton = findViewById(R.id.imageView);

        // Set click listener for "Create New Event" button
        createNewEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerAddEventActivity to create a new event
                Intent intent = new Intent(OrganizerHomeActivity.this, OrganizerAddEventActivity.class);
                // Get the unique identifier for the organizer's device and pass it as extra
                String organizerIDString = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                intent.putExtra("OrganizerID", organizerIDString);
                startActivity(intent);
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerHomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener for "View My Events" button
        viewMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerViewEvents activity to view existing events
                Intent intent = new Intent(OrganizerHomeActivity.this, OrganizerViewEventsActivity.class);
                String organizerIDString = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                intent.putExtra("OrganizerID", organizerIDString);
                startActivity(intent);
            }
        });
    }
}
