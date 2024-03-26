package com.example.mergefault;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;

public class CheckInScreen extends AppCompatActivity {
    private Button checkIn;
    private FirebaseFirestore db;
    private CollectionReference attendeesRef;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_check_in_screen);
        checkIn = findViewById(R.id.checkInButton);
        db = FirebaseFirestore.getInstance();
        attendeesRef = db.collection("attendees");

        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
    }
}