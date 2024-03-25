package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity allows organizers to choose whether to generate a new QR code or reuse an existing one.
 */
public class OrganizerNewOrReuseQR extends AppCompatActivity {

    private Button generateNewQR;
    private String eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_generate_or_reuse);

        generateNewQR = findViewById(R.id.generateNewButton);

        // Retrieve the eventId from the intent
        Intent receiverIntent = getIntent();
        eventId = receiverIntent.getStringExtra("EventId");
        Log.d("eventIdAfter", "eventid:" + eventId);

        // Set click listener for generate new QR code button
        generateNewQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerShareQR activity to generate a new QR code
                Intent intent = new Intent(OrganizerNewOrReuseQR.this, OrganizerShareQR.class);
                intent.putExtra("EventId", eventId);
                startActivity(intent);
            }
        });
    }
}
