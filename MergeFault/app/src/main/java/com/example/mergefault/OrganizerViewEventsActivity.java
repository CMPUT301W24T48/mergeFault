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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * @see OrganizerEventOptions
 * This Activity displays all of the organizer's current events
 * which are clickable and lead to a new page for modification
 */
public class OrganizerViewEventsActivity extends AppCompatActivity {
    private ImageView homeButton;
    private ListView createdEventsList;
    private Button cancelButton;
    private EventArrayAdapter eventArrayAdapter;

    private ArrayList<Event> createdEvents;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference eventAttendeeRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventPosterRef;
    private Date dateTime;
    private Calendar date;
    private String organizerId;
    private Event event;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_my_events);

        homeButton = findViewById(R.id.imageView);
        cancelButton = findViewById(R.id.cancelButton);
        createdEventsList = findViewById(R.id.myEventListView);

        createdEvents = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, createdEvents);
        createdEventsList.setAdapter(eventArrayAdapter);

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        eventRef = db.collection("events");

        eventArrayAdapter.notifyDataSetChanged();
        Intent recieverIntent = getIntent();
        organizerId = recieverIntent.getStringExtra("OrganizerID");

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerViewEventsActivity.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        createdEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event) createdEventsList.getItemAtPosition(position);
                Intent intent = new Intent(OrganizerViewEventsActivity.this, OrganizerEventOptions.class);
                intent.putExtra("EventId", selectedEvent.getEventID());
                intent.putExtra("OrganizerID", organizerId);
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
                if (value != null){
                    createdEvents.clear();
                    for(QueryDocumentSnapshot doc: value){
                        Date currentTime = Calendar.getInstance().getTime();
                        if (currentTime.before(doc.getDate("DateTime"))) {
                            if(Objects.equals(doc.getString("OrganizerID"), organizerId)){
                                event = new Event(null,null,null,null,null,null,null,null,null,null);
                                event.setEventName(doc.getString("EventName"));
                                event.setOrganizerId(doc.getString("OrganizerID"));
                                event.setLocation(doc.getString("Location"));
                                event.setPlaceId(doc.getString("PlaceID"));
                                dateTime = doc.getDate("DateTime");
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

                                Log.d("Firestore", String.format("Event(%s, $s) fetched", event.getEventName(), event.getOrganizerId()));

                                date = Calendar.getInstance();
                                date.setTime(dateTime);
                                createdEvents.add(new Event(event.getEventName(), event.getOrganizerId(), event.getLocation(), date, event.getAttendeeLimit(), event.getEventPoster(), event.getDescription(), event.getGeoLocOn(), event.getEventID(), event.getPlaceId()));
                            }
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
                        }
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerViewEventsActivity.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(OrganizerViewEventsActivity.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        OrganizerViewEventsActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }
}
