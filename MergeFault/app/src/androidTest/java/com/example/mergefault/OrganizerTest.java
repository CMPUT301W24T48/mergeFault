package com.example.mergefault;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class OrganizerTest {
    FirebaseFirestore db;
    CollectionReference snapshot;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);
    public IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void testCreateEventActivity(){
        db = FirebaseFirestore.getInstance();
        // Clicks "ORGANIZER" button
        onView(withId(R.id.organizerButton)).perform(click());
        onView(withId(R.id.organizerHome)).check(matches(isDisplayed()));

        // Clicks "Create new event" button
        onView(withId(R.id.createNewEventButton)).perform(click());
        onView(withId(R.id.organizerAddEventDetails)).check(matches(isDisplayed()));

        Resources resources = InstrumentationRegistry.getTargetContext().getResources();
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(R.mipmap.ic_launcher) + '/' +
                resources.getResourceTypeName(R.mipmap.ic_launcher) + '/' +
                resources.getResourceEntryName(R.mipmap.ic_launcher));

        Intent galleryResultData = new Intent();
        galleryResultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(
                Activity.RESULT_OK, galleryResultData);

        Matcher<Intent> expectedIntent = hasAction(MediaStore.ACTION_PICK_IMAGES);
        Intents.init();
        intending(expectedIntent).respondWith(result);

        onView(withId(R.id.eventPosterImageView)).perform(click());
        intended(expectedIntent);
        Intents.release();

        onView(withId(R.id.locationSetButton)).perform(click());
        onView(withHint("Search a place")).perform(typeText("CCIS"));
        onView(withText("CCIS")).perform(click());
        //onView(withText("CCIS University of, Edmonton, AB, Canada")).perform(click());

        onView(withId(R.id.datSetButton)).perform(click());
        //onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2000, 6, 0));
        // onView(withId(R.id.timeSetButton)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.attendeeLimitSetButton)).perform(click());
        onView(withId(R.id.editNumberText)).perform(typeText("20"));
        onView(withText("Add")).perform(click());
        onView(withId(R.id.descriptionSetButton)).perform(click());
        onView(withId(R.id.editTextBox)).perform(typeText("This is the event description"));
        onView(withText("Add")).perform(click());
        onView(withId(R.id.switch1)).perform(click());
        onView(withId(R.id.eventNameEditText)).perform(typeText("TestEvent"), ViewActions.closeSoftKeyboard());
    }


    @Test
    public void testViewEventsButton(){
        onView(withId(R.id.organizerButton)).perform(click());
        onView(withId(R.id.viewMyEventsButton)).perform(click());
        //onView(withId(R.id.myEventListView)).perform(click());
    }

    /*
    @Test
    public void generateNewQR(){
        onView(withId(R.id.generateNewButton)).perform(click());
    }

     */
    @Test
    public void addTestEvent(){
        db = FirebaseFirestore.getInstance();
        java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf("2007-09-23 10:10:10.0");
        Map<String, Object> event = new HashMap<>();
        event.put("EventName", "Test Name");
        event.put("DateTime", timestamp);
        event.put("Description", "Test Description");
        event.put("EventID", "1234567890");
        event.put("EventPoster", "http://image.com/image");
        event.put("GeoLocOn", true);
        event.put("Location", "Test Location");
        event.put("OrganizerID", "ID");
        event.put("AttendeeLimit", 20);
        db.collection("events").document("1234567890").set(event).addOnSuccessListener(documentReference -> {
            Log.d("Success", "Test Event Added");
        }).addOnFailureListener(e -> {
            Log.d("Failure", "Test Event not Added");
            Log.d("Error", e.toString());
        });
    }

    @Test
    public void deleteTestEvent(){
        db = FirebaseFirestore.getInstance();
        db.collection("events").document("1234567890").delete().addOnSuccessListener(unused -> {
            Log.d("Success", "Test Event Deleted");

        }).addOnFailureListener(e -> {
            Log.d("Failure", "Test Event not Deleted");
            Log.d("Error", e.toString());
        });
    }

}
