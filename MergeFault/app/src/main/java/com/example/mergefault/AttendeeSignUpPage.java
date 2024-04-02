package com.example.mergefault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;


/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeSignUpPage extends AppCompatActivity {

    // Event ID
    private String eventId;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference eventAttendeeRef;
    private CollectionReference attendeeRef;
    private TextView location;
    private TextView description;
    private TextView time;
    private Button signUpButton;
    private Button cancelButton;
    private ImageView homeButton;
    private ImageView profileImageView;
    private SharedPreferences sharedPreferences;
    /**
     * this Activity displays event details and a button that signs up attendees to the event
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signup_for_event);
        location = findViewById(R.id.LocationText);
        description = findViewById(R.id.DescriptionText);
        time = findViewById(R.id.TimeText);
        signUpButton = findViewById(R.id.withdrawButton);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);
        profileImageView = findViewById(R.id.ProfilePicture);
        // Get the intent that started this activity
        Intent intent = getIntent();

        // Get the data URI from the intent
        Uri uri = intent.getData();

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        loadProfileImage();
        /*
        if (("myapp".equals(uri.getScheme()) && "www.lotuseventspromotions.com".equals(uri.getHost()))) {
            eventId = uri.getQueryParameter("eventId");
        }
        else {

         */
            Bundle bundle = intent.getExtras();
            eventId = bundle.getString("0");
        //}
        eventAttendeeRef = db.collection("events").document(eventId).collection("attendees");

        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null){
                    for(QueryDocumentSnapshot doc: value) {
                        if(Objects.equals(doc.getString("EventID"), eventId)){
                            location.setText(doc.getString("Location"));
                            description.setText(doc.getString("Description"));
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMM dd hh:mm a z");
                            String dateString = simpleDateFormat.format(doc.getDate("DateTime"));
                            time.setText(dateString);
                            new AttendeeSignUpPage.DownloadImageFromInternet((ImageView) findViewById(R.id.eventPoster)).execute(doc.getString("EventPoster"));
                        }
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchActivities();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                switchActivities();
            }
        };
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpPage.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubscribeAttendee();
                AddAttendee();

            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpPage.this, AttendeeEditProfileActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Adds attendee and their information to the event upon signup button click with a unique ID
     */
    public void AddAttendee() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("AttendeeName", sharedPreferences.getString("name", ""));
        data.put("AttendeePhoneNumber", sharedPreferences.getString("phonenumber", ""));;
        data.put("AttendeeEmail", sharedPreferences.getString("email", ""));
        data.put("AttendeeProfile", sharedPreferences.getString("imageUri", ""));
        data.put("geoLocChecked", sharedPreferences.getBoolean("geoLocChecked", false));
        data.put("notifChecked", sharedPreferences.getBoolean("notifSwitchChecked", false));
        data.put("CheckedIn", false);
        data.put("CheckedInCount", "0");
        //data.put("AttendeeNotificationPref", attendee.getNotificationPref());
        //data.put("AttendeeGeolocationPref", attendee.getGeolocationPref());
        eventAttendeeRef.document(sharedPreferences.getString("attendeeId", null)).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Successfully Signed Up!", Toast.LENGTH_SHORT).show();
                switchActivities();
            }
        });
    }
    public void SubscribeAttendee(){
        FirebaseMessaging.getInstance().subscribeToTopic(eventId)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Subscription successful
                            Log.d("Subscribe","Successfully subscribed to topic: " + eventId);
                        } else {
                            // Subscription failed
                            Log.d("Subscribe","Failed to subscribe to topic: " + eventId);
                            Exception e = task.getException();
                            if (e != null) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


    }
    private void loadProfileImage() {
        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getId().equals(sharedPreferences.getString("attendeeId", null))) {
                        if (doc.getString("AttendeeProfile") != null) {
                            new AttendeeSignUpPage.DownloadImageFromInternet((ImageView) findViewById(R.id.ProfilePicture)).execute(doc.getString("AttendeeProfile"));
                        }
                    }
                }
            }
        });
    }
    class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
            Toast.makeText(getApplicationContext(), "Please wait, it may take a few seconds...", Toast.LENGTH_SHORT).show();
        }
        protected Bitmap doInBackground(String... urls) {
            String imageURL=urls[0];
            Bitmap bimage=null;
            try {
                InputStream in=new java.net.URL(imageURL).openStream();
                bimage= BitmapFactory.decodeStream(in);
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
    public void switchActivities() {
        Intent intent = new Intent(AttendeeSignUpPage.this, AttendeeBrowsePostedEventsActivity.class);
        startActivity(intent);
        finish();
    }

}

