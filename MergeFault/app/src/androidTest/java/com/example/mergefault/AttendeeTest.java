package com.example.mergefault;

import androidx.annotation.NonNull;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AttendeeTest {

    FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<AttendeeHomeActivity> activityScenarioRule = new ActivityScenarioRule<>(AttendeeHomeActivity.class);

    @Test
    public void testProfileImageClick() {
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withId(R.id.editEventPosterText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testMyEventsClick() {
        onView(withId(R.id.viewMyEventsButton)).perform(click());
        onView(withId(R.id.myEventListView)).perform(click());
    }


    @Test
    public void testBrowsePostedEventsClick() {
        onView(withId(R.id.browseEventsButton)).perform(click());
        onView(withId(R.id.myEventListView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void profileActivitySwitchCheck() {
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withId(R.id.cancelButton)).perform(click());
        onView(withId(R.id.viewMyEventsButton)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }

    @Test
    public void addTestUser(){
        db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("AttendeeName", "Test Name");
        user.put("AttendeeEmail", "test@ualberta.ca");
        user.put("AttendeePhoneNumber", "1234567890");
        user.put("AttendeeID", "1234567890");
        user.put("AttendeeProfile", "http://image.com/image");
        db.collection("events").document("testdoc").collection("attendees").document("1234567890").set(user).addOnSuccessListener(documentReference -> {
            Log.d("Success", "Test User Added");

        }).addOnFailureListener(e -> {
            Log.d("Failure", "Test User not Added");
            Log.d("Error", e.toString());
        });
    }

    @Test
    public void deleteTestUser(){
        db = FirebaseFirestore.getInstance();
        db.collection("events").document("testdoc").collection("attendees").document("1234567890").delete().addOnSuccessListener(unused -> {
            Log.d("Success", "Test User Deleted");

        }).addOnFailureListener(e -> {
            Log.d("Failure", "Test User not Deleted");
            Log.d("Error", e.toString());
        });
    }

}
