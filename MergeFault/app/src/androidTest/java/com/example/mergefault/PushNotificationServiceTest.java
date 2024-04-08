package com.example.mergefault;

import static org.junit.Assert.assertEquals;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PushNotificationServiceTest {
    private PushNotificationService pushNotificationService;

    @Before
    public void setup() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference notificationsRef = firestore.collection("notifications");
        pushNotificationService = new PushNotificationService();
    }

    @Test
    public void testAddNotificationUpdate_documentExists() {
        // Arrange
        String token = "testToken";
        String title = "Test Title";
        String message = "Test Message";

        // Add a document to simulate existing data
        Map<String, Object> existingData = new HashMap<>();
        existingData.put("title", "Existing Title");
        existingData.put("message", "Existing Message");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference notificationsRef = firestore.collection("notifications");
        notificationsRef.document(token).set(existingData);

        pushNotificationService.addNotificationUpdate(token, title, message);

        // Assert
        notificationsRef.document(token).collection("allnotifications").document().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assertEquals(title, document.get("title"));
                        assertEquals(message, document.get("message"));
                    }
                });
    }

    @Test
    public void testAddNotificationUpdate_documentNotExists() {
        // Arrange
        String token = "testToken";
        String title = "Test Title";
        String message = "Test Message";

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference notificationsRef = firestore.collection("notifications");

        pushNotificationService.addNotificationUpdate(token, title, message);

        // Assert
        notificationsRef.document(token).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assertEquals(title, document.get("title"));
                        assertEquals(message, document.get("message"));
                    }
                });
    }

    @After
    public void cleanup() {
        // Clean up any resources or data created during the tests
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference notificationsRef = firestore.collection("notifications");

        // Delete the document created during the test
        String token = "testToken";
        notificationsRef.document(token).delete()
                .addOnSuccessListener(aVoid -> System.out.println("DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> System.out.println("Error deleting document"));
    }






}
