package com.example.mergefault;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

public class OrganizerAddEventActivityTest {
    @Rule
    public ActivityScenarioRule<OrganizerAddEventActivity> scenario = new ActivityScenarioRule<OrganizerAddEventActivity>(OrganizerAddEventActivity.class);

    @Test
    public void testAddAddress() {
        onView(withId(R.id.locationSetButton)).perform(click());
        onView(withId(R.id.editTextBox)).perform(ViewActions.typeText("Edmonton"));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.locationText)).check(matches(withText("Address: Edmonton")));
    }

    @Test
    public void testAddLimit() {
        onView(withId(R.id.attendeeLimitSetButton)).perform(click());
        onView(withId(R.id.editNumberText)).perform(ViewActions.typeText("123"));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.attendeeLimitText)).check(matches(withText("Limit: 123")));
    }
    @Test
    public void testAddDescription() {
        onView(withId(R.id.descriptionSetButton)).perform(click());
        onView(withId(R.id.editTextBox)).perform(ViewActions.typeText("Test Description"));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.descriptionText)).check(matches(withText("Description: Test Description")));
    }
    @Test
    public void testAddEventName() {
        //onView(withId(R.id.eventNameEditText)).perform(click());
        onView(withId(R.id.eventNameEditText)).perform(ViewActions.typeText("Event Name"));
        onView(withId(R.id.eventNameEditText)).check(matches(withText("Event Name")));
    }
}
