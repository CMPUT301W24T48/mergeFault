package com.example.mergefault;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * This activity allows attendees to edit their profile information.
 * They can edit their name, email, phone number, and profile picture.
 */
public class AttendeeEditProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private TextView textEditImage;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhoneNumber;
    private Button cancelButton;
    private ImageView homeButton;
    private ImageButton deleteImageButton;
    private SwitchCompat geoLocSwitch;
    private SwitchCompat notifSwitch;
    private SharedPreferences sharedPreferences;
    private Uri imageUri;
    private FirebaseFirestore db;
    private CollectionReference attendeesRef;
    private String name;
    private String phonenumber;
    private String email;
    private Boolean geoLocChecked;
    private Boolean notifChecked;
    private String attendeeId;

    private ImageView notificationButton;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE=99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_edit_profile);
        db = FirebaseFirestore.getInstance();
        attendeesRef = db.collection("attendees");

        imageViewProfile = findViewById(R.id.imageView);
        textEditImage = findViewById(R.id.editProfilePictureButton);
        editTextName = findViewById(R.id.editAttendeeName);
        editTextEmail = findViewById(R.id.editEmailText);
        editTextPhoneNumber = findViewById(R.id.editPhoneNumber);
        cancelButton = findViewById(R.id.cancelButton);
        deleteImageButton = findViewById(R.id.deleteImageButton);
        homeButton = findViewById(R.id.imageView2);
        geoLocSwitch = findViewById(R.id.geolocationTrackSwitch);
        notifSwitch = findViewById(R.id.notifSwitch);
        notificationButton = findViewById(R.id.notifBellImageView);

        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // Load profile data when activity is created
        loadProfileData();

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeEditProfileActivity.this, AttendeeNotifications.class);
                startActivity(intent);

            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(2);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!editTextName.getText().toString().equals("") && !editTextEmail.getText().toString().equals("")) {
                    saveProfile();
                } else {
                    Toast.makeText(getApplicationContext(), "Did not enter all required info, Profile not saved", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    switchActivities();
                }
            }
        };
        AttendeeEditProfileActivity.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Set click listener for cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editTextName.getText().toString().equals("") && !editTextEmail.getText().toString().equals("")) {
                    saveProfile();
                } else {
                    Toast.makeText(getApplicationContext(), "Did not enter all required info, Profile not saved", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    switchActivities();
                }

            }
        });
        // Set click listener for editing profile picture
        textEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickingImage.launch("image/*");
            }
        });
        // Set click listener for delete image button
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProfilePicture();
            }
        });

        geoLocSwitch = findViewById(R.id.geolocationTrackSwitch);
        geoLocSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestLocationPermission();
            } else {
                geoLocSwitch.setChecked(false);
                Toast.makeText(this, "No permission please manage in settings", Toast.LENGTH_SHORT).show();
            }
        });

        notifSwitch = findViewById(R.id.notifSwitch);
        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestNotificationPermission();

            } else {
                notifSwitch.setChecked(false);
                Toast.makeText(this, "No permission please manage in settings", Toast.LENGTH_SHORT).show();
            }
        });

    }



    /**
     * Deletes the profile picture.
     */
    private void deleteProfilePicture() {
        //imageViewProfile.setImageResource(R.id.d);
        imageViewProfile.setImageResource(R.drawable.pfp);
        imageUri = null;
        Toast.makeText(this, "Profile picture deleted", Toast.LENGTH_SHORT).show();

    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
    /**
     * Request notification permission if the attendee enables notifications.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Handle permission request results.
     *
     * @param requestCode  The request code passed to requestPermissions().
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                geoLocSwitch.setChecked(false);
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                geoLocSwitch.setChecked(false);

            }
        }
    }



    private final ActivityResultLauncher<String> startPickingImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        imageUri = uri;
                        imageViewProfile.setImageURI(imageUri);
                    }
                }
            }
    );

    /**
     * Loads profile data from SharedPreferences.
     */
    private void loadProfileData() {

        name = sharedPreferences.getString("name", null);
        email = sharedPreferences.getString("email", null);
        String imageUriString = sharedPreferences.getString("imageUri", null);
        attendeeId = sharedPreferences.getString("attendeeId", null);
        phonenumber = sharedPreferences.getString("phonenumber", null);
        geoLocChecked = sharedPreferences.getBoolean("geoLocChecked", false);
        notifChecked = sharedPreferences.getBoolean("notifSwitchChecked", false);
        Log.d("Checked", "Geo: " + geoLocChecked + " Notif: " + notifChecked);

        if (name != null) {
            editTextName.setText(name);
        }
        if (email != null) {
            editTextEmail.setText(email);
        }
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
            Picasso.get().load(imageUri).into(imageViewProfile);
        }
        if (phonenumber != null) {
            editTextPhoneNumber.setText(phonenumber);
        }
        geoLocSwitch.setChecked(geoLocChecked);
        notifSwitch.setChecked(notifChecked);

    }

    /**
     * Saves the profile.
     */
    private void saveProfile() {
        String url = "https://api.dicebear.com/5.x/pixel-art/png?seed=";
        name = editTextName.getText().toString().trim();
        email = editTextEmail.getText().toString().trim();
        if (!editTextPhoneNumber.getText().toString().equals("")) {
            phonenumber = editTextPhoneNumber.getText().toString().trim();
        } else {
            phonenumber = null;
        }
        geoLocChecked = geoLocSwitch.isChecked();
        notifChecked = notifSwitch.isChecked();

        if (imageUri == null) {
            Picasso.get().load(url + name).into(imageViewProfile);
            imageUri = Uri.parse(url + name);
        }
        Picasso.get().load(imageUri).into(imageViewProfile);

        if (attendeeId != null) {
            DocumentReference doc = attendeesRef.document(attendeeId);
            doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("LoadProfile", "attendee in firebase");
                            doc.update("AttendeeProfile", imageUri);
                            doc.update("AttendeeName", name);
                            doc.update("AttendeePhoneNumber", phonenumber);
                            doc.update("AttendeeEmail", email);
                            doc.update("geoLocChecked", geoLocChecked);
                            doc.update("notifChecked", notifChecked);
                            saveProfileData(getApplicationContext(), name, email, imageUri, phonenumber, geoLocChecked, notifChecked, attendeeId);
                        }
                    }
                }
            });
        }
        else {
            HashMap<String, Object> data = new HashMap<>();
            data.put("AttendeeProfile", imageUri);
            data.put("AttendeeName", name);
            data.put("AttendeePhoneNumber", phonenumber);
            data.put("AttendeeEmail", email);
            data.put("geoLocChecked", geoLocChecked);
            data.put("notifChecked", notifChecked);
            attendeesRef.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    attendeeId = documentReference.getId();
                    saveProfileData(getApplicationContext(), name, email, imageUri, phonenumber, geoLocChecked, notifChecked,attendeeId);
                }
            });
        }

    }

    /**
     * Saves profile data to SharedPreferences.
     *
     * @param context
     * @param name
     * @param email
     * @param imageUri
     * @param phonenum
     * @param geoLocChecked
     * @param notifSwitchChecked
     * @param attendeeId
     */
    private void saveProfileData(Context context, String name, String email, Uri imageUri, String phonenum, Boolean geoLocChecked, Boolean notifSwitchChecked, String attendeeId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("imageUri", imageUri.toString());
        editor.putString("phonenumber", phonenum);
        editor.putString("attendeeId", attendeeId);
        editor.putBoolean("geoLocChecked", geoLocChecked);
        editor.putBoolean("notifSwitchChecked", notifSwitchChecked);

        editor.apply();
        Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        switchActivities();
    }

    private void switchActivities(){
        finish();
    }
}