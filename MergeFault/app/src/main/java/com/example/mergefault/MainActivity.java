package com.example.mergefault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;

/**
 * Start of the flow of the app
 */
public class MainActivity extends AppCompatActivity {

    private Button attendeeButton;
    private Button organizerButton;
    private Button adminButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Initialize buttons
        attendeeButton = findViewById(R.id.attendeeButton);
        organizerButton = findViewById(R.id.organizerButton);
        adminButton = findViewById(R.id.adminButton);

        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.PLACES_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey) || apiKey.equals("DEFAULT_API_KEY")) {
            Log.e("Places test", "No api key");
            finish();
            return;
        }

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Set OnClickListener for attendeeButton
        attendeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AttendeeHomeActivity when attendeeButton is clicked
                startActivity(new Intent(MainActivity.this, AttendeeHomeActivity.class));
            }
        });

        // Set OnClickListener for organizerButton
        organizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start OrganizerHomeActivity when organizerButton is clicked
                startActivity(new Intent(MainActivity.this, OrganizerHomeActivity.class));
            }
        });

        // Set OnClickListener for adminButton
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AdminHomeActivity when adminButton is clicked
                Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String adminKey = data.getStringExtra("AdminKey");
            //key right now is "Admin"
            /*
            if (Objects.equals(adminKey, "Admin")) {
                Toast.makeText(getApplicationContext(), "Key accepted", Toast.LENGTH_SHORT);
                Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Key not accepted", Toast.LENGTH_SHORT);
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

             */
        } else {
            Toast.makeText(getApplicationContext(),"Scan failed", Toast.LENGTH_SHORT);
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /*

    // Method to validate the deep link
    private boolean isDeepLinkValid(Uri data) {
        // Check if the deep link host and scheme match your criteria
        return "www.lotuseventsadminpermission.com".equals(data.getHost()) && "myapp".equals(data.getScheme());
    }

     */
}
