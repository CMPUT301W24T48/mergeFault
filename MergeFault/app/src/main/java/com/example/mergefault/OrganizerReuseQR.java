package com.example.mergefault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class OrganizerReuseQR extends AppCompatActivity {
    private String eventId;
    private Uri selectedImage;
    private FirebaseStorage firebaseStorage;
    private StorageReference eventCheckInQRRef;
    private StorageReference eventPosterRef;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private QRArrayAdapter qrArrayAdapter;
    private ListView prevQRListView;
    private Button cancelButton;
    private ImageView homeButton;
    private ArrayList<String> checkInQRList;
    private ArrayList<String> eventIdList;
    private ArrayList<String> expiredEventIdList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_reuse_qr);

        prevQRListView = findViewById(R.id.prevQRListView);
        homeButton = findViewById(R.id.imageView);
        cancelButton = findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("EventId");
        selectedImage = intent.getData();

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        firebaseStorage = FirebaseStorage.getInstance();
        eventPosterRef = firebaseStorage.getReference().child( "eventPosters/" + eventId + ".jpg");
        eventCheckInQRRef = firebaseStorage.getReference().child("QRCodes").child("CheckIn");

        checkInQRList = new ArrayList<String>();
        eventIdList = new ArrayList<String>();
        expiredEventIdList = new ArrayList<String>();

        qrArrayAdapter = new QRArrayAdapter(this, expiredEventIdList);
        prevQRListView.setAdapter(qrArrayAdapter);
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null){
                    checkInQRList.clear();
                    eventIdList.clear();

                    for (QueryDocumentSnapshot doc : value){
                        eventIdList.add(doc.getId());
                    }
                    eventCheckInQRRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (StorageReference item : listResult.getItems()) {
                                String eventId = item.getName().substring(0,item.getName().indexOf("."));
                                checkInQRList.add(eventId);
                                Log.d("checkInQRList", checkInQRList.toString());
                            }
                            expiredEventIdList.clear(); // Clear the existing data
                            expiredEventIdList.addAll(checkInQRList); // Add all new data
                            expiredEventIdList.removeAll(eventIdList); // Remove expired events
                            qrArrayAdapter.notifyDataSetChanged(); // Notify adapter of changes
                        }
                    });
                }
            }
        });
        prevQRListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                eventPosterRef.getStream().addOnCompleteListener(new OnCompleteListener<StreamDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<StreamDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult();
                        }
                    }
                });
                String selectedEventId = expiredEventIdList.get(position);
                eventRef.document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                HashMap<String, Object> data = new HashMap<>();

                                StorageReference newEventPosterRef = firebaseStorage.getReference().child( "eventPosters/" + selectedEventId + ".jpg");
                                UploadTask uploadTask = newEventPosterRef.putFile(selectedImage);

                                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }

                                        // Continue with the task to get the download URL
                                        return newEventPosterRef.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadUrl = task.getResult();
                                            data.put("EventPoster", downloadUrl);
                                            data.put("Location", doc.get("Location"));
                                            data.put("PlaceID", doc.get("PlaceID"));
                                            data.put("DateTime", doc.get("DateTime"));
                                            data.put("AttendeeLimit", doc.get("AttendeeLimit"));
                                            data.put("EventName", doc.get("EventName"));
                                            data.put("Description", doc.get("Description"));
                                            data.put("GeoLocOn", doc.get("GeoLocOn"));
                                            data.put("OrganizerID", doc.get("OrganizerID"));
                                            data.put("EventID", selectedEventId);
                                            eventRef.document(selectedEventId).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    eventRef.document(eventId).delete();
                                                    eventPosterRef.delete();
                                                    Intent intent = new Intent(OrganizerReuseQR.this, OrganizerShareQR.class);
                                                    intent.putExtra("EventId", selectedEventId);
                                                    intent.putExtra("ParentActivity", "OrganizerReuseQR");
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerReuseQR.this, OrganizerHomeActivity.class);
                eventPosterRef.delete();
                eventRef.document(eventId).delete();
                startActivity(intent);
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerReuseQR.this, OrganizerNewOrReuseQR.class);
                intent.putExtra("EventId", eventId);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(OrganizerReuseQR.this, OrganizerNewOrReuseQR.class);
                intent.putExtra("EventId", eventId);
                startActivity(intent);
                finish();
            }
        };
        OrganizerReuseQR.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }
}
