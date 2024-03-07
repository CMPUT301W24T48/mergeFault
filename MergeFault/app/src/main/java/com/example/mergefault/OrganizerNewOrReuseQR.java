package com.example.mergefault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerNewOrReuseQR extends AppCompatActivity {

    private Button generateNewQR;
    private String eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_generate_or_reuse);

        generateNewQR = findViewById(R.id.generateNewButton);

        Intent recieverIntent = getIntent();
        eventId = recieverIntent.getStringExtra("EventId");
        Log.d("eventIdAfter", "eventid:" + eventId);
        generateNewQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OrganizerNewOrReuseQR.this, OrganizerGeneratedQR.class);
                intent.putExtra("EventId", eventId);
                startActivity(intent);
            }
        });
    }
}
