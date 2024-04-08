package com.example.mergefault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeSignUpActivity extends AppCompatActivity {

    // Event ID
    private String eventId;
    private String parentActivity;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private CollectionReference eventRef;
    private CollectionReference eventAttendeeRef;
    private CollectionReference attendeeRef;
    private TextView location;
    private TextView description;
    private TextView time;
    private Button signUpButton;
    private Button cancelButton;
    private ImageView homeButton;
    private ImageView profileImageView;
    private ImageView eventPoster;
    private SharedPreferences sharedPreferences;
    private ImageView notificationButton;
    /**
     * @see AttendeeBrowsePostedEventsActivity
     * this Activity displays event details and a button that signs up attendees to the event
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signup_for_event);
        location = findViewById(R.id.LocationText);
        description = findViewById(R.id.DescriptionText);
        time = findViewById(R.id.TimeText);
        signUpButton = findViewById(R.id.withdrawButton);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);
        profileImageView = findViewById(R.id.ProfilePicture);
        eventPoster = findViewById(R.id.eventPoster);
        notificationButton = findViewById(R.id.notifBellImageView);
        // Get the intent that started this activity
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        parentActivity = intent.getStringExtra("parentActivity");
        Log.d("eventId", "eventId: " + eventId);

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        eventAttendeeRef = eventRef.document(eventId).collection("attendees");

        loadProfileImage();

        eventRef.document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Date currentTime = Calendar.getInstance().getTime();
                        if (currentTime.before(doc.getDate("DateTime"))) {
                            location.setText(doc.getString("Location"));
                            description.setText(doc.getString("Description"));
                            time.setText(doc.getDate("DateTime").toString());
                            if (doc.getString("EventPoster") != null) {
                                Picasso.get().load(doc.getString("EventPoster")).into(eventPoster);
                            }
                        } else {
                            deleteEventAndAssociation(doc, db, firebaseStorage);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No event detected", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeHomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(parentActivity, "AttendeeHome")) {
                    Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    switchActivities();
                }
            }
        });

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Objects.equals(parentActivity, "AttendeeHome")) {
                    Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    switchActivities();
                }
            }
        };
        AttendeeSignUpActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeNotifications.class);
                startActivity(intent);

            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubscribeAttendee();
                AddAttendee();

            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 0);
            }
        });

    }

    /**
     * Adds attendee and their information to the event upon signup button click with a unique ID
     */
    public void AddAttendee() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("CheckedIn", false);
        data.put("CheckedInCount", 0);
        //data.put("AttendeeNotificationPref", attendee.getNotificationPref());
        //data.put("AttendeeGeolocationPref", attendee.getGeolocationPref());
        eventAttendeeRef.document(sharedPreferences.getString("attendeeId", null)).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                attendeeRef.document(sharedPreferences.getString("attendeeId", null)).update("signedInEvents", FieldValue.arrayUnion(eventId));
                Toast.makeText(getApplicationContext(), "Successfully Signed Up!", Toast.LENGTH_SHORT).show();
                switchActivities();
            }
        });
    }
    public void SubscribeAttendee(){
        FirebaseMessaging.getInstance().subscribeToTopic(eventId)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Subscription successful
                            Log.d("Subscribe","Successfully subscribed to topic: " + eventId);
                        } else {
                            // Subscription failed
                            Log.d("Subscribe","Failed to subscribe to topic: " + eventId);
                            Exception e = task.getException();
                            if (e != null) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


    }
    private void loadProfileImage() {
        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getId().equals(sharedPreferences.getString("attendeeId", null))) {
                        if (doc.getString("AttendeeProfile") != null) {
                            Picasso.get().load(doc.getString("AttendeeProfile")).into(profileImageView);
                        }
                    }
                }
            }
        });
    }
    public void switchActivities() {
        Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeBrowsePostedEventsActivity.class);
        startActivity(intent);
        finish();
    }
    private void deleteEventAndAssociation (DocumentSnapshot doc, FirebaseFirestore db, FirebaseStorage firebaseStorage) {
        CollectionReference eventRef = db.collection("events");
        CollectionReference attendeeRef = db.collection("attendees");
        CollectionReference eventAttendeeRef = eventRef.document(doc.getId()).collection("attendees");
        eventAttendeeRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        eventRef.document(doc.getId()).collection("attendees").document(document.getId()).delete();
                    }
                    StorageReference eventPosterRef = firebaseStorage.getReference().child("eventPosters/" + doc.getId() + ".jpg");
                    eventPosterRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            eventRef.document(doc.getId()).delete();
                        }
                    });
                }
            }
        });
        attendeeRef.whereArrayContains("signedInEvents", doc.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (!querySnapshot.isEmpty()) {
                    List<DocumentSnapshot> attendeesThatSignedUp = querySnapshot.getDocuments();
                    for (int i = 0; i < attendeesThatSignedUp.size(); i++) {
                        DocumentSnapshot attendee = attendeesThatSignedUp.get(i);
                        attendeeRef.document(attendee.getId()).update("signedInEvents", FieldValue.arrayRemove(doc.getId()));
                    }
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Reload the profile image if changes were made
            loadProfileImage();
        }
    }

}

