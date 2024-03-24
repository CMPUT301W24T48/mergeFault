package com.example.mergefault;

import static okhttp3.internal.http.HttpDate.format;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

/**
 * @see AttendeeSignUpEventFragment
 * This activity displays the current list of all events posted on the app
 * Attendees can view all event details and sign up to any event
 */
public class AttendeeBrowsePostedEventsActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private SharedPreferences sharedPreferences;
    private ListView eventsList;
    private EventArrayAdapter eventArrayAdapter;
    private ImageView homeIcon;

    private ArrayList<Event> eventDataList;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeImageRef;

    private Event selectedEvent;
    private String eventName;
    private String organizerId;
    private String location;
    private String placeId;
    private Date dateTime;
    private Uri imageURL;
    private Integer attendeeLimit;
    private Calendar date;
    private String description;
    private Boolean geoLocOn;
    private String eventID;
    private AttendeeSignUpEventFragment eventFragment;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_browse_posted_events);

        profileImageView = findViewById(R.id.pfpImageView);
        eventsList = findViewById(R.id.myEventListView);
        homeIcon = findViewById(R.id.imageView);
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeImageRef = db.collection("attendees");

        loadProfileImage();

        eventDataList = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, eventDataList);
        eventsList.setAdapter(eventArrayAdapter);


        eventArrayAdapter.notifyDataSetChanged();

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeBrowsePostedEventsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedEvent = (Event) eventsList.getItemAtPosition(position);
                eventFragment = new AttendeeSignUpEventFragment();
                Bundle bundle = new Bundle();
                bundle.putString("0", selectedEvent.getEventID());
                bundle.putString("1", selectedEvent.getEventName());
                bundle.putString("2", selectedEvent.getLocation());
                bundle.putString("3", format(selectedEvent.getDateTime().getTime()));
                bundle.putString("4", selectedEvent.getDescription());
                eventFragment.setArguments(bundle);
                eventFragment.show(getSupportFragmentManager(), selectedEvent.getEventName());
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
                    eventDataList.clear();
                    for(QueryDocumentSnapshot doc: value){
                        eventName = doc.getString("EventName");//doc.getID();
                        organizerId = doc.getString("OrganizerID");
                        location = doc.getString("Location");
                        placeId = doc.getString("PlaceID");
                        dateTime = doc.getDate("DateTime");
                        attendeeLimit = Integer.parseInt(doc.getString("AttendeeLimit"));
                        imageURL = Uri.parse(doc.getString("EventPoster"));
                        description = doc.getString("Description");
                        geoLocOn = doc.getBoolean("GeoLocOn");
                        Log.d("Firestore", String.format("Event(%s, $s) fetched", eventName, organizerId));
                        eventID = doc.getString("eventID");

                        date = Calendar.getInstance();
                        date.setTime(dateTime);

                        eventDataList.add(new Event(eventName, organizerId, location, date, attendeeLimit, imageURL,description,geoLocOn, eventID, placeId));
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });
        // makes it so that when the image icon is clicked we go to the edit/view profile screen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeBrowsePostedEventsActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    // loads the profile image from the saved user profile.
    // imageuri references the link or source of where the image originates from such as it could originate from the device or the api call. However it is treated as empty if there is the generic pfp image there.
    // picasso is an external api that helps cache in images and load them to the imageview works on urls as well as internal images
    private void loadProfileImage() {
        attendeeImageRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for(QueryDocumentSnapshot  doc: value){
                    if (doc.getId().equals(sharedPreferences.getString("phonenumber", ""))){
                        if (!doc.getString("AttendeeProfile").isEmpty()) {
                            Picasso.get().load(doc.getString("AttendeeProfile")).into(profileImageView);
                        }
                        break;
                    }
                }
            }
        });
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

