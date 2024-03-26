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

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class AdminManageProfiles extends AppCompatActivity{

    private FirebaseFirestore db;
    private CollectionReference attendeeRef;
    private String name;
    private Integer phoneNum;
    private String emailId;
    private Boolean notificationPref;
    private Boolean geolocationPref;
    private String profImageURL;
    private ArrayList<Attendee> attendees;
    private AttendeeArrayAdapter attendeeArrayAdapter;
    private ListView attendeeList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_profiles);
        attendeeList = findViewById(R.id.profileLinearLayout);

        db = FirebaseFirestore.getInstance();
        attendeeRef = db.collection("attendees");
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
                        phoneNum = Integer.parseInt(doc.getString("AttendeePhoneNumber"));
                        emailId = doc.getString("AttendeeEmail");
                        profImageURL = doc.getString("AttendeeProfile");

                        attendees.add(new Attendee(name, phoneNum, emailId, false, false, profImageURL, 0));
                    }
                    attendeeArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
