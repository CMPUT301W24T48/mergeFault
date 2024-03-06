package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerHomeActivity extends AppCompatActivity {

    private Button createNewEventButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_home);

        createNewEventButton = findViewById(R.id.createNewEventButton);


        createNewEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OrganizerHomeActivity.this, OrganizerNewOrReuseQR.class);
                startActivity(intent);
            }
        });
    }


}
