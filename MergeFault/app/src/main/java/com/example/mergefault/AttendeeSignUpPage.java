package com.example.mergefault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AttendeeSignUpPage extends AppCompatActivity {

    private String eventId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_signup_for_event);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        if ("myapp".equals(uri.getScheme()) && "www.lotuseventspromotions.com".equals(uri.getHost())) {

            eventId = uri.getQueryParameter("eventId");
        }





    }
}
