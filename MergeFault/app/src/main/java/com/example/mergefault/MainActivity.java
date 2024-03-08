package com.example.mergefault;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * The main entry point of the application.
 * This activity allows users to navigate to different sections of the application based on their roles (attendee, organizer, admin).
 */
public class MainActivity extends AppCompatActivity {

    private Button attendeeButton;
    private Button organizerButton;
    private Button adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize attendeeButton
        attendeeButton = findViewById(R.id.attendeeButton);
        organizerButton = findViewById(R.id.organizerButton);
        adminButton = findViewById(R.id.adminButton);

        // Set OnClickListener for attendeeButton
        attendeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AttendeeHomeActivity when attendeeButton is clicked
                Intent intent = new Intent(MainActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for organizerButton
        organizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start OrganizerHomeActivity when organizerButton is clicked
                Intent intent = new Intent(MainActivity.this, OrganizerHomeActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for adminButton
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AdminHomeActivity when adminButton is clicked
                Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
