package com.example.mergefault;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.ListFragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class AttendeeSignUpEventFragment extends DialogFragment {

    private Button signup;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference attendeeRef;
    private SharedPreferences sharedPreferences;
    private String eventID;
    private String eventName;
    private String eventLocation;
    private String eventDateTime;
    private String imageUri;
    private String attendeeID;




    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_event_details, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        eventID = getArguments().getString("0");
        eventName = getArguments().getString("1");
        eventLocation = getArguments().getString("2");
        eventDateTime = getArguments().getString("3");
        String[] eventDetails = {eventName, eventLocation, eventDateTime};
        attendeeRef = db.collection("events").document(eventID).collection("attendees");
        sharedPreferences = getActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        return builder
                .setView(view)
                .setItems(eventDetails, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddAttendee();
                    }
                })
                .create();
    }
    public void AddAttendee() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("AttendeeName", sharedPreferences.getString("name", ""));
        data.put("AttendeePhoneNumber", sharedPreferences.getString("phonenumber", ""));;
        data.put("AttendeeEmail", sharedPreferences.getString("email", ""));
        data.put("AttendeeProfile", sharedPreferences.getString("imageUri", ""));
        //data.put("AttendeeNotificationPref", attendee.getNotificationPref());
        //data.put("AttendeeGeolocationPref", attendee.getGeolocationPref());
        attendeeRef.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                attendeeID = documentReference.getId();
                data.put("AttendeeID", attendeeID);
                documentReference.delete();
                attendeeRef.document(attendeeID).set(data);
                Log.d("attendeeIDBefore", "attendeeid" + attendeeID);
            }
        });
    }
}