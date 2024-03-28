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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * this fragment displays event details and a button that signs up attendees to the event
 */
public class AttendeeSignUpEventFragment extends DialogFragment {
    private FirebaseFirestore db;
    private CollectionReference attendeeRef;
    private SharedPreferences sharedPreferences;
    private String eventID;
    private String eventName;
    private String eventLocation;
    private String eventDateTime;
    private String attendeeID;




    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
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
                        SubscribeAttendee();
                    }
                })
                .create();
    }

    /**
     * Adds attendee and their information to the event upon signup button click with a unique ID
     */
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
                //TODO: make unique attendee ID for each new attendee
                attendeeID = documentReference.getId();
                data.put("AttendeeID", sharedPreferences.getString("phonenumber", ""));
                documentReference.delete();
                attendeeRef.document(sharedPreferences.getString("phonenumber", "")).set(data);
                Log.d("attendeeIDBefore", "attendeeid" + attendeeID);
            }
        });
    }

    /**
     * Subscribes attendee to event's topic upon signup button
     */
    public void SubscribeAttendee(){
        FirebaseMessaging.getInstance().subscribeToTopic(eventID)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Subscription successful
                            Log.d("SubscribeAttendee","Successfully subscribed to topic: " + eventID);
                        } else {
                            // Subscription failed
                            Log.d("SubscribeAttendee","Failed to subscribe to topic: " + eventID);
                            Exception e = task.getException();
                            if (e != null) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

    }
}