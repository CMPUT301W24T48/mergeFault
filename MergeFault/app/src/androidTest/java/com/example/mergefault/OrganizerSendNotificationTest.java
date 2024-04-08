package com.example.mergefault;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.Matchers.not;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;




public class OrganizerSendNotificationTest {

    @Rule
    public IntentsTestRule<OrganizerEventNotification> activityRule =
            new IntentsTestRule<>(OrganizerEventNotification.class, true, false);


    private FirebaseFirestore db;

    @Before
    public void setUp() {
        addTestEvent();
    }

    @After
    public void tearDown() {
        deleteTestEvent();
    }

    @Test
    public void testAddMessage() {
        // Simulate data from the previous activity
        String eventId = "1234567890";
        Intent intent = new Intent();
        intent.putExtra("EventId", eventId);
        activityRule.launchActivity(intent);

        onView(withId(R.id.eventEditText)).perform(ViewActions.click());
        onView(withId(R.id.eventEditText)).perform(ViewActions.typeText("Test message"));
    }

    @Test
    public void testSendButton() {
        String eventId = "1234567890";
        Intent intent = new Intent();
        intent.putExtra("EventId", eventId);
        activityRule.launchActivity(intent);

        onView(withId(R.id.eventEditText)).perform(ViewActions.click());
        onView(withId(R.id.eventEditText)).perform(ViewActions.typeText("Test message"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.sendButton)).perform(ViewActions.click());
        try {
            Thread.sleep(2000); // 2 seconds delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.setText))
                .check(matches(withText("Notification sent")));

    }

    @Test
    public void testSendEmpty() {
        String eventId = "1234567890";
        Intent intent = new Intent();
        intent.putExtra("EventId", eventId);
        activityRule.launchActivity(intent);
        onView(withId(R.id.sendButton)).perform(ViewActions.click());
        try {
            Thread.sleep(2000); // 2 seconds delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.setText))
                .check(matches(withText("Please enter all required fields.")));
    }

    // Method to add a test event to Firestore
    private void addTestEvent() {
        db = FirebaseFirestore.getInstance();
        Map<String, Object> event = new HashMap<>();
        event.put("EventName", "Test Name");
        event.put("DateTime", "Some Time");
        event.put("Description", "Test Description");
        event.put("EventID", "1234567890");
        event.put("EventPoster", "http://image.com/image");
        event.put("GeoLocOn", true);
        event.put("Location", "Test Location");
        event.put("OrganizerID", "ID");
        event.put("AttendeeLimit", 20);
        db.collection("events").document("1234567890").set(event)
                .addOnSuccessListener(documentReference -> Log.d("Success", "Test Event Added"))
                .addOnFailureListener(e -> {
                    Log.d("Failure", "Test Event not Added");
                    Log.d("Error", e.toString());
                });
    }

    // Method to delete the test event from Firestore
    private void deleteTestEvent() {
        db.collection("events").document("1234567890").delete()
                .addOnSuccessListener(aVoid -> Log.d("Success", "Test Event Deleted"))
                .addOnFailureListener(e -> Log.d("Failure", "Failed to delete test event: " + e));
    }
}
