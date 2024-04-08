package com.example.mergefault;

import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * This Activity displays all of the organizer's current events
 * which are clickable and lead to a new page for more options to be done to the selected event
 */
public class OrganizerViewEventsActivity extends AppCompatActivity {
    private ImageView homeButton;
    private ListView createdEventsList;
    private Button cancelButton;
    private EventArrayAdapter eventArrayAdapter;
    private ArrayList<Event> createdEvents;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private FirebaseStorage firebaseStorage;
    private String organizerId;
    private Event event;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_my_events);

        // Get the necessary objects from the UI
        homeButton = findViewById(R.id.imageView);
        cancelButton = findViewById(R.id.cancelButton);
        createdEventsList = findViewById(R.id.myEventListView);

        // Receive organizerId from the previous activity
        Intent recieverIntent = getIntent();
        organizerId = recieverIntent.getStringExtra("OrganizerID");

        // Set up events array adapter and link it to the listview
        createdEvents = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, createdEvents);
        createdEventsList.setAdapter(eventArrayAdapter);

        // Get instance and reference to the firebase firestore
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        // Get instance to the firebase storage
        firebaseStorage = FirebaseStorage.getInstance();

        // Set up snapshot listener to listen to changes in the event collection on firestore
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null){
                    createdEvents.clear();
                    for(QueryDocumentSnapshot doc: value){
                        // Creates a current time object to see if event has expired
                        Date currentTime = Calendar.getInstance().getTime();
                        if (currentTime.before(doc.getDate("DateTime"))) {
                            // Checks the organizerId and compare it to the deviceId
                            if(Objects.equals(doc.getString("OrganizerID"), organizerId)){
                                // Gets event details using the document snapshot
                                event = getEventFromDoc(doc);
                                // Adds the created event into list
                                createdEvents.add(event);
                            }
                        } else {
                            // Deletes all related data linking to the event on the firebase
                            deleteEventAndAssociation(doc,db,firebaseStorage);
                        }
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        // Set click listener for the Logo
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerHomeActivity to go back to to organizer home
                Intent intent = new Intent(OrganizerViewEventsActivity.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the event list
        createdEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event) createdEventsList.getItemAtPosition(position);
                // Start OrganizerEventOptions to go see more options
                Intent intent = new Intent(OrganizerViewEventsActivity.this, OrganizerEventOptions.class);
                // Passing selected event id and unique identifier for the organizer's device through intent
                intent.putExtra("EventId", selectedEvent.getEventID());
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerHomeActivity to go back to to organizer home
                Intent intent = new Intent(OrganizerViewEventsActivity.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set what happens when back button is pressed
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Start OrganizerHomeActivity to go back to to organizer home
                Intent intent = new Intent(OrganizerViewEventsActivity.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        OrganizerViewEventsActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
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
}
