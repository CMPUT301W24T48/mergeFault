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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @see AttendeeCheckInScreenActivity
 * This activity displays the current list of all events posted on the app
 * Attendees can view all event details and sign up to any event
 */
public class AttendeeBrowsePostedEventsActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private SharedPreferences sharedPreferences;
    private ListView eventsList;
    private EventArrayAdapter eventArrayAdapter;
    private ImageView homeIcon;

    private ArrayList<Event> eventDataList;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;
    private CollectionReference eventAttendeeRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventPosterRef;
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
    private String eventId;
    private Button cancelButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_browse_posted_events);

        profileImageView = findViewById(R.id.pfpImageView);
        eventsList = findViewById(R.id.myEventListView);
        homeIcon = findViewById(R.id.imageView);
        cancelButton = findViewById(R.id.cancelButton);
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");
        firebaseStorage = FirebaseStorage.getInstance();

        loadProfileImage();

        eventDataList = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, eventDataList);
        eventsList.setAdapter(eventArrayAdapter);


        eventArrayAdapter.notifyDataSetChanged();

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeBrowsePostedEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeBrowsePostedEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AttendeeBrowsePostedEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AttendeeBrowsePostedEventsActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedEventId = eventDataList.get(position).getEventID();
                Intent intent = new Intent(AttendeeBrowsePostedEventsActivity.this, AttendeeSignUpActivity.class);
                Log.d("eventId", "eventId: " + selectedEventId);
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
                    eventDataList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Date currentDate = Calendar.getInstance().getTime();
                        if (currentDate.before(doc.getDate("DateTime"))) {
                            eventName = doc.getString("EventName");//doc.getID();
                            eventId = doc.getString("EventID");
                            organizerId = doc.getString("OrganizerID");
                            location = doc.getString("Location");
                            placeId = doc.getString("PlaceID");
                            dateTime = doc.getDate("DateTime");
                            if (doc.getString("AttendeeLimit") != null) {
                                attendeeLimit = Integer.parseInt(doc.getString("AttendeeLimit"));
                            } else {
                                attendeeLimit = null;
                            }
                            if (doc.getString("EventPoster") != null) {
                                imageURL = Uri.parse(doc.getString("EventPoster"));
                            } else {
                                imageURL = null;
                            }
                            description = doc.getString("Description");
                            geoLocOn = doc.getBoolean("GeoLocOn");
                            Log.d("Firestore", String.format("Event(%s, $s) fetched", eventName, organizerId));
                            date = Calendar.getInstance();
                            date.setTime(dateTime);

                            eventDataList.add(new Event(eventName, organizerId, location, date, attendeeLimit, imageURL, description, geoLocOn, eventId, placeId));
                        } else {
                            eventAttendeeRef = eventRef.document(doc.getId()).collection("attendees");
                            eventAttendeeRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            eventRef.document(doc.getId()).collection("attendees").document(document.getId()).delete();
                                        }
                                        eventPosterRef = firebaseStorage.getReference().child( "eventPosters/" + doc.getId() + ".jpg");
                                        eventPosterRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                eventRef.document(doc.getId()).delete();
                                            }
                                        });
                                    }
                                }
                            });
                            eventPosterRef = firebaseStorage.getReference().child( "eventPosters/" + doc.getId() + ".jpg");
                            eventPosterRef.delete();
                        }
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });
        // makes it so that when the image icon is clicked we go to the edit/view profile screen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeBrowsePostedEventsActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 0);
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

