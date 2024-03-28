package com.example.mergefault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for attendee sign-up for an event.
 */
public class AttendeeSignUpPage extends AppCompatActivity {

    // Event ID
    private String eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signup_for_event);

        // Get the intent that started this activity
        Intent intent = getIntent();

        // Get the data URI from the intent
        Uri uri = intent.getData();

        // Check if the URI scheme and host match the expected values
        if ("myapp".equals(uri.getScheme()) && "www.lotuseventspromotions.com".equals(uri.getHost())) {
            // Extract event ID from the query parameters of the URI
            eventId = uri.getQueryParameter("eventId");
        }
    }
}
