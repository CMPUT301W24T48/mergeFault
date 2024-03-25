package com.example.mergefault;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This activity displays the list of attendees for an event to the organizer.
 * It also provides functionality to count the number of attendees who have checked in or signed up for the event.
 */
public class OrganizerAttendeeList extends AppCompatActivity{
    private FirebaseFirestore db;
    private ListView attendeeList;
    private TextView checkInCount;
    private TextView signUpCount;
    private ArrayList<String> tempAttendees;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_attendee_list);

        attendeeList = findViewById(R.id.myEventListView);
        checkInCount = findViewById(R.id.checkedInCountText);
        signUpCount = findViewById(R.id.signUpCountText);

        tempAttendees = new ArrayList<>();
        // Temporarily populate with sample data
        Collections.addAll(tempAttendees,"john smith", "gary cole", "mary doe", "tom john");

        db = FirebaseFirestore.getInstance();
        // Get firebase list
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, tempAttendees);
        attendeeList.setAdapter(adapter);
    }

    /**
     * Counts the number of attendees who have checked in and displays the count.
     */
    public void countCheckIn(){
        Integer count = attendeeList.getAdapter().getCount();
        String text = "Check-In Count: "+count.toString();
        checkInCount.setText(text);
    }

    /**
     * Counts the number of attendees who have signed up for the event and displays the count.
     */
    public void countSignUp(){
        // Implementation to count sign-ups goes here
    }
}
