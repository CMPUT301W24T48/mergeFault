package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
public class AdminHomeActivity extends AppCompatActivity{
    private Button manageEvents;
    private Button manageProfiles;
    private Button manageImages;
    private ImageView homeButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_home);

        manageEvents = findViewById(R.id.manageEventsButton);
        manageProfiles = findViewById(R.id.manageProfilesButton);
        manageImages = findViewById(R.id.manageImages);
        homeButton = findViewById(R.id.imageView);

        manageEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminManageEvents.class);
                startActivity(intent);
                finish();
            }
        });

        manageProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminManageProfiles.class);
                startActivity(intent);
                finish();
            }
        });

        manageImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminManageImages.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AdminHomeActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
