package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

public class AttendeeNotifications extends AppCompatActivity {

    private ListView notificationsListView;
    private TextView notificationsSetText;
    private Button cancelButton;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_my_events);

        notificationsListView = findViewById(R.id.myEventListView);
        notificationsSetText = findViewById(R.id.reuseText);
        cancelButton = findViewById(R.id.cancelButton);
        notificationsSetText.setText("Notifications");


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
                                        // Handle errors
                                    }
                                }
                            });

                        } else {
                            Log.e("FCM Token", "Failed to get token");
                        }
                    }
                });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




    }
}
