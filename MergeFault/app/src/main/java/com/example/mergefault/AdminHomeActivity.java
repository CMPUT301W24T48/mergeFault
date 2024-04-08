package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity shows the admin what actions available to them and directs them to the subsequent activity
 * @see AdminManageEvents event management
 * @see AdminManageImages image management
 * @see AdminManageProfiles and profile management
 */
public class AdminHomeActivity extends AppCompatActivity{
    private Button manageEvents;
    private Button manageProfiles;
    private Button manageImages;
    private ImageView homeButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_home);

        // Get the necessary objects from the UI
        manageEvents = findViewById(R.id.manageEventsButton);
        manageProfiles = findViewById(R.id.manageProfilesButton);
        manageImages = findViewById(R.id.manageImages);
        homeButton = findViewById(R.id.imageView);

        // Set click listener for the "Manage Events" button
        manageEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminManageEvents.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Manage Profiles" button
        manageProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminManageProfiles.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Manage Images" button
        manageImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminManageImages.class);
                startActivity(intent);
                finish();
            }
        });

        // Set what happens when back button is pressed
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        AdminHomeActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Set click listener for the Logo
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
