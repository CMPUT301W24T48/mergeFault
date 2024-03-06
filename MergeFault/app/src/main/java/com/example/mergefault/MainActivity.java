package com.example.mergefault;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button attendeeButton;
    private Button organizerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize attendeeButton
        attendeeButton = findViewById(R.id.attendeeButton);

        organizerButton = findViewById(R.id.organizerButton);

        // Set OnClickListener for attendeeButton
        attendeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AttendeeHomeActivity when attendeeButton is clicked
                Intent intent = new Intent(MainActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
            }
        });

        organizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OrganizerHomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
