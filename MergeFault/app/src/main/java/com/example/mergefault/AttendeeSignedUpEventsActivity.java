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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
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
import java.util.List;

/**
 * @see AttendeeViewEventDetailsActivity
 * This activity displays all currently signed up events for attendees
 * Attendees can view the list of signed up events and click on them to view event details or withdraw
 */
public class AttendeeSignedUpEventsActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private SharedPreferences sharedPreferences;
    private ListView signedUpEventsList;
    private EventArrayAdapter eventArrayAdapter;

    private ArrayList<Event> signedUpEventDataList;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference eventAttendeeRef;
    private DocumentReference eventAttendeeDocRef;
    private CollectionReference attendeeRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventPosterRef;

    private Date dateTime;
    private Calendar date;
    private Event event;

    private ImageView homeIcon;
    private Button cancelButton;
    private ImageView notificationButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signed_up_events);

        profileImageView = findViewById(R.id.pfpImageView);
        signedUpEventsList = findViewById(R.id.myEventListView);
        homeIcon = findViewById(R.id.imageView);
        profileImageView = findViewById(R.id.pfpImageView);
        cancelButton = findViewById(R.id.cancelButton);
        notificationButton = findViewById(R.id.notifBellImageView);
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);


        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");
        firebaseStorage = FirebaseStorage.getInstance();

        loadProfileImage();

        signedUpEventDataList = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, signedUpEventDataList);
        signedUpEventsList.setAdapter(eventArrayAdapter);


        eventArrayAdapter.notifyDataSetChanged();

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeNotifications.class);
                startActivity(intent);

            }
        });

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
                        Date currentTime = Calendar.getInstance().getTime();
                        if (currentTime.before(doc.getDate("DateTime"))) {
                            eventAttendeeDocRef = db.collection("events").document(doc.getId()).collection("attendees").document(sharedPreferences.getString("attendeeId", null));
                            eventAttendeeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if (documentSnapshot.exists()) {
                                            Event event = getEventFromDoc(doc);
                                            signedUpEventDataList.add(event);
                                        }
                                    }
                                    eventArrayAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            Log.d("TAG", "onEvent: starting delete");
                            deleteEventAndAssociation(doc, db, firebaseStorage);
                        }
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

    private Event getEventFromDoc (DocumentSnapshot doc) {
        Event event = new Event();
        event.setEventName(doc.getString("EventName"));
        event.setOrganizerId(doc.getString("OrganizerID"));
        event.setLocation(doc.getString("Location"));
        event.setPlaceId(doc.getString("PlaceID"));
        Date dateTime = doc.getDate("DateTime");
        if (doc.getString("AttendeeLimit") != null) {
            event.setAttendeeLimit(Integer.parseInt(doc.getString("AttendeeLimit")));
        } else {
            event.setAttendeeLimit(null);
        }
        if (doc.getString("EventPoster") != null) {
            event.setEventPoster(Uri.parse(doc.getString("EventPoster")));
        } else {
            event.setEventPoster(null);
        }
        event.setDescription(doc.getString("Description"));
        event.setGeoLocOn(doc.getBoolean("GeoLocOn"));
        event.setEventID(doc.getId());

        Calendar date = Calendar.getInstance();
        date.setTime(dateTime);
        event.setDateTime(date);

        return event;
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
                    StorageReference eventPosterRef = firebaseStorage.getReference().child( "eventPosters/" + doc.getId() + ".jpg");
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
                    List<DocumentSnapshot> attendeesThatSignedUp =  querySnapshot.getDocuments();
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
