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

/**
 * An Activity which allows admins to view and delete events
 */
public class AdminManageEvents extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;
    private CollectionReference eventAttendeeRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventPosterRef;
    private StorageReference eventCheckInQRRef;
    private StorageReference eventPromotionQRRef;

    private Date dateTime;
    private Calendar date;
    private Event event;

    private ArrayList<Event> eventDataList;
    private EventArrayAdapter eventArrayAdapter;

    private ListView eventsList;
    private Button cancelButton;
    private ImageView homeButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_events);
        eventsList = findViewById(R.id.myEventListView);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");
        eventDataList = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, eventDataList);
        eventsList.setAdapter(eventArrayAdapter);

        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null){
                    eventDataList.clear();
                    for (QueryDocumentSnapshot doc : value){
                        Date currentTime = Calendar.getInstance().getTime();
                        if (currentTime.before(doc.getDate("DateTime"))) {
                            event = getEventFromDoc(doc);
                            eventDataList.add(event);
                        } else {
                            deleteEventAndAssociation(doc, db , firebaseStorage);
                        }
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                eventRef.document(eventDataList.get(position).getEventID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            deleteQRs(doc.getId(), firebaseStorage);
                            deleteEventAndAssociation(doc, db, firebaseStorage);
                        }
                    }
                });
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminManageEvents.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AdminManageEvents.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AdminManageEvents.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminManageEvents.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
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
