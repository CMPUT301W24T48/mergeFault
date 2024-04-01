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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * This activity allows attendees to edit their profile information.
 * They can edit their name, email, phone number, and profile picture.
 */
public class AttendeeEditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageViewProfile;
    private TextView textEditImage;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhoneNumber;
    private Button cancelButton;
    private ImageButton deleteImageButton;

    private SharedPreferences sharedPreferences;
    private String imageUri;
    private FirebaseFirestore db;
    private CollectionReference attendeesRef;
    private String name;
    private String phonenumber;
    private String email;
    private Boolean infoFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_edit_profile);
        db = FirebaseFirestore.getInstance();
        attendeesRef = db.collection("attendees");

        imageViewProfile = findViewById(R.id.imageView);
        textEditImage = findViewById(R.id.editEventPosterText);
        editTextName = findViewById(R.id.attendeeListButton);
        editTextEmail = findViewById(R.id.manageProfilesButton);
        editTextPhoneNumber = findViewById(R.id.eventDetailsButton);
        cancelButton = findViewById(R.id.cancelButton);
        deleteImageButton = findViewById(R.id.deleteImageButton);

        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // Set click listener for editing profile picture
        textEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndOpenGallery();
            }
        });

        // Set click listener for cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });

        // Set click listener for delete image button
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProfilePicture();
            }
        });

        // Load profile data when activity is created
        loadProfileData();
    }

    /**
     * Deletes the profile picture.
     */
    private void deleteProfilePicture() {
        imageViewProfile.setImageResource(R.drawable.pfp);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("imageUri", "");
        editor.apply();
        imageUri = "";
        Toast.makeText(this, "Profile picture deleted", Toast.LENGTH_SHORT).show();

    }

    /**
     * Opens the gallery to select a profile picture.
     */
    private void checkAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            } else {
                openGallery();
            }
        }
    }

    /**
     * Opens the gallery.
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles permission requests for accessing the gallery.
     */
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

    /**
     * Loads profile data from SharedPreferences.
     */
    private void loadProfileData() {

        infoFound = false;
        attendeesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    for(QueryDocumentSnapshot doc: value){
                        if (doc.getId().equals(sharedPreferences.getString("phonenumber", ""))){
                            name = doc.getString("AttendeeName");
                            email = doc.getString("AttendeeEmail");
                            phonenumber = doc.getId();
                            imageUri = doc.getString("AttendeeProfile");
                            infoFound = true;
                        }
                    }
                }
            }
        });
        if(!infoFound){
            name = sharedPreferences.getString("name","");
            email = sharedPreferences.getString("email", "");
            phonenumber = "";
            imageUri = sharedPreferences.getString("imageUri","");
        }
        editTextName.setText(name);
        editTextEmail.setText(email);
        editTextPhoneNumber.setText(phonenumber);
        if (!imageUri.isEmpty()) {
            Picasso.get().load(imageUri).into(imageViewProfile);
        }

    }

    /**
     * Saves the profile.
     */
    private void saveProfile() {
        String url = "https://api.dicebear.com/5.x/pixel-art/png?seed=";
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phonenum = editTextPhoneNumber.getText().toString().trim();
        if (imageUri == null || imageUri.isEmpty()) {
            Picasso.get().load(url + name).into(imageViewProfile);
            imageUri = url + name;
        }
        saveProfileData(getApplicationContext(), name, email, imageUri, phonenum);
        Intent intent = new Intent();
        if (!imageUri.isEmpty()) {
            intent.putExtra("updatedImageUri", imageUri);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("AttendeeProfile", imageUri);
        data.put("AttendeeName", name);
        data.put("AttendeePhoneNumber", phonenum);
        data.put("AttendeeEmail", email);
        if(!phonenum.equals("")){
            attendeesRef.document(phonenum).set(data);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Saves profile data to SharedPreferences.
     *
     * @param context
     * @param name
     * @param email
     * @param imageUri
     * @param phonenum
     */
    private void saveProfileData(Context context, String name, String email, String imageUri, String phonenum) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("imageUri", imageUri);
        editor.putString("phonenumber", phonenum);
        editor.apply();
        if(phonenum.isEmpty()){
            Toast.makeText(context, "Error: No phone number, rest of profile saved", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context, "Profile created and saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the result of selecting an image from the gallery.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
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
