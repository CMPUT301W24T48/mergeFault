package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class OrganizerReuseQR extends AppCompatActivity {
    private String eventId;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventCheckInQRRef;
    private StorageReference eventPromotionQRRef;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_reuse_qr);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("EventId");

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        firebaseStorage = FirebaseStorage.getInstance();
        eventCheckInQRRef = firebaseStorage.getReference().child( "QRCodes/" + eventId + "CheckIn.jpg");
        eventPromotionQRRef = firebaseStorage.getReference().child( "QRCodes/" + eventId + "Promotion.jpg");


    }
}
