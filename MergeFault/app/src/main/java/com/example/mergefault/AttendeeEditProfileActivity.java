package com.example.mergefault;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;

public class AttendeeEditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageViewProfile;
    private TextView textEditImage;
    private EditText editTextName;
    private EditText editTextEmail;
    private Button cancelButton;
    private ImageButton deleteImageButton;

    private SharedPreferences sharedPreferences;
    private String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_edit_profile);

        imageViewProfile = findViewById(R.id.imageView);
        textEditImage = findViewById(R.id.editEventPosterText);
        editTextName = findViewById(R.id.attendeeListButton);
        editTextEmail = findViewById(R.id.manageProfilesButton);
        cancelButton = findViewById(R.id.cancelButton);
        deleteImageButton = findViewById(R.id.deleteImageButton);

        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        textEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndOpenGallery();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProfilePicture();
            }
        });

        loadProfileData();
    }

    private void deleteProfilePicture() {
        imageViewProfile.setImageResource(R.drawable.pfp);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("imageUri", ""); // Clear the value of "imageUri" in SharedPreferences
        editor.apply();
        imageUri = ""; // Update the imageUri variable
        Toast.makeText(this, "Profile picture deleted", Toast.LENGTH_SHORT).show();
    }

    private void checkAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfileData() {
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        imageUri = sharedPreferences.getString("imageUri", "");
        editTextName.setText(name);
        editTextEmail.setText(email);
        if (!imageUri.isEmpty()) {
            Picasso.get().load(imageUri).into(imageViewProfile);
        }
    }

    private void saveProfile() {
        String url = "https://api.dicebear.com/5.x/pixel-art/png?seed=";
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        if (imageUri == null || imageUri.isEmpty()) {
            Picasso.get().load(url + name).into(imageViewProfile);
            imageUri = url + name;
        }
        saveProfileData(getApplicationContext(), name, email, imageUri);
        Intent intent = new Intent();
        if (!imageUri.isEmpty()) {
            intent.putExtra("updatedImageUri", imageUri);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void saveProfileData(Context context, String name, String email, String imageUri) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("imageUri", imageUri);
        editor.apply();
        Toast.makeText(context, "Profile created and saved successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageViewProfile.setImageBitmap(bitmap);
                        imageViewProfile.setTag(selectedImageUri.toString());
                        imageUri = selectedImageUri.toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
