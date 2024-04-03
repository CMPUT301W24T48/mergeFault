package com.example.mergefault;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private String name;
    private Integer phoneNum;
    private String emailId;
    private Boolean notificationPref;
    private Boolean geolocationPref;
    private String profImageURL;
    private String attendeeId;
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
                        geolocationPref = doc.getBoolean("geoLocChecked");
                        notificationPref = doc.getBoolean("notifChecked");
                        attendeeId = doc.getId();

                        attendees.add(new Attendee(name, phoneNum, emailId, notificationPref, geolocationPref, profImageURL, attendeeId));
                    }
                    attendeeArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        attendeeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (attendees.size() != 0){
                    db.collection("attendees").document(attendees.get(position).getPhoneNum().toString()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("","profile deleted successfully");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("","failed to delete profile");
                        }
                    });
                    attendeeArrayAdapter.notifyDataSetChanged();

                    eventAttendeeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e("Firestore", error.toString());
                                return;
                            }
                            if (value != null){
                                for (QueryDocumentSnapshot doc : value){
                                    eventAttendeeRef = db.collection("events").document(doc.getId()).collection("attendees");
                                    eventAttendeeRef.get().addOnCompleteListener(task -> {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (querySnapshot != null){
                                            for (QueryDocumentSnapshot document : querySnapshot){
                                                if (doc.getString("AttendeeID") == attendees.get(position).getPhoneNum().toString()){
                                                    db.collection("events").document(attendees.get(position).getPhoneNum().toString()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Log.d("","deleted profile from all events");
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("", "failed to delete profile from all events");
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                    attendees.remove(position);
                    attendeeArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
