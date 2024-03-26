package com.example.mergefault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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

public class OrganizerEventNotification extends AppCompatActivity {
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_notification);
        Bundle extras = getIntent().getExtras();
        String eventID = extras.getString("eventID");
        String eventName = extras.getString("eventName");
        EditText input = findViewById(R.id.eventNameEditText);
        findViewById(R.id.sendNotifButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = input.getText().toString().trim();
                if (!message.equals("")) {
                    sendNotification(message, eventName, eventID);
                }
            }
        });
    }


    public void sendNotification(String message, String eventName, String eventID){
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("to", "/topics/" + eventID);
            JSONObject notification = new JSONObject();
            notification.put("title", eventName);
            notification.put("body", message);
            json.put("notification", notification);
        } catch (JSONException e) {
            throw new RuntimeException(e);
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
                    String errorResponse = response.body().string(); // Read the response body for error details
                    String status = response.code() + " " + response.message(); // HTTP status code and message
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