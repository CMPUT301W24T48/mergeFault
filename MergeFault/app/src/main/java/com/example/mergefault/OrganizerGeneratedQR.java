package com.example.mergefault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

/**
 * This activity displays a generated QR code for a specific event.
 * It allows organizers to continue to the event creation process.
 */
public class OrganizerGeneratedQR extends AppCompatActivity {

    private ImageView QR;
    private Integer EventId;
    private Button continueToCreation;
    private String eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_generated_qr);

        // Retrieve the eventId from the intent
        Intent receiverIntent = getIntent();
        eventId = receiverIntent.getStringExtra("EventId");
        Log.d("eventIdAfter2", "eventid:" + eventId);

        QR = findViewById(R.id.qrImageView);
        continueToCreation = findViewById(R.id.continueButton);

        // Set click listener for continue button
        continueToCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start OrganizerAddEventActivity to continue to the event creation process
                Intent intent = new Intent(OrganizerGeneratedQR.this, OrganizerAddEventActivity.class);
                startActivity(intent);
            }
        });

        EventId = 123; // Temporary value for demonstration purposes

        // Generate QR code for the event ID
        generateQRCode(EventId);
    }

    /**
     * Generates a QR code containing the specified event ID and sets it to the ImageView.
     *
     * @param embedData The event ID to embed in the QR code.
     */
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

    /**
     * Converts a BitMatrix to a Bitmap.
     *
     * @param matrix The BitMatrix to convert.
     * @return The converted Bitmap.
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
}
