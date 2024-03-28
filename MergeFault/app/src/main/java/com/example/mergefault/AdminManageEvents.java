package com.example.mergefault;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AdminManageEvents extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference eventRef;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_events);
        eventsList = findViewById(R.id.myEventListView);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
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
                        attendeeLimit = Integer.parseInt(doc.getString("AttendeeLimit"));
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
    }
}
