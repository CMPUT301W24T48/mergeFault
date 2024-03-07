package com.example.mergefault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class OrganizerShareQR extends AppCompatActivity {

    private ImageView checkInQRImageView, promoteQRImageView;
    private Button cancelButton;
    private Button shareBothButton;

    private Integer eventId;

    private String PromotionalActivityRedirect= "www.lotuseventspromotions.com";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_share_qr);

        checkInQRImageView = findViewById(R.id.checkInQRImageView);
        promoteQRImageView = findViewById(R.id.promoteQRImageView);
        shareBothButton = findViewById(R.id.shareBoth);
        cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity when cancel button is clicked
            }
        });

        //temporary way to go to add events screen
        shareBothButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerShareQR.this, OrganizerAddEventActivity.class);
                startActivity(intent);
            }
        }));

        eventId = 123;
        String myEventID = String.valueOf(eventId);

        generateQRCode(myEventID, checkInQRImageView);
        generateQRCode("myapp://" + PromotionalActivityRedirect, promoteQRImageView);
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
}
