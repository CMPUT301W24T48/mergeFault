package com.example.mergefault;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

@RunWith(AndroidJUnit4.class)
public class NotificationListTest{

    private String token;
    private FirebaseFirestore db;
    private CountDownLatch latch = new CountDownLatch(1);

    @Rule
    public ActivityScenarioRule<AttendeeNotifications> activityScenarioRule =
            new ActivityScenarioRule<>(AttendeeNotifications.class);

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            token = task.getResult();
                            Log.d("TOKEN", token);
                            latch.countDown(); // Signal that token is fetched
                        } else {
                            Log.e("TOKEN", "Error getting token", task.getException());
                        }
                    }
                });


        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (token != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            CollectionReference notificationsRef = firestore.collection("notifications").document(token).collection("allnotifications");

            // Add fake notifications
            Map<String, Object> notification1 = new HashMap<>();
            notification1.put("title", "Notification 1");
            notification1.put("message", "This is the first notification.");
            notificationsRef.document("notification1").set(notification1);

            Map<String, Object> notification2 = new HashMap<>();
            notification2.put("title", "Notification 2");
            notification2.put("message", "This is the second notification.");
            notificationsRef.document("notification2").set(notification2);
        } else {
            Log.e("TOKEN", "Token is null, unable to populate Firestore");
        }
    }

    @After
    public void cleanup() {

        if (token != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            CollectionReference notificationsRef = firestore.collection("notifications").document(token).collection("allnotifications");
            notificationsRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        document.getReference().delete();
                    }
                }
            });
        }
    }

    @Test
    public void testNotificationsDisplayed() {
        // Verify that notifications are displayed in the list
        onView(withId(R.id.myEventListView))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        try {
            Thread.sleep(2000); // Sleep for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText("Notification 1: This is the first notification.")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withText("Notification 2: This is the second notification.")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
