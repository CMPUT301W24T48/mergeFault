package com.example.mergefault;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class responsible for handling push notifications received from Firebase Cloud Messaging (FCM).
 * This class extends FirebaseMessagingService to handle incoming messages and process them accordingly.
 */
public class PushNotificationService extends FirebaseMessagingService {
    /**
     * Called when a message is received from Firebase Cloud Messaging (FCM).
     * Processes the received message and displays a notification to the user.
     *
     * @param message The message received from FCM.
     */
    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = message.getNotification().getTitle();
        String text = message.getNotification().getBody();
        String CHANNEL_ID = "MESSAGE";
        CharSequence name;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Message Notification",
                NotificationManager.IMPORTANCE_HIGH);

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("Permission", "no permission");
            return;
        }
        NotificationManagerCompat.from(this).notify(1, notification.build());
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            Log.d("TOKEN", token);

                            // Now you can use the token here
                            addNotificationUpdate(token, title, text);
                        } else {
                            // Handle the error
                            Log.e("TOKEN", "Error getting token", task.getException());
                        }
                    }
                });



    }
    /**
     * Adds a notification update to Firestore database.
     *
     * @param token  The FCM token associated with the device.
     * @param title  The title of the notification.
     * @param message The body text of the notification.
     */
    public void addNotificationUpdate(String token, String title, String message) {
        FirebaseFirestore db;
        CollectionReference notificationsRef;
        db = FirebaseFirestore.getInstance();
        notificationsRef = db.collection("notifications");


        notificationsRef.document(token).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("doc", "Document exists: " + document.getData());
                                Map<String, Object> notificationData = new HashMap<>();
                                notificationData.put("title", title);
                                notificationData.put("message", message);
                                notificationsRef.document(token).collection("allnotifications").add(notificationData);

                            } else {
                                Log.d("doc", "Document does not exist");
                                Map<String, Object> data = new HashMap<>();
                                data.put("devicetoken", token);
                                notificationsRef.add(data);
                                Map<String, Object> notificationData = new HashMap<>();
                                notificationData.put("title", title);
                                notificationData.put("message", message);
                                notificationsRef.document(token).collection("allnotifications").add(notificationData);

                            }
                        } else {
                            // Error getting document
                            Log.d("doc","Error getting document", task.getException());
                        }
                    }
                });

    }




}

