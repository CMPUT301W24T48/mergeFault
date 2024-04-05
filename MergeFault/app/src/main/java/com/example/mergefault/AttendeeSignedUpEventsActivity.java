package com.example.mergefault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @see AttendeeViewEventDetailsActivity
 * This activity displays all currently signed up events for attendees
 * Attendees can view the list of signed up events
 */
public class AttendeeSignedUpEventsActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private SharedPreferences sharedPreferences;
    private ListView signedUpEventsList;
    private EventArrayAdapter eventArrayAdapter;

    private ArrayList<Event> signedUpEventDataList;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private DocumentReference eventAttendeeRef;
    private CollectionReference attendeeRef;

    private String eventID;
    private String eventName;
    private String organizerId;
    private String location;
    private String placeId;
    private Date dateTime;
    private Uri imageURL;
    private Integer attendeeLimit;
    private Calendar date;
    private String description;
    private Boolean geoLocOn;
    private ImageView homeIcon;
    private Button cancelButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signed_up_events);

        profileImageView = findViewById(R.id.pfpImageView);
        signedUpEventsList = findViewById(R.id.myEventListView);
        homeIcon = findViewById(R.id.imageView);
        profileImageView = findViewById(R.id.pfpImageView);
        cancelButton = findViewById(R.id.cancelButton);
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");

        loadProfileImage();

        signedUpEventDataList = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, signedUpEventDataList);
        signedUpEventsList.setAdapter(eventArrayAdapter);


        eventArrayAdapter.notifyDataSetChanged();

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AttendeeSignedUpEventsActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        signedUpEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedEventId = signedUpEventDataList.get(position).getEventID();
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeViewEventDetailsActivity.class);
                intent.putExtra("eventId", selectedEventId);
                startActivity(intent);
                finish();
            }
        });

        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    signedUpEventDataList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        eventAttendeeRef = db.collection("events").document(doc.getId()).collection("attendees").document(sharedPreferences.getString("attendeeId", null));
                        eventAttendeeRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()) {
                                        eventName = doc.getString("EventName");
                                        organizerId = doc.getString("OrganizerID");
                                        location = doc.getString("Location");
                                        placeId = doc.getString("PlaceID");
                                        dateTime = doc.getDate("DateTime");
                                        if (doc.getString("AttendeeLimit") != null) {
                                            attendeeLimit = Integer.parseInt(doc.getString("AttendeeLimit"));
                                        } else {
                                            attendeeLimit = null;
                                        }
                                        imageURL = Uri.parse(doc.getString("EventPoster"));
                                        description = doc.getString("Description");
                                        geoLocOn = doc.getBoolean("GeoLocOn");
                                        eventID = doc.getId();

                                        Log.d("Firestore", String.format("Event(%s, $s) fetched", eventName, organizerId));

                                        date = Calendar.getInstance();
                                        date.setTime(dateTime);
                                        signedUpEventDataList.add(new Event(eventName, organizerId, location, date, attendeeLimit, imageURL, description, geoLocOn, eventID, placeId));
                                    } else {
                                        Log.d("TAG", "No such document");
                                    }
                                    eventArrayAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            }
        });
        // makes it so that when the image icon is clicked we go to the edit/view profile screen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent,0);
            }
        });
    }

    // loads the profile image from the saved user profile.
    // imageuri references the link or source of where the image originates from such as it could originate from the device or the api call. However it is treated as empty if there is the generic pfp image there.
    // picasso is an external api that helps cache in images and load them to the imageview works on urls as well as internal images
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Reload the profile image if changes were made
            loadProfileImage();
        }
    }
}
