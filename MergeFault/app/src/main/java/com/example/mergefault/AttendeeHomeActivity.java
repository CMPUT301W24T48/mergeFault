package com.example.mergefault;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

public class AttendeeHomeActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_home);

        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        profileImageView = findViewById(R.id.profileImageView);

        loadProfileImage();

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeHomeActivity.this, AttendeeEditProfileActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void loadProfileImage() {
        String imageUri = sharedPreferences.getString("imageUri", "");
        Log.d("ImageUriLoadedInHome:" , imageUri);
        if (!imageUri.isEmpty()) {
            Picasso.get().load(imageUri).into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.pfp);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadProfileImage();
        }
    }
}
