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
/**
 * this fragment displays event details and a button that withdraws attendees from the event
 */
public class AttendeeMyEventFragment extends DialogFragment {

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
        //eventRef = db.collection("events");
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
                .setPositiveButton("Withdraw", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        attendeeRef.document(sharedPreferences.getString("phonenumber", "")).delete();
                    }
                })
                .create();
    }
}
