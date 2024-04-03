package com.example.mergefault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

public class OrganizerEventOptions extends AppCompatActivity {

    private String eventId;
    private ImageView homeButton;
    private FirebaseFirestore db;
    private DocumentReference eventRef;
    private Event event;
    private String organizerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_edit_event);

        Intent receiverIntent = getIntent();
        eventId = receiverIntent.getStringExtra("EventId");
        organizerId = receiverIntent.getStringExtra("OrganizerID");

        Log.d("eventId", "eventid: " + eventId);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Calendar date = Calendar.getInstance();
                        Date dateTime = doc.getDate("DateTime");
                        date.setTime(dateTime);
                        Integer attendeeLimit = null;
                        if (doc.getString("AttendeeLimit") != null) {
                            attendeeLimit = Integer.parseInt(doc.getString("AttendeeLimit"));
                        }
                        Uri downloadUrl = Uri.parse(doc.getString("EventPoster"));
                        event = new Event(doc.getString("EventName"),
                                doc.getString("OrganizerID"),
                                doc.getString("Location"),
                                date,
                                attendeeLimit,
                                downloadUrl,
                                doc.getString("Description"),
                                doc.getBoolean("GeoLocOn"),
                                doc.getId(),
                                doc.getString("PlaceID"));
                        new DownloadImageFromInternet((ImageView) findViewById(R.id.eventPosterImageView)).execute(downloadUrl.toString());
                    }
                }
            }
        });

        //set up buttons
        Button attendeeListButton = findViewById(R.id.attendeeListButton);
        Button eventDetailsButton = findViewById(R.id.eventDetailsButton);
        Button sendNotificationsButton = findViewById(R.id.sendNotifButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button mapButton = findViewById(R.id.checkinMapButton);
        Button removeButton = findViewById(R.id.removeEventButton);
        Button shareButton = findViewById(R.id.shareQR);
        homeButton = findViewById(R.id.logoImageView);
        /// TODO: 20-03-2024 add map button

        // Attendee List button click
        attendeeListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerAttendeeList.class);
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerShareQR.class);
                intent.putExtra("EventId", eventId);
                intent.putExtra("PrevActivity", "OrganizerEventOptions");
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
            }
        });

        // Event Details button click
        eventDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerEditEventActivity.class);
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        });

        // Send Notification button click
        sendNotificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerEventNotification.class);
                intent.putExtra("EventId", eventId);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OrganizerEventOptions.this, MapActivity.class);
                intent.putExtra("placeID", event.getPlaceId());
                intent.putExtra("eventPosterUri", event.getEventPoster().toString());
                startActivity(intent);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Event Deleted",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerViewEventsActivity.class);
                                intent.putExtra("OrganizerID", organizerId);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Could not delete event",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerViewEventsActivity.class);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        };
        OrganizerEventOptions.this.getOnBackPressedDispatcher().addCallback(this, callback);

        //Cancel button click
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, OrganizerViewEventsActivity.class);
                intent.putExtra("OrganizerID", organizerId);
                startActivity(intent);
                finish();
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventOptions.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
            Toast.makeText(getApplicationContext(), "Please wait, it may take a few seconds...", Toast.LENGTH_SHORT).show();
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
}


