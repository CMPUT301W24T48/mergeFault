package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
/**
 * Activity for displaying notifications to the attendee.
 * This activity retrieves notifications from Firebase Firestore and displays them in a list view.
 */
public class AttendeeNotifications extends AppCompatActivity {

    private ListView notificationsListView;
    private TextView notificationsSetText;
    private Button cancelButton;
    private ArrayAdapter<String> adapter;

    /**
     * Initializes the activity, sets up UI components, and retrieves notifications from Firestore.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_my_events);

        notificationsListView = findViewById(R.id.myEventListView);
        notificationsSetText = findViewById(R.id.reuseText);
        cancelButton = findViewById(R.id.cancelButton);
        notificationsSetText.setText("Notifications");

        Toast.makeText(this, "Please ensure notification preferences are turned on to receive notifications", Toast.LENGTH_LONG).show();


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        notificationsListView.setAdapter(adapter);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            Log.d("FCM Token", token);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            CollectionReference notificationsRef = db.collection("notifications").document(token).collection("allnotifications");
                            notificationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        List<String> notificationsList = new ArrayList<>();
                                        for (DocumentSnapshot document : task.getResult()) {
                                            String title = document.getString("title");
                                            String message = document.getString("message");
                                            notificationsList.add(title + ": " + message);
                                        }
                                        adapter.addAll(notificationsList);
                                    } else {
                                        return;
                                    }
                                }
                            });

                        } else {
                            Log.e("FCM Token", "Failed to get token");
                        }
                    }
                });

        // Handle cancel button click to close the activity
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




    }
}
