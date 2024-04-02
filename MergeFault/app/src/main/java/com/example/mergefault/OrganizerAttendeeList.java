package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * This activity displays the list of attendees for an event to the organizer.
 * It also provides functionality to count the number of attendees who have checked in or signed up for the event.
 */
public class OrganizerAttendeeList extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference attendeeRef;
    private CollectionReference eventAttendeeRef;
    private String name;
    private Integer phoneNum;
    private String emailId;
    private String eventId;
    private String organizerId;
    private Boolean notificationPref;
    private Boolean geolocationPref;
    private String profImageURL;
    private ArrayList<Attendee> attendees;
    private AttendeeArrayAdapter attendeeArrayAdapter;
    private ListView attendeeList;
    private TextView checkInCount;
    private TextView signUpCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_attendee_list);
        attendeeList = findViewById(R.id.myEventListView);
        checkInCount = findViewById(R.id.checkedInCountText);
        signUpCount = findViewById(R.id.signUpCountText);

        Intent receiverIntent = getIntent();
        eventId = receiverIntent.getStringExtra("EventId");
        organizerId = receiverIntent.getStringExtra("OrganizerID");

        db = FirebaseFirestore.getInstance();
        eventAttendeeRef = db.collection("events").document(eventId).collection("attendees");
        attendees = new ArrayList<Attendee>();
        String user = "organizer";
        attendeeArrayAdapter = new AttendeeArrayAdapter(this,attendees,user);
        attendeeList.setAdapter(attendeeArrayAdapter);

        eventAttendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    attendees.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        name = doc.getString("AttendeeName");
                        phoneNum = Integer.parseInt(doc.getString("AttendeePhoneNumber"));
                        emailId = doc.getString("AttendeeEmail");
                        profImageURL = doc.getString("AttendeeProfile");

                        attendees.add(new Attendee(name, phoneNum, emailId, false, false, profImageURL, 0));
                    }
                    attendeeArrayAdapter.notifyDataSetChanged();
                }

            }
        });

    }
}
