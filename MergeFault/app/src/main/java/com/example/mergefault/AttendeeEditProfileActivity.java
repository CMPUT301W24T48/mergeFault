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

        // if someone clicks the edit profile picture it will go to the checkandOpenGallery func
        textEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndOpenGallery();
            }
        });

        // if someone clicks cancel button we will initiate the saveProfile() process.
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });
        // if someone clicks the delete image button it will go the deleteProfilePicture() process
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProfilePicture();
            }
        });
        // we load in the profile data on create so that if the user had already filled info before they can see it already present
        loadProfileData();
    }

    // once the delete button is clicked, it will call the imageUri that has been stored in the userProfile, make it empty so it doesnt call anything, changes the uri locally so that any and all changes will apply to it and then gives a small toast that the pic has been deleted.
    private void deleteProfilePicture() {
        imageViewProfile.setImageResource(R.drawable.pfp);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("imageUri", ""); // Clear the value of "imageUri" in SharedPreferences
        editor.apply();
        imageUri = ""; // Update the imageUri variable
        Toast.makeText(this, "Profile picture deleted", Toast.LENGTH_SHORT).show();
    }


    // Opens the gallery checks what build is being used for the android and then makes sure to check if it has permission from the user to be able to gain access to the storage
    // if successful it will open the gallery
    private void checkAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            } else {
                openGallery();
            }
        }
    }
    // it will open the gallery and then make it so that you can select an image type object
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    // whenever it requests permission this function checks if the request is valid or not and if it is valid it will open the gallery otherwise on error it will provide a toast that the permission has been denied.
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
    // we load the profile data from the saved profile in the phone using shared preferences and set the changes if there are any and also make sure to load the image.
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

    // this saves the profile and makes the api call to cache in the new image if the image profile is empty.
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
        setResult(RESULT_OK, intent); // this is what is sent back to the activity request in the home page
        finish();
    }

    // this saves all the profile data locally on the phone
    private void saveProfileData(Context context, String name, String email, String imageUri) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("imageUri", imageUri);
        editor.apply();
        Toast.makeText(context, "Profile created and saved successfully", Toast.LENGTH_SHORT).show();
    }

    // When this activity is requested it loads the data and the bitmap to help load the image
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
