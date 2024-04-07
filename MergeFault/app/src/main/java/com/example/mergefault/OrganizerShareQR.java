package com.example.mergefault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Activity for sharing QR codes with organizers.
 */
public class OrganizerShareQR extends AppCompatActivity {

    private ImageView checkInQRImageView, promoteQRImageView;
    private Button cancelButton, shareCheckInButton, sharePromoteButton, shareBothButton;
    private ImageView homeButton;

    private String eventId;

    private String parentActivity;

    private String organizerId;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private StorageReference eventCheckInQRRef;
    private StorageReference eventPromoteQRRef;
    private Bitmap checkInBitmap;
    private Bitmap promoteBitmap;
    private Uri checkInUri;
    private Uri promoteUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_share_qr);

        checkInQRImageView = findViewById(R.id.checkInQRImageView);
        promoteQRImageView = findViewById(R.id.promoteQRImageView);
        shareCheckInButton = findViewById(R.id.shareCheckInButton);
        sharePromoteButton = findViewById(R.id.sharePromoteButton);
        shareBothButton = findViewById(R.id.shareBoth);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.logoImgView);


        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();

        // Set click listener for cancel button
        Intent intent = getIntent();
        eventId = intent.getStringExtra("EventId");
        organizerId = intent.getStringExtra("OrganizerID");
        parentActivity = intent.getStringExtra("ParentActivity");

        // Set click listener for share check-in button
        shareCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInUri != null) {
                    shareQRCode(checkInUri);
                } else {
                    Toast.makeText(OrganizerShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for share promotion button
        sharePromoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (promoteUri != null) {
                    shareQRCode(promoteUri);
                } else {
                    Toast.makeText(OrganizerShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for share both button
        shareBothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri combinedUri = combineBitmaps(checkInQRImageView, promoteQRImageView);
                if (combinedUri != null) {
                    shareQRCode(combinedUri);
                } else {
                    Toast.makeText(OrganizerShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(parentActivity, "OrganizerEventOptions")) {
                    Intent intent = new Intent(OrganizerShareQR.this, OrganizerEventOptions.class);
                    intent.putExtra("EventId", eventId);
                    intent.putExtra("OrganizerID", organizerId);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(OrganizerShareQR.this, OrganizerHomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Objects.equals(parentActivity, "OrganizerEventOptions")) {
                    Intent intent = new Intent(OrganizerShareQR.this, OrganizerEventOptions.class);
                    intent.putExtra("EventId", eventId);
                    intent.putExtra("OrganizerID", organizerId);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(OrganizerShareQR.this, OrganizerHomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        OrganizerShareQR.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerShareQR.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Generate QR codes for event check-in and promotion
        eventCheckInQRRef = storageRef.child("QRCodes").child("CheckIn/" + eventId + ".png");
        eventCheckInQRRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    new OrganizerShareQR.DownloadImageFromInternet((ImageView) findViewById(R.id.checkInQRImageView)).execute(uri.toString());
                }
            }
        });
        eventPromoteQRRef = storageRef.child("QRCodes").child("Promotion/" + eventId + ".png");
        eventPromoteQRRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    new OrganizerShareQR.DownloadImageFromInternet((ImageView) findViewById(R.id.promoteQRImageView)).execute(uri.toString());
                }
            }
        });
    }

    /**
     * Combine multiple bitmaps into a single bitmap.
     *
     * @param imageViews ImageViews containing bitmaps to be combined.
     * @return The combined bitmap.
     */
    private Uri combineBitmaps(ImageView... imageViews) {
        Bitmap combinedBitmap = null;
        int totalWidth = 0;
        int maxHeight = 0;

        for (ImageView imageView : imageViews) {
            totalWidth += imageView.getWidth();
            maxHeight = Math.max(maxHeight, imageView.getHeight());
        }

        combinedBitmap = Bitmap.createBitmap(totalWidth, maxHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combinedBitmap);
        int currentX = 0;

        for (ImageView imageView : imageViews) {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            canvas.drawBitmap(bitmap, currentX, 0, null);
            currentX += imageView.getWidth();
        }

        return bitmapToUri(combinedBitmap);
    }

    private Uri bitmapToUri(Bitmap bitmap) {
        Uri contentUri = null;
        try {
            // Create a temporary file to hold the bitmap
            File tempFile = File.createTempFile("qr_code", ".png", getCacheDir());
            tempFile.deleteOnExit(); // Delete the file when the VM exits

            // Write the bitmap to the temporary file
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

            // Generate a content URI for the temporary file
            contentUri = FileProvider.getUriForFile(this, "com.example.mergefault.fileprovider", tempFile);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(OrganizerShareQR.this, "Error making QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return contentUri;
    }

    /**
     * Share the QR code bitmap via an intent.
     *
     * @param bitmap The bitmap image of the QR code to share.
     */
    private void shareQRCode(Uri contentUri) {
        // Create a sharing intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Start the chooser for sharing
        startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
    }
    class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
            if(imageView.equals(checkInQRImageView)) {
                checkInBitmap = result;
                checkInUri = bitmapToUri(checkInBitmap);
            } else if (imageView.equals(promoteQRImageView)) {
                promoteBitmap = result;
                promoteUri = bitmapToUri(promoteBitmap);
            }
        }
    }


}
