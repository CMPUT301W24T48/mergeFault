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
        Collections.addAll(tempAttendees,"john smith", "gary cole", "mary doe", "tom john");

        db = FirebaseFirestore.getInstance();
        //get firebase list
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, tempAttendees);
        attendeeList.setAdapter(adapter);
    }

    public void countCheckIn(){
        Integer count = attendeeList.getAdapter().getCount();
        String text = "Check-In Count: "+count.toString();
        checkInCount.setText(text);
    }

    public void countSignUp(){

    }

}
