package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

/**
 * This activity shows a list of image in real time on the firebase currently and the admin can delete them
 */
public class AdminManageImages extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;
    private FirebaseStorage firebaseStorage;
    private ListView imagesListView;
    private ArrayList<String[]> images;
    private com.example.mergefault.ImagesArrayAdapter ImagesArrayAdapter;
    private Button cancelButton;
    private ImageView homeButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_images);

        // Get the necessary objects from the UI
        imagesListView = findViewById(R.id.imageListView);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);

        // Get instance and reference to the firebase firestore
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        attendeeRef = db.collection("attendees");

        // Get instance to the firebase storage
        firebaseStorage = FirebaseStorage.getInstance();

        // Set up image array adapter and link it to the listview
        images = new ArrayList<String[]>();
        ImagesArrayAdapter = new ImagesArrayAdapter(this, images);
        imagesListView.setAdapter(ImagesArrayAdapter);

        // Set up snapshot listener to listen to changes in the attendee collection on firestore
        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                // Update images list and notify adapter of changes
                updateImages();
            }
        });

        // Set up snapshot listener to listen to changes in the event collection on firestore
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                // Update images list and notify adapter of changes
                updateImages();
            }
        });

        // Set click listener for the "Cancel" button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminManageImages.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // Set what happens when back button is pressed
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AdminManageImages.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AdminManageImages.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Set click listener for the Logo
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminManageImages.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * This method gets all the attendee profiles and event posters from events and attendees that still have their images and adds it into a list
     */
    private void updateImages() {
        // Clear the images list
        images.clear();

        // Update images from attendees collection
        db.collection("attendees").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot attendeeSnapshot) {
                for (QueryDocumentSnapshot doc : attendeeSnapshot) {
                    if (doc.getString("AttendeeProfile") != null) {
                        String[] image = {doc.getString("AttendeeProfile"), "AttendeeProfile", doc.getId()};
                        // Check if the image already exists
                        if (!containsImage(images, image)) {
                            images.add(image);
                        }
                    }
                }

                // Update images from events collection
                db.collection("events").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot eventSnapshot) {
                        for (QueryDocumentSnapshot doc : eventSnapshot) {
                            if (doc.getString("EventPoster") != null) {
                                String[] image = {doc.getString("EventPoster"), "EventPoster", doc.getId()};
                                // Check if the image already exists
                                if (!containsImage(images, image)) {
                                    images.add(image);
                                }
                            }
                        }
                        ImagesArrayAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     * Takes in an array list of string arrays and a string array to see if said string array already exists within the array list
     * @param images This is the array list that is given
     * @param image This is the string array that the method checks if images already contains
     * @return This is a boolean to whether the array list contains the string array
     */
    private boolean containsImage(ArrayList<String[]> images, String[] image) {
        for (String[] existingImage : images) {
            if (existingImage[0].equals(image[0])) {
                return true;
            }
        }
        return false;
    }
}
