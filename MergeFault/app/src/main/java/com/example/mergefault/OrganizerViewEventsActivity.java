package com.example.mergefault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class OrganizerViewEventsActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private ImageView homeButton;
    private SharedPreferences sharedPreferences;
    private ListView createdEventsList;
    private Button cancelButton;
    private EventArrayAdapter eventArrayAdapter;

    private ArrayList<Event> createdEvents;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;

    private Event selectedEvent;
    private String eventName;
    private String orgName;
    private String location;
    private String placeId;
    private Date dateTime;
    private Uri imageURL;
    private Integer attendeeLimit;
    private Calendar date;
    private String description;
    private Boolean geoLocOn;
    private String eventID;
    private String organizerId;
    private Boolean geoLocation;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signed_up_events);

        profileImageView = findViewById(R.id.pfpImageView);
        homeButton = findViewById(R.id.imageView);
        cancelButton = findViewById(R.id.cancelButton);
        createdEventsList = findViewById(R.id.myEventListView);
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        createdEvents = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, createdEvents);
        createdEventsList.setAdapter(eventArrayAdapter);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");

        eventArrayAdapter.notifyDataSetChanged();
        Intent recieverIntent = getIntent();
        organizerId = recieverIntent.getStringExtra("OrganizerID");

        loadProfileImage();

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
                        if(Objects.equals(doc.getString("OrganizerID"), organizerId)){
                            eventName = doc.getString("EventName");
                            orgName = doc.getString("OrganizerID");
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
                            eventID = doc.getString("EventID");
                            Log.d("Firestore", String.format("Event(%s, $s) fetched", eventName, orgName));

                            date = Calendar.getInstance();
                            date.setTime(dateTime);

                            createdEvents.add(new Event(eventName, orgName, location, date, attendeeLimit, imageURL,description,geoLocOn,eventID, placeId));
                        }
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });
        // makes it so that when the image icon is clicked we go to the edit/view profile screen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerViewEventsActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 1);
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

    // loads the profile image from the saved user profile.
    // imageuri references the link or source of where the image originates from such as it could originate from the device or the api call. However it is treated as empty if there is the generic pfp image there.
    // picasso is an external api that helps cache in images and load them to the imageview works on urls as well as internal images
    private void loadProfileImage() {
        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getId().equals(sharedPreferences.getString("attendeeId", null))) {
                        if (doc.getString("AttendeeProfile") != null) {
                            new OrganizerViewEventsActivity.DownloadImageFromInternet((ImageView) findViewById(R.id.pfpImageView)).execute(doc.getString("AttendeeProfile"));
                        }
                    }
                }
            }
        });
    }
    class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    // this is when we return to the activity from another one, essentially the cancel button. When we return to this activity, load the profile image depending upon any changes made to the Uri in the AttendeeEditProfileActivity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Reload the profile image if changes were made
            loadProfileImage();
        }
    }

}
