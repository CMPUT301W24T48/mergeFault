package com.example.mergefault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

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
        homeButton = findViewById(R.id.imageView2);
        geoLocSwitch = findViewById(R.id.geolocationTrackSwitch);
        notifSwitch = findViewById(R.id.notifSwitch);

        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // Load profile data when activity is created
        loadProfileData();

        Log.d("attendeeId", attendeeId);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeEditProfileActivity.this, AttendeeHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (name != null && email != null) {
                    saveProfile();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter all required info", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Set click listener for editing profile picture
        textEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickingImage.launch("image/*");
            }
        });

        // Set click listener for cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name != null && email != null) {
                    saveProfile();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter all required info", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Set click listener for delete image button
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedPreferences.getString("imageUri", null) == null){
                    Toast.makeText(getApplicationContext(),"No image to delete", Toast.LENGTH_SHORT);
                } else {
                    deleteProfilePicture();
                }
            }
        });
    }

    /**
     * Deletes the profile picture.
     */
    private void deleteProfilePicture() {
        imageViewProfile.setImageResource(R.drawable.pfp);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("imageUri", null);
        editor.apply();
        if (attendeeId != null) {
            attendeesRef.document(attendeeId).update("AttendeeProfile", null);
        }
        Toast.makeText(this, "Profile picture deleted", Toast.LENGTH_SHORT).show();

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
            new AttendeeEditProfileActivity.DownloadImageFromInternet((ImageView) findViewById(R.id.imageView)).execute(imageUri.toString());
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
        new AttendeeEditProfileActivity.DownloadImageFromInternet((ImageView) findViewById(R.id.imageView)).execute(imageUri.toString());

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
                        saveProfileData(getApplicationContext(), name, email, imageUri, phonenumber, geoLocChecked, notifChecked,attendeeId);
                    } else {
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
            }
        });
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
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("imageUri", imageUri.toString());
        editor.putString("phonenumber", phonenum);
        editor.putString("attendeeId", attendeeId);
        editor.putBoolean("geoLocChecked", geoLocChecked);
        editor.putBoolean("notifSwitchChecked", notifSwitchChecked);

        editor.apply();
        Toast.makeText(context, "Profile created and saved successfully", Toast.LENGTH_SHORT).show();

        switchActivities();
    }
    class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
            Toast.makeText(getApplicationContext(), "Please wait, it may take a few seconds...", Toast.LENGTH_SHORT).show();
        }
        protected Bitmap doInBackground(String... urls) {
            String imageURL=urls[0];
            Bitmap bimage=null;
            try {
                InputStream in=new java.net.URL(imageURL).openStream();
                bimage= BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    private void switchActivities(){
        finish();
    }
}
