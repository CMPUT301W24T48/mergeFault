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

public class AdminManageEvents extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;
    private String eventName;
    private String organizerId;
    private String placeId;
    private String location;
    private Date dateTime;
    private Uri imageURL;
    private Integer attendeeLimit;
    private Calendar date;
    private String description;
    private Boolean geoLocOn;
    private String eventID;
    private ArrayList<Event> eventDataList;
    private EventArrayAdapter eventArrayAdapter;
    private ListView eventsList;
    private Button cancelButton;
    private ImageView homeButton;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventPosterRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_events);
        eventsList = findViewById(R.id.myEventListView);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);

        db = FirebaseFirestore.getInstance();
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
                        Log.d("Firestore", String.format("Event(%s, $s) fetched", eventName, organizerId));
                        eventID = doc.getString("EventID");
                        placeId = doc.getString("PlaceID");

                        date = Calendar.getInstance();
                        date.setTime(dateTime);

                        eventDataList.add(new Event(eventName, organizerId, location, date, attendeeLimit, imageURL,description,geoLocOn, eventID, placeId));

                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            eventRef.document(eventDataList.get(position).getEventID()).collection("attendees").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Log.d("attendeeId", document.getId());
                                                    eventRef.document(eventDataList.get(position).getEventID()).collection("attendees").document(document.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            eventRef.document(eventDataList.get(position).getEventID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    eventPosterRef = firebaseStorage.getReference().child( "eventPosters/" + eventDataList.get(position).getEventID() + ".jpg");
                                                                    eventPosterRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            Log.d("", "event deleted successfully");
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                            attendeeRef.whereArrayContains("signedInEvents", eventDataList.get(position).getEventID()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot querySnapshot) {
                                    if (!querySnapshot.isEmpty()) {
                                        List<DocumentSnapshot> attendeesThatSignedUp =  querySnapshot.getDocuments();
                                        for (int i = 0; i < attendeesThatSignedUp.size(); i++) {
                                            DocumentSnapshot attendee = attendeesThatSignedUp.get(i);
                                            attendeeRef.document(attendee.getId()).update("signedInEvents", FieldValue.arrayRemove(eventDataList.get(position).getEventID()));
                                        }
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
}
