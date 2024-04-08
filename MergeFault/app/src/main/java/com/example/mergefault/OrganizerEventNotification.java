package com.example.mergefault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Activity class responsible for sending notifications to participants of an event.
 * It retrieves necessary event details and sends a notification to the specified event topic on FCM.
 */
public class OrganizerEventNotification extends AppCompatActivity {
    private FirebaseFirestore db;
    private String eventName;
    private TextView setText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_notification);
        Intent receiverIntent = getIntent();
        String eventID = receiverIntent.getStringExtra("EventId");
        String organizerID = receiverIntent.getStringExtra("OrganizerID");

        setText = findViewById(R.id.setText);
        setText.setVisibility(View.INVISIBLE);

        db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventID);

        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d("EventName", "Error getting document: " + task.getException());
                    return;
                }
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    eventName = doc.getString("EventName");

                } else {
                    Log.d("EventName", "Document does not exist");
                }
            }
        });



        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText msgInput = findViewById(R.id.eventEditText);
                String message = msgInput.getText().toString().trim();

                boolean valid = false;
                if (!message.isEmpty()) {
                    sendNotification(message, eventName, eventID);
                    msgInput.setText("");
                    valid = true;

                }
                if (!valid){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setText.setVisibility(View.VISIBLE);
                            setText.setText("Please enter all required fields.");
                        }
                    });
                }


            }
        });
        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerEventNotification.this, OrganizerEventOptions.class);
                intent.putExtra("EventId", eventID);
                intent.putExtra("OrganizerID", organizerID);
                startActivity(intent);
                finish();

            }
        });
    }

    /**
     * Sends a notification to the participants of the event using Firebase Cloud Messaging (FCM).
     *
     * @param message The message to be sent in the notification.
     * @param title The title of the notification.
     * @param eventID The ID of the event, used to specify the topic for FCM.
     */
    public void sendNotification(String message, String title, String eventID){
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("to", "/topics/" + eventID);
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", message);
            json.put("notification", notification);
        } catch (JSONException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setText.setVisibility(View.VISIBLE);
                    setText.setText("Notification did not send.");
                }
            });

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
                    Log.d("Notification", "Sent");
                    Log.d("FCM_RESPONSE", "Response: " + responseBody);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setText.setVisibility(View.VISIBLE);
                            setText.setText("Notification sent");
                        }
                    });

                } else {
                    String errorResponse = response.body().string();
                    String status = response.code() + " " + response.message();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setText.setVisibility(View.VISIBLE);
                            setText.setText("Notification did not send.");
                        }
                    });
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


}