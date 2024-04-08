package com.example.mergefault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeSignUpActivity extends AppCompatActivity {

    // Event ID
    private String eventId;
    private String parentActivity;
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
    private ImageView eventPoster;
    private SharedPreferences sharedPreferences;
    private ImageView notificationButton;
    private String eventName;
    /**
     * @see AttendeeBrowsePostedEventsActivity
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
        eventPoster = findViewById(R.id.eventPoster);
        notificationButton = findViewById(R.id.notifBellImageView);
        // Get the intent that started this activity
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        parentActivity = intent.getStringExtra("parentActivity");
        Log.d("eventId", "eventId: " + eventId);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        eventAttendeeRef = eventRef.document(eventId).collection("attendees");

        loadProfileImage();
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null){
                    for(QueryDocumentSnapshot doc: value) {
                        if(Objects.equals(doc.getString("EventID"), eventId)) {
                            eventName = doc.getString("EventName");
                            location.setText(doc.getString("Location"));
                            description.setText(doc.getString("Description"));
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMM dd hh:mm a z");
                            String dateString = simpleDateFormat.format(doc.getDate("DateTime"));
                            time.setText(dateString);
                            if (doc.getString("EventPoster") != null) {
                                Picasso.get().load(doc.getString("EventPoster")).into(eventPoster);
                            }
                        }
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(parentActivity, "AttendeeHome")) {
                    Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    switchActivities();
                }
            }
        });

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Objects.equals(parentActivity, "AttendeeHome")) {
                    Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    switchActivities();
                }
            }
        };
        AttendeeSignUpActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeNotifications.class);
                startActivity(intent);

            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubscribeAttendee();
                AddAttendee();
                SendNotificationToOrganizer();



            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 0);
            }
        });

    }

    /**
     * Adds attendee and their information to the event upon signup button click with a unique ID
     */
    public void AddAttendee() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("CheckedIn", false);
        data.put("CheckedInCount", 0);
        //data.put("AttendeeNotificationPref", attendee.getNotificationPref());
        //data.put("AttendeeGeolocationPref", attendee.getGeolocationPref());
        eventAttendeeRef.document(sharedPreferences.getString("attendeeId", null)).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                attendeeRef.document(sharedPreferences.getString("attendeeId", null)).update("signedInEvents", FieldValue.arrayUnion(eventId));
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
    public void SendNotificationToOrganizer(){
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        String topic = eventId + "_organizer";
        try {
            json.put("to", "/topics/" + topic);
            JSONObject notification = new JSONObject();
            notification.put("title", eventName);
            notification.put("body", "You have a new sign-up for your event!");
            json.put("notification", notification);
        } catch (JSONException e) {
            return;

        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .addHeader("Authorization", "key=AAAAJKAW9vA:APA91bG2WW61c9h2OVwu4A4eg6wLiHfPGLNTA517lEj-s66ywb6VxLcAGv0jHRKWMy3XLf0oE9vdZUBG7hnqjNZuukAs6FNCkdU8Pj6afTLGPPAKh3wH6aC54ev5OkG0rpqMUVI2Dhr2")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("FCM_RESPONSE", "Response: " + responseBody);


                } else {
                    String errorResponse = response.body().string();
                    String status = response.code() + " " + response.message();
                    Log.e("FCM_RESPONSE", "Unsuccessful response: " + status);
                    Log.e("FCM_RESPONSE", "Error Body: " + errorResponse);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("FCM_RESPONSE", "Request failed: " + e.getMessage());
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
                            Picasso.get().load(doc.getString("AttendeeProfile")).into(profileImageView);
                        }
                    }
                }
            }
        });
    }
    public void switchActivities() {
        Intent intent = new Intent(AttendeeSignUpActivity.this, AttendeeBrowsePostedEventsActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Reload the profile image if changes were made
            loadProfileImage();
        }
    }

}

