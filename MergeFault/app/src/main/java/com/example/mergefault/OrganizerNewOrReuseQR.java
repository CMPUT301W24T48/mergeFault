package com.example.mergefault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * This activity allows organizers to choose whether to generate a new QR code or reuse an existing one.
 */
public class OrganizerNewOrReuseQR extends AppCompatActivity {
    private Button generateNewQR;
    private Button reuseQR;
    private Button cancelButton;
    private ImageView homeButton;
    private String eventId;
    private Uri selectedImage;
    private FirebaseFirestore db;
    private DocumentReference eventRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventPosterRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_generate_or_reuse);

        // Get the necessary objects from the UI
        generateNewQR = findViewById(R.id.generateNewButton);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.logoImgView);
        reuseQR = findViewById(R.id.continueButton);

        // Receive eventId and selectedImage uri from the previous activity
        Intent intent = getIntent();
        selectedImage = intent.getData();
        eventId = intent.getStringExtra("EventId");

        // Get instance and reference to the firebase firestore
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events").document(eventId);

        // Get instance and reference to the firebase storage
        firebaseStorage = FirebaseStorage.getInstance();
        eventPosterRef = firebaseStorage.getReference().child( "eventPosters/" + eventId + ".jpg");

        // Set click listener for the Logo
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventPosterRef.delete();
                eventRef.delete();
                Intent intent = new Intent(OrganizerNewOrReuseQR.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Generate New QR" button
        generateNewQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerGenerateAndShareQR activity to generate a new QR code
                Intent intent = new Intent(OrganizerNewOrReuseQR.this, OrganizerGenerateAndShareQR.class);
                intent.putExtra("EventId", eventId);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Reuse Existing QR" button
        reuseQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerGenerateAndShareQR activity to generate a new QR code
                Intent intent = new Intent(OrganizerNewOrReuseQR.this, OrganizerReuseQR.class).setData(selectedImage).setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("EventId", eventId);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Cancel" button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventPosterRef.delete();
                eventRef.delete();
                Intent intent = new Intent(OrganizerNewOrReuseQR.this, OrganizerAddEventActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set what happens when back button is pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                eventPosterRef.delete();
                eventRef.delete();
                Intent intent = new Intent(OrganizerNewOrReuseQR.this, OrganizerAddEventActivity.class);
                startActivity(intent);
                finish();
            }
        };
        OrganizerNewOrReuseQR.this.getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
