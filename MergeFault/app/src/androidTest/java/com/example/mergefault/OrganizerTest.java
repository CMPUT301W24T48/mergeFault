package com.example.mergefault;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
    /*
    @Rule
    public ActivityScenarioRule<OrganizerHomeActivity> activityScenarioRule = new ActivityScenarioRule<>(OrganizerHomeActivity.class);
    */
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);
    @Test
    public void testCreateEventActivity(){
        db = FirebaseFirestore.getInstance();
        onView(withId(R.id.organizerButton)).perform(click());
        onView(withId(R.id.createNewEventButton)).perform(click());
        onView(withId(R.id.locationSetButton)).perform(click());
        onView(withId(R.id.editTextBox)).perform(typeText("some Location"));
        onView(withText("Add")).perform(click());
        onView(withId(R.id.datSetButton)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.timeSetButton)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.attendeeLimitSetButton)).perform(click());
        onView(withId(R.id.editNumberText)).perform(typeText("20"));
        onView(withText("Add")).perform(click());
        onView(withId(R.id.descriptionSetButton)).perform(click());
        onView(withId(R.id.editTextBox)).perform(typeText("This is the event description"));
        onView(withText("Add")).perform(click());
        onView(withId(R.id.switch1)).perform(click());
        onView(withId(R.id.eventNameEditText)).perform(typeText("TestEvent"), ViewActions.closeSoftKeyboard());
        //onView(withId(R.id.createEventButton)).perform(click());
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
