package com.example.mergefault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This activity shows the selected event with more options to be done to this event
 * The organizer can choose to view the attendee list, see/edit the event details, send notifications, view check-in map or share the qr codes
 */
public class OrganizerEventOptions extends AppCompatActivity {
    private String eventId;
    private ImageView homeButton;
    private Button attendeeListButton;
    private Button eventDetailsButton;
    private Button sendNotificationsButton;
    private Button cancelButton;
    private Button mapButton;
    private Button removeButton;
    private Button shareButton;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private Event event;
    private String organizerId;
    private ImageView eventPosterImageView;
    private FirebaseStorage firebaseStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_options);

        // Get the necessary objects from the UI
        eventPosterImageView = findViewById(R.id.eventPosterImageView);
        attendeeListButton = findViewById(R.id.attendeeListButton);
        eventDetailsButton = findViewById(R.id.eventDetailsButton);
        sendNotificationsButton = findViewById(R.id.sendNotifButton);
        cancelButton = findViewById(R.id.cancelButton);
        mapButton = findViewById(R.id.checkinMapButton);
        removeButton = findViewById(R.id.removeEventButton);
        shareButton = findViewById(R.id.shareQR);
        homeButton = findViewById(R.id.logoImageView);

        // Receive organizerId and eventId from the previous activity
        Intent receiverIntent = getIntent();
        eventId = receiverIntent.getStringExtra("EventId");
        organizerId = receiverIntent.getStringExtra("OrganizerID");

        // Get instance and reference to the firebase firestore
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        // Get instance to the firebase storage
        firebaseStorage = FirebaseStorage.getInstance();

        // Getting the selected event's details from firestore
        eventRef.document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()){
                        // Gets event details using the document snapshot
                        event = getEventFromDoc(doc);
                        // Using Picasso to load uri onto image view
                        Picasso.get().load(event.getEventPoster()).into(eventPosterImageView);
                    }
                }
            }
        });

        // Set click listener for the "Attendee List" button
        attendeeListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerAttendeeList to go see the event's attendee list
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerAttendeeList.class);
                // Passing selected event id and unique identifier for the organizer's device through intent
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Event Details" button
        eventDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerEditEventActivity to go edit the event details
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerEditEventActivity.class);
                // Passing selected event id and unique identifier for the organizer's device through intent
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Send Notification" button
        sendNotificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerEventNotification to send a notification
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerEventNotification.class);
                // Passing selected event id and unique identifier for the organizer's device through intent
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
            }
        });

        // Set click listener for the "View Check-In Map" button
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MapActivity to go see check-in map of the event
                Intent intent = new Intent(OrganizerEventOptions.this, MapActivity.class);
                // Passing selected event id, unique identifier for the organizer's device and placeId through intent
                intent.putExtra("placeID", event.getPlaceId());
                intent.putExtra("EventId", event.getEventID());
                intent.putExtra("eventPosterUri", event.getEventPoster().toString());
                startActivity(intent);
            }
        });

        // Set click listener for the "Share QR" button
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerShareQR to go share the events check in qr and promotion qr
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerShareQR.class);
                // Passing selected event id, unique identifier for the organizer's device and their activity name through intent
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                intent.putExtra("ParentActivity", "OrganizerEventOptions");
                startActivity(intent);
            }
        });

        // Set click listener for the "Remove Event" button
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventRef.document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            deleteQRs(eventId, firebaseStorage);
                            deleteEventAndAssociation(doc, db, firebaseStorage);
                            Toast.makeText(getApplicationContext(),"Event Deleted",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(OrganizerEventOptions.this, OrganizerViewEventsActivity.class);
                            intent.putExtra("OrganizerID", organizerId);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });

        // Set click listener for the "Cancel" button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerViewEventsActivity to go back to previous activity
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerViewEventsActivity.class);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the Logo
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set what happens when back button is pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Start OrganizerViewEventsActivity to go back to previous activity
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerViewEventsActivity.class);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        };
        OrganizerEventOptions.this.getOnBackPressedDispatcher().addCallback(this, callback);
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
     * This method takes an eventId and a storage instance and finds and deletes the check in and promotion qrs associated with them
     * @param eventId This is the string eventId of the qrs that are going to be deleted
     * @param firebaseStorage This is a instance of the storage
     */
    private void deleteQRs (String eventId, FirebaseStorage firebaseStorage) {
        StorageReference eventCheckInQRRef = firebaseStorage.getReference().child( "QRCodes").child("CheckIn/" + eventId + ".png");
        StorageReference eventPromotionQRRef = firebaseStorage.getReference().child( "QRCodes").child("Promotion/" + eventId + ".png");
        eventCheckInQRRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                eventPromotionQRRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("QRCodes", "Deleted");
                    }
                });
            }
        });
    }
}


