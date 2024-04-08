package com.example.mergefault;

import android.provider.CalendarContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.anything;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AdminTest {
    FirebaseFirestore db;
    @Rule
    public ActivityScenarioRule<AdminHomeActivity> activityScenarioRule = new ActivityScenarioRule<>(AdminHomeActivity.class);
    @Test
    public void testDeleteEvents(){
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
        db.collection("events").document("111111111").set(event).addOnSuccessListener(documentReference -> {
            Log.d("Success", "Test Event Added");
        }).addOnFailureListener(e -> {
            Log.d("Failure", "Test Event not Added");
            Log.d("Error", e.toString());
            throw new RuntimeException("failed to add event");
        });
        onView(withId(R.id.manageEventsButton)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.myEventListView)).atPosition(0).perform(click());
        DocumentReference docRef = db.collection("events").document("111111111");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        Log.d("failed", "failed to delete event");
                        throw new RuntimeException("failed to delete event");
                    } else {
                        Log.d("passed", "deleted event successfully");
                    }
                }
            }
        });
    }

    @Test
    public void testDeleteProfile(){
        db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("AttendeeName", "Test Name");
        user.put("AttendeeEmail", "test@ualberta.ca");
        user.put("AttendeePhoneNumber", "1234567890");
        user.put("AttendeeID", "1234567890");
        user.put("AttendeeProfile", "http://image.com/image");
        db.collection("attendees").document("111111111").set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("success", "test profile added");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("failed", "failed to add test profile");
                throw new RuntimeException("failed to add profile");
            }
        });
        onView(withId(R.id.manageProfilesButton)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.profileLinearLayout)).atPosition(0).perform(click());
        DocumentReference docRef = db.collection("attendees").document("111111111");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        Log.d("failed", "failed to delete profile");
                        throw new RuntimeException("failed to delete profile");
                    } else {
                        Log.d("passed", "deleted profile successfully");
                    }
                }
            }
        });
    }

    @Test
    public  void testDeleteImage(){
        db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("AttendeeName", "Test Name");
        user.put("AttendeeEmail", "test@ualberta.ca");
        user.put("AttendeePhoneNumber", "1234567890");
        user.put("AttendeeID", "1234567890");
        user.put("AttendeeProfile", "http://image.com/image");
        db.collection("attendees").document("111111111").set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("", "Error writing document", e);
            }
        });
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
        db.collection("events").document("111111111").set(event).addOnSuccessListener(documentReference -> {
            Log.d("Success", "Test Event Added");
        }).addOnFailureListener(e -> {
            Log.d("Failure", "Test Event not Added");
            Log.d("Error", e.toString());
        });
        onView(withId(R.id.manageImages)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.imageListView)).atPosition(0).perform(click());
        DocumentReference attendeeDocRef = db.collection("attendees").document("111111111");

    }
}
