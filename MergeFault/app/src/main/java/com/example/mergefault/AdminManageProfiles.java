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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AdminManageProfiles extends AppCompatActivity{

    private FirebaseFirestore db;
    private CollectionReference attendeeRef;
    private CollectionReference eventAttendeeRef;
    private CollectionReference eventRef;
    private String name;
    private String phoneNum;
    private String emailId;
    private Boolean notificationPref;
    private Boolean geolocationPref;
    private String profImageURL;
    private String attendeeId;
    private ArrayList<Attendee> attendees;
    private AttendeeArrayAdapter attendeeArrayAdapter;
    private ListView attendeeList;
    private Button cancelButton;
    private ImageView homeButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_profiles);
        attendeeList = findViewById(R.id.profileLinearLayout);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.imageView);


        db = FirebaseFirestore.getInstance();
        attendeeRef = db.collection("attendees");
        eventRef = db.collection("events");

        attendees = new ArrayList<Attendee>();
        String user = "admin";
        attendeeArrayAdapter = new AttendeeArrayAdapter(this,attendees,user);
        attendeeList.setAdapter(attendeeArrayAdapter);

        attendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    attendees.clear();
                    for (QueryDocumentSnapshot doc : value){
                        name = doc.getString("AttendeeName");
                        if (doc.getString("AttendeePhoneNumber") != null) {
                            phoneNum = doc.getString("AttendeePhoneNumber");
                        } else {
                            phoneNum = null;
                        }
                        emailId = doc.getString("AttendeeEmail");
                        profImageURL = doc.getString("AttendeeProfile");
                        geolocationPref = doc.getBoolean("geoLocChecked");
                        notificationPref = doc.getBoolean("notifChecked");
                        attendeeId = doc.getId();

                        attendees.add(new Attendee(name, phoneNum, emailId, notificationPref, geolocationPref, profImageURL, attendeeId));
                    }
                } else if (value.isEmpty()) {
                    attendees.clear();
                }
                attendeeArrayAdapter.notifyDataSetChanged();
            }
        });



        /*
        attendeeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Position", "position: " + position +" size: " + attendees.size());
                if (attendees.size() != 0){
                    DocumentReference attendeeDocRef = db.collection("attendees").document(attendees.get(position).getAttendeeId());
                    attendeeDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.get("signedInEvents") != null){
                                List<String> signedInEvents = (List<String>) documentSnapshot.get("signedInEvents");
                                for (int i = 0; i < signedInEvents.size(); i++) {
                                    db.collection("events").document(signedInEvents.get(i)).collection("attendees").document(attendees.get(position).getAttendeeId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("","deleted profile from all events");
                                        }
                                    });
                                }
                            }
                            attendeeDocRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "profile deleted successfully", Toast.LENGTH_SHORT);
                                    Log.d("","profile deleted successfully");
                                    Log.d("Position", "position: " + position +" size: " + attendees.size());
                                }
                            });
                        }
                    });
                }
            }
        });

         */
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminManageProfiles.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AdminManageProfiles.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AdminManageProfiles.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminManageProfiles.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
