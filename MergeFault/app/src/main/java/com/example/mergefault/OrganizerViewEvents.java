package com.example.mergefault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.content.SharedPreferences;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class OrganizerViewEvents extends AppCompatActivity {
    private ImageView profileImageView;
    private SharedPreferences sharedPreferences;
    private ListView signedUpEventsList;
    private EventArrayAdapter eventArrayAdapter;

    private ArrayList<Event> signedUpEventDataList;
    private FirebaseFirestore db;
    private CollectionReference eventRef;

    private Event selectedEvent;
    private String eventName;
    private String orgName;
    private String location;
    private Date dateTime;
    private Uri imageURL;
    private Integer attendeeLimit;
    private Calendar date;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signed_up_events);

        profileImageView = findViewById(R.id.pfpImageView);
        signedUpEventsList = findViewById(R.id.myEventListView);
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        loadProfileImage();

        signedUpEventDataList = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, signedUpEventDataList);
        signedUpEventsList.setAdapter(eventArrayAdapter);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        eventArrayAdapter.notifyDataSetChanged();

        signedUpEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedEvent = (Event) signedUpEventsList.getItemAtPosition(position);
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
                    signedUpEventDataList.clear();
                    for(QueryDocumentSnapshot doc: value){
                        eventName = doc.getString("EventName");
                        orgName = doc.getString("OrganizerID");
                        location = doc.getString("Location");
                        dateTime = doc.getDate("DateTime");
                        attendeeLimit = 0;  TODO: //Integer.parseInt(doc.getString("AttendeeLimit"));
                        imageURL = Uri.parse(doc.getString("EventPoster"));
                        Log.d("Firestore", String.format("Event(%s, $s) fetched", eventName, orgName));

                        date = Calendar.getInstance();
                        date.setTime(dateTime);

                        signedUpEventDataList.add(new Event(eventName, orgName, location, date, attendeeLimit, imageURL));
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });
        // makes it so that when the image icon is clicked we go to the edit/view profile screen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerViewEvents.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    // loads the profile image from the saved user profile.
    // imageuri references the link or source of where the image originates from such as it could originate from the device or the api call. However it is treated as empty if there is the generic pfp image there.
    // picasso is an external api that helps cache in images and load them to the imageview works on urls as well as internal images
    private void loadProfileImage() {
        String imageUri = sharedPreferences.getString("imageUri", "");
        if (!imageUri.isEmpty()) {
            Picasso.get().load(imageUri).into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.pfp);
        }
    }

    // this is when we return to the activity from another one, essentially the cancel button. When we return to this activity, load the profile image depending upon any changes made to the Uri in the AttendeeEditProfileActivity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadProfileImage();
        }
    }
}
