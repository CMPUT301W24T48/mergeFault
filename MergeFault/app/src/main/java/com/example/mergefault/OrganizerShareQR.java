package com.example.mergefault;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;

public class OrganizerShareQR extends AppCompatActivity {

    private ImageView checkInQRImageView, promoteQRImageView;
    private Button cancelButton, shareCheckInButton, sharePromoteButton, shareBothButton;

    private String eventId;

    private String PromotionalActivityRedirect = "www.lotuseventspromotions.com?eventId=";

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

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

        // Set click listener for cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity when cancel button is clicked
            }
        });

        // Set click listener for share check-in button
        shareCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = combineBitmaps(checkInQRImageView);
                if (bitmap != null) {
                    checkPermissionAndShare(bitmap);
                } else {
                    Toast.makeText(OrganizerShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for share promotion button
        sharePromoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = combineBitmaps(promoteQRImageView);
                if (bitmap != null) {
                    checkPermissionAndShare(bitmap);
                } else {
                    Toast.makeText(OrganizerShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for share both button
        shareBothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = combineBitmaps(checkInQRImageView, promoteQRImageView);
                if (bitmap != null) {
                    checkPermissionAndShare(bitmap);
                } else {
                    Toast.makeText(OrganizerShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent intent = getIntent();
        eventId = intent.getStringExtra("EventId");

        // Generate QR codes for event check-in and promotion
        generateQRCode(eventId, checkInQRImageView);
        generateQRCode("myapp://" + PromotionalActivityRedirect + eventId, promoteQRImageView);
    }

    private void checkPermissionAndShare(Bitmap bitmap) {
        // Check if we have permission to write to external storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            // Permission is granted, proceed with sharing
            shareQRCode(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with sharing
                Bitmap bitmap = combineBitmaps(checkInQRImageView, promoteQRImageView);
                if (bitmap != null) {
                    shareQRCode(bitmap);
                } else {
                    Toast.makeText(OrganizerShareQR.this, "Failed to generate QR code image", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateQRCode(String content, ImageView imageView) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            String contentString = String.valueOf(content);
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400);
            Bitmap bitmap = toBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private Bitmap combineBitmaps(ImageView... imageViews) {
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

        return combinedBitmap;
    }

    private void shareQRCode(Bitmap bitmap) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QR Code", null);
            Uri imageUri = Uri.parse(path);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
        } catch (Exception e) {
            Toast.makeText(OrganizerShareQR.this, "Error sharing QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
