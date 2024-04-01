package com.example.mergefault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;


/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeSignUpPage extends AppCompatActivity {

    // Event ID
    private String eventId;
    private FirebaseFirestore db;
    private CollectionReference events;
    private CollectionReference attendeeRef;
    private TextView location;
    private TextView description;
    private TextView time;
    private Button signUpButton;
    private Button cancelButton;
    private ImageView eventPoster;
    private SharedPreferences sharedPreferences;
    /**
     * this Activity displays event details and a button that signs up attendees to the event
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signup_for_event);
        location = findViewById(R.id.LocationText);
        description = findViewById(R.id.DescriptionText);
        time = findViewById(R.id.TimeText);
        eventPoster = findViewById(R.id.eventPoster);
        signUpButton = findViewById(R.id.withdrawButton);
        cancelButton = findViewById(R.id.cancelButton);
        // Get the intent that started this activity
        Intent intent = getIntent();

        // Get the data URI from the intent
        Uri uri = intent.getData();

        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        /*
        if (("myapp".equals(uri.getScheme()) && "www.lotuseventspromotions.com".equals(uri.getHost()))) {
            eventId = uri.getQueryParameter("eventId");
        }
        else {

         */
            Bundle bundle = intent.getExtras();
            eventId = bundle.getString("0");
        //}
        attendeeRef = db.collection("events").document(eventId).collection("attendees");

        events.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null){
                    for(QueryDocumentSnapshot doc: value) {
                        if(Objects.equals(doc.getString("EventID"), eventId)){
                            location.setText(doc.getString("Location"));
                            description.setText(doc.getString("Description"));
                            time.setText(doc.getDate("DateTime").toString());
                            Picasso.get().load(Uri.parse(doc.getString("EventPoster"))).into(eventPoster);
                        }
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpPage.this, AttendeeBrowsePostedEventsActivity.class);
                startActivity(intent);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAttendee();
                Intent intent = new Intent(AttendeeSignUpPage.this, AttendeeBrowsePostedEventsActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Adds attendee and their information to the event upon signup button click with a unique ID
     */
    public void AddAttendee() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("AttendeeName", sharedPreferences.getString("name", ""));
        data.put("AttendeePhoneNumber", sharedPreferences.getString("phonenumber", ""));;
        data.put("AttendeeEmail", sharedPreferences.getString("email", ""));
        data.put("AttendeeProfile", sharedPreferences.getString("imageUri", ""));
        data.put("CheckedIn", false);
        data.put("CheckedInCount", "0");
        //data.put("AttendeeNotificationPref", attendee.getNotificationPref());
        //data.put("AttendeeGeolocationPref", attendee.getGeolocationPref());
        attendeeRef.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String attendeeID = documentReference.getId();
                data.put("AttendeeID", sharedPreferences.getString("phonenumber", ""));
                documentReference.delete();
                attendeeRef.document(sharedPreferences.getString("phonenumber", "")).set(data);
                Log.d("attendeeIDBefore", "attendeeid" + attendeeID);
                Toast.makeText(getApplicationContext(), "Successfully Signed Up!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

