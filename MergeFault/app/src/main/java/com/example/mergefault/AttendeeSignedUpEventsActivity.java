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
    private DocumentReference eventAttendeeDocRef;
    private CollectionReference attendeeRef;
    private FirebaseStorage firebaseStorage;
    private ImageView homeIcon;
    private Button cancelButton;
    private ImageView notificationButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signed_up_events);

        // Get the necessary objects from the UI
        profileImageView = findViewById(R.id.pfpImageView);
        signedUpEventsList = findViewById(R.id.myEventListView);
        homeIcon = findViewById(R.id.imageView);
        profileImageView = findViewById(R.id.pfpImageView);
        cancelButton = findViewById(R.id.cancelButton);
        notificationButton = findViewById(R.id.notifBellImageView);

        // Get shared preferences from device
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // Get instance and reference to the firebase firestore
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");

        // Get instance firebase storage
        firebaseStorage = FirebaseStorage.getInstance();

        // Loads profile image
        loadProfileImage();

        // Set up events array adapter and link it to the listview
        signedUpEventDataList = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, signedUpEventDataList);
        signedUpEventsList.setAdapter(eventArrayAdapter);

        // Set up snapshot listener to listen to changes in the event collection on firestore
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

        // Set click listener for notification icon
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeNotifications.class);
                startActivity(intent);

            }
        });

        // Set click listener for Logo
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for "Cancel" button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set what happens if the back button is pressed
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AttendeeSignedUpEventsActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Set click listener signed up event list
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

        // Set click listener profile icon
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent,0);
            }
        });
    }

    /**
     * loads the profile image from the saved user profile
     */
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

    /**
     * This method takes a document snapshot and creates and returns a new event from data gathered in firebase
     * @param doc This is the document snapshot of the event from firestore
     * @return Returns an event that is created from firebase data
     */
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

    /**
     * This method takes a document snapshot, a instance of firestore and an instance of storage to delete all associated data with the event like attendee sub-collections and event poster
     * @param doc This is the document snapshot of the event from firestore
     * @param db This is an the instance of the firebase firestore
     * @param firebaseStorage This is an instance of the firebase storage
     */
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

    /**
     * This method handles what happens after a activity result is made
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Reload the profile image if changes were made
            loadProfileImage();
        }
    }
}
