package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerHomeActivity extends AppCompatActivity {

    private Button createNewEventButton;
    private Button viewMyEvents;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_home);

        createNewEventButton = findViewById(R.id.createNewEventButton);
        viewMyEvents = findViewById(R.id.viewMyEventsButton);


        createNewEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OrganizerHomeActivity.this, OrganizerAddEventActivity.class);
                String organizerIDString = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                intent.putExtra("OrganizerID", organizerIDString);
                startActivity(intent);
            }
        });
        viewMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerHomeActivity.this, OrganizerViewEvents.class);
                startActivity(intent);
            }
        });
    }


}
