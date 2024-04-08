package com.example.mergefault;

import androidx.test.espresso.assertion.ViewAssertions;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains Espresso tests for the AttendeeHomeActivity.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AttendeeTest {
    /**
     * Firebase Firestore instance for database operations.
     */
    FirebaseFirestore db;
    /**
     * SharedPreferences for storing user profile data.
     */
    private SharedPreferences sharedPreferences;

    /**
     * Rule to launch AttendeeHomeActivity for each test case.
     */
    @Rule
    public ActivityScenarioRule<AttendeeHomeActivity> activityScenarioRule = new ActivityScenarioRule<>(AttendeeHomeActivity.class);

    /**
     * Setup method to initialize necessary resources before each test case.
     */
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
    }

    /**
     * Teardown method to clean up after each test case.
     */
    @After
    public void tearDown() {
        // Clean up shared preferences after each test
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Test method to verify the behavior of clicking on the profile image.
     */
    @Test
    public void testProfileImageClick() {
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withId(R.id.editProfilePictureButton)).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test method to verify the behavior of clicking on "My Events" button.
     */
    @Test
    public void testMyEventsClick() {
        onView(withId(R.id.viewMyEventsButton)).perform(click());
        onView(withId(R.id.myEventListView)).perform(click());
        onView(withId(R.id.myEventListView)).check(matches(ViewMatchers.isDisplayed()));
    }


    /**
     * Test method to verify the behavior of clicking on "Browse Posted Events" button.
     */
    @Test
    public void testBrowsePostedEventsClick() {
        onView(withId(R.id.browseEventsButton)).perform(click());
        onView(withId(R.id.myEventListView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.myEventListView)).check(matches(ViewMatchers.isDisplayed()));

    }

    /**
     * Test method to verify the behavior of switching activities from profile to events view.
     */
    @Test
    public void profileActivitySwitchCheck() {
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withId(R.id.cancelButton)).perform(click());
        onView(withId(R.id.viewMyEventsButton)).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test method to add a test user to Firestore.
     */
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

    /**
     * Test method to delete a test user from Firestore.
     */
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

    /**
     * Test method to verify saving of profile data in SharedPreferences.
     */
    public void testSaveProfileData() {
        String name = "User";
        String email = "user@example.com";
        String imageUri = "https://api.dicebear.com/5.x/pixel-art/png?seed=User";
        String phoneNumber = "1234567890";

        onView(withId(R.id.profileImageView)).perform(click());

        // Perform UI actions to fill profile data
        onView(withId(R.id.editEmailText)).perform(click()).perform(ViewActions.typeText(email));
        onView(withId(R.id.editAttendeeName)).perform(click()).perform(ViewActions.typeText(name));
        onView(withId(R.id.editPhoneNumber)).perform(click()).perform(ViewActions.typeText(phoneNumber));
        closeSoftKeyboard();
        onView(withId(R.id.cancelButton)).perform(click());

        // Validate that profile data is saved correctly in SharedPreferences
        assertEquals(name, sharedPreferences.getString("name", null));
        assertEquals(email, sharedPreferences.getString("email", null));
        assertEquals(imageUri, sharedPreferences.getString("imageUri", null));
        assertEquals(phoneNumber, sharedPreferences.getString("phonenumber", null));
    }
}
