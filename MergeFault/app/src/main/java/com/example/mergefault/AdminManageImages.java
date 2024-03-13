package com.example.mergefault;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;

public class AdminManageImages extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;
    private String imageURL;
    private ListView images;
    private ArrayList<String> eventImages;
    private ArrayList<String> attendeeImages;
    private AttendeeImagesArrayAdapter attendeeImagesArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_images);

        images = findViewById(R.id.imageListView);

        eventImages = new ArrayList<String>();

        attendeeImages = new ArrayList<String>();
        attendeeImagesArrayAdapter = new AttendeeImagesArrayAdapter(this,attendeeImages);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");

        images.setAdapter(attendeeImagesArrayAdapter);
        attendeeImagesArrayAdapter.notifyDataSetChanged();

        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    attendeeImages.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc.getString("AttendeeProfile") !=null) {
                            imageURL = doc.getString("AttendeeProfile");
                            attendeeImages.add(imageURL);
                        }
                    }
                    attendeeImagesArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
