package com.example.mergefault;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.InputStream;

/**
 * This activity serves as the home screen for attendees.
 * Attendees can view their profile image, view their events, and browse all events from this screen.
 */
public class AttendeeHomeActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private ImageView homeIcon;

    private Button viewMyEvents;
    private Button browseAllEvents;
    private SharedPreferences sharedPreferences;

    private FirebaseFirestore db;
    private CollectionReference attendeeRef;
    ActivityResultLauncher<Intent> profileEditLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            (result) -> {

            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_home);

        viewMyEvents = findViewById(R.id.viewMyEventsButton);
        browseAllEvents = findViewById(R.id.browseEventsButton);
        homeIcon = findViewById(R.id.imageView);

        db = FirebaseFirestore.getInstance();
        attendeeRef = db.collection("attendees");

        // Start recording user information
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        attendeeRef.document(sharedPreferences.getString("attendeeId", null)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (!doc.exists()) {
                        Log.d("clear", "cleared preferences");
                        sharedPreferences.edit().clear().apply();
                    }
                }
            }
        });
        profileImageView = findViewById(R.id.profileImageView);

        // Load the profile image at the top of the screen

        loadProfileImage();

        // Set click listener for the profile image to navigate to the edit/view profile screen
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        // Set click listener for "View My Events" button
        viewMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeSignedUpEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for "Browse All Events" button
        browseAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeBrowsePostedEventsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
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
                            new AttendeeHomeActivity.DownloadImageFromInternet((ImageView) findViewById(R.id.profileImageView)).execute(doc.getString("AttendeeProfile"));
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
    /**
     * Handles the result when returning from another activity.
     * If changes were made to the profile image, reload it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Reload the profile image if changes were made
            loadProfileImage();
        }
    }
}
