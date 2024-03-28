package com.example.mergefault;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdminManageImages extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;
    private String imageURL;
    private ListView imagesListView;
    private ArrayList<String> eventIDs;
    private ArrayList<String> attendeeIDS;
    private ArrayList<String> Images;
    private com.example.mergefault.ImagesArrayAdapter ImagesArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_images);

        imagesListView = findViewById(R.id.imageListView);
        eventIDs = new ArrayList<String>();
        attendeeIDS = new ArrayList<String>();
        Images = new ArrayList<String>();
        ImagesArrayAdapter = new ImagesArrayAdapter(this, Images);


        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");

        imagesListView.setAdapter(ImagesArrayAdapter);
        ImagesArrayAdapter.notifyDataSetChanged();

        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc.getString("AttendeeProfile") !=null) {
                            imageURL = doc.getString("AttendeeProfile");
                            Images.add(imageURL);
                            attendeeIDS.add(doc.getString("AttendeePhoneNumber"));
                        }
                    }
                    ImagesArrayAdapter.notifyDataSetChanged();
                }
            }
        });
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    for(QueryDocumentSnapshot doc : value){
                        if(doc.getString("EventPoster") !=null){
                            imageURL = doc.getString("EventPoster");
                            Images.add(imageURL);
                            eventIDs.add(doc.getString("EventID"));
                        }
                    }
                }
            }
        });
        imagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (eventIDs.size() != 0 && Images.size() != 0) {
                    DocumentReference tempRef = db.collection("events").document(eventIDs.get(position));
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("EventPoster", "");
                    tempRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ImagesArrayAdapter.notifyDataSetChanged();
                        }
                    });
                    Images.remove(position);
                    ImagesArrayAdapter.notifyDataSetChanged();
                }
                if (attendeeIDS.size() != 0 && Images.size() != 0){
                    DocumentReference tempRef = db.collection("attendees").document(attendeeIDS.get(position));
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("AttendeeProfile", FieldValue.delete());
                    tempRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ImagesArrayAdapter.notifyDataSetChanged();
                        }
                    });
                    Images.remove(position);
                    ImagesArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
