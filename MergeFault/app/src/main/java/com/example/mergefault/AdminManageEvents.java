package com.example.mergefault;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminManageEvents extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_events);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
    }
}
