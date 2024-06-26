package com.example.mergefault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Activity for generating and sharing QR codes with organizers.
 */
public class OrganizerGenerateAndShareQR extends AppCompatActivity {
    private ImageView checkInQRImageView, promoteQRImageView;
    private Button cancelButton, shareCheckInButton, sharePromoteButton, shareBothButton;
    private ImageView homeButton;
    private String eventId;
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private Bitmap checkInBitmap;
    private Bitmap promoteBitmap;
    private Uri checkInUri;
    private Uri promoteUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_share_qr);

        // Get the necessary objects from the UI
        checkInQRImageView = findViewById(R.id.checkInQRImageView);
        promoteQRImageView = findViewById(R.id.promoteQRImageView);
        shareCheckInButton = findViewById(R.id.shareCheckInButton);
        sharePromoteButton = findViewById(R.id.sharePromoteButton);
        shareBothButton = findViewById(R.id.shareBoth);
        cancelButton = findViewById(R.id.cancelButton);
        homeButton = findViewById(R.id.logoImgView);

        // Receive eventId from the previous activity
        Intent intent = getIntent();
        eventId = intent.getStringExtra("EventId");

        // Get instance and reference to the firebase firestore
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");

        // Get instance and reference to the firebase storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();

        // Generate QR codes for event check-in and promotion
        checkInBitmap = generateQRCode("CheckIn." + eventId, checkInQRImageView, "CheckIn");
        promoteBitmap = generateQRCode("Promotion."  + eventId, promoteQRImageView, "Promotion");
        upload(eventId,checkInUri,"CheckIn");
        upload(eventId,promoteUri,"Promotion");

        // Set click listener for the "Share Check-In" button
        shareCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInUri != null) {
                    shareQRCode(checkInUri);
                } else {
                    Toast.makeText(OrganizerGenerateAndShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for the "Share Promotional QR" button
        sharePromoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (promoteUri != null) {
                    shareQRCode(promoteUri);
                } else {
                    Toast.makeText(OrganizerGenerateAndShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for the "Share Both" button
        shareBothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri combinedUri = combineBitmaps(checkInQRImageView, promoteQRImageView);
                if (combinedUri != null) {
                    shareQRCode(combinedUri);
                } else {
                    Toast.makeText(OrganizerGenerateAndShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for the Logo
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerGenerateAndShareQR.this, OrganizerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Cancel" button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent defaultIntent = new Intent(OrganizerGenerateAndShareQR.this, OrganizerHomeActivity.class);
                startActivity(defaultIntent);
                finish();
            }
        });

        // Set what happens when back button is pressed
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        OrganizerGenerateAndShareQR.this.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    /**
     * Generate a QR code bitmap for the given content and set it to the specified ImageView.
     * @param content    The content to encode into the QR code.
     * @param imageView  The ImageView to display the QR code.
     */
    private Bitmap generateQRCode(String content, ImageView imageView, String type) {
        Bitmap bitmap = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            String contentString = String.valueOf(content);
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400);
            bitmap = toBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
            if (Objects.equals(type, "CheckIn")) {
                checkInUri = bitmapToUri(bitmap);
            } else if (Objects.equals(type, "Promotion")) {
                promoteUri = bitmapToUri(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Convert a BitMatrix to a Bitmap.
     * @param matrix The BitMatrix to convert.
     * @return The resulting Bitmap.
     */
    private Bitmap toBitmap(BitMatrix matrix) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
            }
        }
        return bitmap;
    }

    /**
     * Combine multiple bitmaps into a single bitmap.
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

    /**
     * This method takes a bitmap and compresses it into a png uri
     * @param bitmap This is the bitmap that's going to be turned into a uri
     * @return Returns a uri created by the method
     */
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
            Toast.makeText(OrganizerGenerateAndShareQR.this, "Error making QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return contentUri;
    }

    /**
     * Share the QR code bitmap via an intent
     * @param contentUri This is the uri png that will be shared
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

    /**
     * This method uploads qr uri to the firebase storage
     * @param eventId This is the event id of the created event
     * @param QRUri This is the QR uri that will be uploaded
     * @param type This tells the function if it is a check-in qr or promotional qr
     */
    public void upload(String eventId, Uri QRUri, String type){
        StorageReference QRCode = storageRef.child( "QRCodes").child(type+ "/" + eventId + ".png");
        QRCode.putFile(QRUri);
    }


}
