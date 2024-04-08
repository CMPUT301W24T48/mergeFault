package com.example.mergefault;

import androidx.annotation.Nullable;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AttendeeTest {
    FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String attendeeID;

    @Rule
    public ActivityScenarioRule<AttendeeHomeActivity> activityScenarioRule = new ActivityScenarioRule<>(AttendeeHomeActivity.class);

    /*
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
    }

    @After
    public void tearDown() {
        // Clean up shared preferences after each test
        sharedPreferences.edit().clear().apply();
    }

     */
    @Test
    public void testProfileImageClick() {
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withId(R.id.editProfilePictureButton)).check(matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void ProfileFunctionalityTest() {
        String name = "User";
        String email = "user@example.com";
        String imageUri = "https://api.dicebear.com/5.x/pixel-art/png?seed=User";
        String phoneNumber = "1234567890";

        onView(withId(R.id.profileImageView)).perform(click());

        // Perform UI actions to fill profile data
        onView(withId(R.id.editEmailText)).perform(click()).perform(ViewActions.typeText(email));
        closeSoftKeyboard();
        onView(withId(R.id.editAttendeeName)).perform(click()).perform(ViewActions.typeText(name));
        closeSoftKeyboard();
        onView(withId(R.id.editPhoneNumber)).perform(click()).perform(ViewActions.typeText(phoneNumber));
        closeSoftKeyboard();
        onView(withId(R.id.cancelButton)).perform(click());

        db = FirebaseFirestore.getInstance();
        CollectionReference attendeesRef = db.collection("attendees");
        attendeesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc: value) {
                    if(doc.getString("AttendeeName").equals(name)){
                        assertEquals(name, doc.getString("AttendeeName"));
                        assertEquals(email, doc.getString("AttendeeEmail"));
                        assertEquals(imageUri, doc.getString("AttendeeProfile"));
                        assertEquals(phoneNumber, doc.getString("AttendeePhoneNumber"));
                        attendeeID = doc.getId();
                    }
                }
            }
        });
        //myEventsTest
        onView(withId(R.id.viewMyEventsButton)).perform(click());
        onView(withId(R.id.myEventListView)).perform(click());
        onView(withId(R.id.myEventListView)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.imageView)).perform(click());
        //browseEventsTest
        onView(withId(R.id.browseEventsButton)).perform(click());
        onView(withId(R.id.myEventListView)).perform(click());
        onView(withId(R.id.myEventListView)).check(matches(ViewMatchers.isDisplayed()));
        //clear Firebase
        attendeesRef.document(attendeeID).delete();
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