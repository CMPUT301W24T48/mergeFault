package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This activity allows organizers to choose whether to generate a new QR code or reuse an existing one.
 */
public class OrganizerNewOrReuseQR extends AppCompatActivity {

    private Button generateNewQR;
    private Button reuseQR;
    private Button cancelButton;
    private ImageView homeButton;
    private String eventId;
    private FirebaseFirestore db;
    private DocumentReference eventRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_generate_or_reuse);

        generateNewQR = findViewById(R.id.generateNewButton);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.logoImgView);
        reuseQR = findViewById(R.id.continueButton);

        db = FirebaseFirestore.getInstance();

        // Retrieve the eventId from the intent
        Intent receiverIntent = getIntent();
        eventId = receiverIntent.getStringExtra("EventId");
        Log.d("eventIdAfter", "eventid:" + eventId);
        eventRef = db.collection("events").document(eventId);

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

        reuseQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerShareQR activity to generate a new QR code
                Intent intent = new Intent(OrganizerNewOrReuseQR.this, OrganizerReuseQR.class);
                intent.putExtra("EventId", eventId);
                startActivity(intent);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventRef.delete();
                finish();
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                eventRef.delete();
                finish();
            }
        };
        OrganizerNewOrReuseQR.this.getOnBackPressedDispatcher().addCallback(this, callback);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerNewOrReuseQR.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
