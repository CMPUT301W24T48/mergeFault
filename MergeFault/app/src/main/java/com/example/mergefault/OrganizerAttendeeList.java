package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private Integer checkInCount;
    private Boolean checkedIn;
    private String attendeeId;
    private ArrayList<Attendee> attendees;
    private AttendeeArrayAdapter attendeeArrayAdapter;
    private ListView attendeeList;
    private TextView checkInText;
    private TextView signUpText;
    private Integer eventCheckInCount;
    private Integer eventSignUpCount;
    private Button cancelButton;
    private ImageView homeButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_attendee_list);
        attendeeList = findViewById(R.id.myEventListView);
        checkInText = findViewById(R.id.checkInCountText);
        signUpText = findViewById(R.id.signUpCountText);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);

        Intent receiverIntent = getIntent();
        eventId = receiverIntent.getStringExtra("EventId");
        organizerId = receiverIntent.getStringExtra("OrganizerID");

        db = FirebaseFirestore.getInstance();
        eventAttendeeRef = db.collection("events").document(eventId).collection("attendees");
        attendeeRef = db.collection(("attendees"));
        attendees = new ArrayList<Attendee>();
        attendeeArrayAdapter = new AttendeeArrayAdapter(this,attendees,"organizer");
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
                    eventCheckInCount = 0;
                    eventSignUpCount = 0;
                    for (QueryDocumentSnapshot doc : value) {
                        doc.getData();
                        attendeeRef.document(doc.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                name = documentSnapshot.getString("AttendeeName");
                                phoneNum = Integer.parseInt(documentSnapshot.getString("AttendeePhoneNumber"));
                                emailId = documentSnapshot.getString("AttendeeEmail");
                                profImageURL = documentSnapshot.getString("AttendeeProfile");
                                geolocationPref = documentSnapshot.getBoolean("geoLocChecked");
                                notificationPref = documentSnapshot.getBoolean("notifChecked");
                                checkInCount = doc.getLong("CheckedInCount").intValue();
                                checkedIn = doc.getBoolean("CheckedIn");
                                attendeeId = doc.getId();
                                Log.d("attendeeInfo", "info: " + checkInCount + checkedIn);
                                attendees.add(new Attendee(name, phoneNum, emailId, notificationPref, geolocationPref, profImageURL, checkInCount, checkedIn, attendeeId));
                                attendeeArrayAdapter.notifyDataSetChanged();

                                eventCheckInCount += checkInCount;
                                checkInText.setText("Check-In Count: " + eventCheckInCount);
                                signUpText.setText("Sign-Up Count: " + eventSignUpCount);


                            }
                        });
                        eventSignUpCount += 1;
                    }
                    Log.d("Counts", "counts" + eventCheckInCount + eventSignUpCount);
                }

            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerAttendeeList.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(OrganizerAttendeeList.this, OrganizerEventOptions.class);
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        };
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerAttendeeList.this, OrganizerEventOptions.class);
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        });
    }

}
