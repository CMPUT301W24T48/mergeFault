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
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
 * This activity displays all currently signed up events for attendees
 * Attendees can view the list of signed up events
 */
public class AttendeeSignedUpEventsActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private SharedPreferences sharedPreferences;
    private ListView signedUpEventsList;
    private EventArrayAdapter eventArrayAdapter;

    private ArrayList<Event> signedUpEventDataList;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;

    private Event selectedEvent;
    private String eventID;
    private String eventName;
    private String organizerId;
    private String location;
    private Date dateTime;
    private Uri imageURL;
    private Integer attendeeLimit;
    private Calendar date;
    private AttendeeMyEventFragment myEventFragment;
    private String description;
    private Boolean geoLocOn;
    private ImageView homeIcon;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signed_up_events);

        profileImageView = findViewById(R.id.pfpImageView);
        signedUpEventsList = findViewById(R.id.myEventListView);
        homeIcon = findViewById(R.id.imageView);
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        loadProfileImage();

        signedUpEventDataList = new ArrayList<Event>();
        eventArrayAdapter = new EventArrayAdapter(this, signedUpEventDataList);
        signedUpEventsList.setAdapter(eventArrayAdapter);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        eventArrayAdapter.notifyDataSetChanged();

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        signedUpEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedEvent = (Event) signedUpEventsList.getItemAtPosition(position);
                myEventFragment = new AttendeeMyEventFragment();
                Bundle bundle = new Bundle();
                bundle.putString("0", selectedEvent.getEventID());
                bundle.putString("1", selectedEvent.getEventName());
                bundle.putString("2", selectedEvent.getLocation());
                bundle.putString("3", format(selectedEvent.getDateTime().getTime()));
                //bundle.putString("4", selectedEvent.getDescription());
                myEventFragment.setArguments(bundle);
                myEventFragment.show(getSupportFragmentManager(), selectedEvent.getEventName());
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
                        attendeeRef = db.collection("events").document(doc.getId()).collection("attendees");
                        attendeeRef.get().addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null) {
                                    for(QueryDocumentSnapshot document : querySnapshot) {
                                        String documentID = document.getId();
                                        if(documentID.equals(sharedPreferences.getString("phonenumber", ""))){
                                            eventName = doc.getString("EventName");
                                            organizerId = doc.getString("OrganizerID");
                                            location = doc.getString("Location");
                                            dateTime = doc.getDate("DateTime");
                                            attendeeLimit = Integer.parseInt(doc.getString("AttendeeLimit"));
                                            imageURL = Uri.parse(doc.getString("EventPoster"));
                                            description = doc.getString("Description");
                                            geoLocOn = doc.getBoolean("GeoLocOn");
                                            eventID = doc.getId();

                                            Log.d("Firestore", String.format("Event(%s, $s) fetched", eventName, organizerId));

                                            date = Calendar.getInstance();
                                            date.setTime(dateTime);
                                            signedUpEventDataList.add(new Event(eventName, organizerId, location, date, attendeeLimit, imageURL, description, geoLocOn, eventID));
                                        }
                                    }
                                    eventArrayAdapter.notifyDataSetChanged();
                                }
                            }
                            else{
                                Exception e = task.getException();
                                if (e != null) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });
        // makes it so that when the image icon is clicked we go to the edit/view profile screen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignedUpEventsActivity.this, AttendeeEditProfileActivity.class);
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
