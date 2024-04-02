package com.example.mergefault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.InputStream;
import java.util.Objects;


/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeViewEventDetailsActivity extends AppCompatActivity {

    // Event ID
    private String eventId;
    private FirebaseFirestore db;
    private CollectionReference events;
    private CollectionReference eventAttendeeRef;
    private TextView location;
    private TextView description;
    private TextView time;
    private Button withdrawButton;
    private Button cancelButton;
    private SwitchCompat notifySwitch;
    private ImageView homeButton;
    private SharedPreferences sharedPreferences;

    /**
     * This Activity displays event details and allows users to sign up for notifications or withdraw
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_event_details);

        location = findViewById(R.id.EventDetailsLocationText);
        description = findViewById(R.id.EventDetailsDescriptionText);
        time = findViewById(R.id.EventDetailsTimeText);
        withdrawButton = findViewById(R.id.withdrawButton);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);
        notifySwitch = findViewById(R.id.notifSwitch);

        // Get the intent that started this activity
        Intent intent = getIntent();
        db = FirebaseFirestore.getInstance();
        events = db.collection("events");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        Bundle bundle = intent.getExtras();
        eventId = bundle.getString("0");
        eventAttendeeRef = db.collection("events").document(eventId).collection("attendees");

        events.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        if (Objects.equals(doc.getString("EventID"), eventId)) {
                            location.setText(doc.getString("Location"));
                            description.setText(doc.getString("Description"));
                            time.setText(doc.getDate("DateTime").toString());
                            new AttendeeViewEventDetailsActivity.DownloadImageFromInternet((ImageView) findViewById(R.id.eventPoster)).execute(doc.getString("EventPoster"));
                        }
                    }
                }
            }
        });
        notifySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implement notif stuff
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventAttendeeRef.document(sharedPreferences.getString("attendeeId", "")).delete();
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeViewEventDetailsActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
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

