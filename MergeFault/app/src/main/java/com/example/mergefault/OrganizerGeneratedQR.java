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

public class OrganizerGeneratedQR extends AppCompatActivity {

    private ImageView QR;
    private Integer EventId;
    private Button continueToCreation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_generated_qr);

        QR = findViewById(R.id.qrImageView);
        continueToCreation = findViewById(R.id.continueButton);

        continueToCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TEMPORARY REMOVAL OF THE SWITCH IN ACTIVITY DUE TO THE BUG

               // Intent intent = new Intent(OrganizerGeneratedQR.this, OrganizerAddEventActivity.class);
               // startActivity(intent);

                Intent intent = new Intent(OrganizerGeneratedQR.this, OrganizerShareQR.class);
                startActivity(intent);
            }
        });

        EventId = 123;

        generateQRCode(EventId);
    }

    private void generateQRCode(Integer embedData) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        String qrdata = String.valueOf(embedData);
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qrdata, BarcodeFormat.QR_CODE, 400, 400);
            Bitmap bitmap = toBitmap(bitMatrix);
            QR.setImageBitmap(bitmap);
            QR.setVisibility(View.VISIBLE);
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
