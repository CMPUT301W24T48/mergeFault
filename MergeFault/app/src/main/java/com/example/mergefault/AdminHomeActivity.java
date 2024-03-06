package com.example.mergefault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
//import android.widget.Switch;
import androidx.annotation.Nullable;
public class AdminHomeActivity extends AppCompatActivity{
    private Button manageEvents;
    private Button manageProfiles;
    private Button manageImages;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_home);

        manageEvents = findViewById(R.id.manageEventsButton);
        manageProfiles = findViewById(R.id.manageProfilesButton);
        manageImages = findViewById(R.id.manageImages);

        manageEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminManageEvents.class);
                startActivity(intent);
            }
        });

        manageProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminManageProfiles.class);
                startActivity(intent);
            }
        });
    }
}
