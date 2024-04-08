package com.example.mergefault;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerGenerateAndShareQRTest {

    @Rule
    public IntentsTestRule<OrganizerGenerateAndShareQR> activityRule = new IntentsTestRule<>(OrganizerGenerateAndShareQR.class, true, false);

    @Test
    public void testCheckInQRWithEventId() {
        // Simulate data from the previous activity
        String eventId = "exampleEventId";
        Intent intent = new Intent();
        intent.putExtra("EventId", eventId);


        // Launch the activity with the specified intent
        activityRule.launchActivity(intent);


        try {
            Thread.sleep(3000); // Wait for 3 seconds to generate the QR code
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        ImageView checkInQRImageView = activityRule.getActivity().findViewById(R.id.checkInQRImageView);
        Bitmap qrBitmap = ((BitmapDrawable) checkInQRImageView.getDrawable()).getBitmap();


        String decodedContents = decodeQRCode(qrBitmap);


        assertNotNull(decodedContents);
        String decodedAction = decodedContents.substring(0,decodedContents.indexOf("."));
        String decodedEventId = decodedContents.substring(decodedContents.indexOf(".") + 1);
        assertEquals("Expected action", "CheckIn", decodedAction);
        assertEquals("Expected event ID", "exampleEventId", decodedEventId);
    }

    @Test
    public void testSignUpQRWithEventId() {
        // Simulate data from the previous activity
        String eventId = "exampleEventId";
        Intent intent = new Intent();
        intent.putExtra("EventId", eventId);


        // Launch the activity with the specified intent
        activityRule.launchActivity(intent);


        try {
            Thread.sleep(3000); // Wait for 3 seconds to generate the QR code
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        ImageView signUpQRImageView = activityRule.getActivity().findViewById(R.id.promoteQRImageView);
        Bitmap qrBitmap = ((BitmapDrawable) signUpQRImageView.getDrawable()).getBitmap();


        String decodedContents = decodeQRCode(qrBitmap);

        Log.d("DECODED:", decodedContents);


        assertNotNull(decodedContents);
        String decodedAction = decodedContents.substring(0,decodedContents.indexOf("."));
        String decodedEventId = decodedContents.substring(decodedContents.indexOf(".") + 1);
        assertEquals("Expected action", "Promotion", decodedAction);
        assertEquals("Expected event ID", "exampleEventId", decodedEventId);
    }

    private String decodeQRCode(Bitmap qrBitmap) {
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        try {
            int[] intArray = new int[qrBitmap.getWidth() * qrBitmap.getHeight()];
            qrBitmap.getPixels(intArray, 0, qrBitmap.getWidth(), 0, 0, qrBitmap.getWidth(), qrBitmap.getHeight());
            RGBLuminanceSource source = new RGBLuminanceSource(qrBitmap.getWidth(), qrBitmap.getHeight(), intArray);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = multiFormatReader.decode(binaryBitmap);
            return result.getText();
        } catch (ReaderException e) {
            e.printStackTrace();
            return null;
        }
    }
}
