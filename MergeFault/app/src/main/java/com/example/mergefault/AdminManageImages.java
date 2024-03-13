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

import java.util.ArrayList;

public class AdminManageImages extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;
    private String imageURL;
    private ListView imagesListView;
    private ArrayList<String> eventImages;
    private ArrayList<String> Images;
    private com.example.mergefault.ImagesArrayAdapter ImagesArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_images);

        imagesListView = findViewById(R.id.imageListView);

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
                    Images.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc.getString("AttendeeProfile") !=null) {
                            imageURL = doc.getString("AttendeeProfile");
                            Images.add(imageURL);
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
                        }
                    }
                }
            }
        });
    }
}
