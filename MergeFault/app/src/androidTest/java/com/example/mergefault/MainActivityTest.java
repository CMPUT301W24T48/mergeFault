package com.example.mergefault;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);



    /*
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.mergefault", appContext.getPackageName());
    }
     */

    @Test
    public void testAttendeeButton() {
        onView(withId(R.id.attendeeButton)).perform(click());
        onView(withId(R.id.viewMyEventsButton)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testOrganizerButton() {
        onView(withId(R.id.organizerButton)).perform(click());
        onView(withId(R.id.createNewEventButton)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    // Tests that the admin button is not visible as it should only be visible to the admins
    @Test
    public void testAdminButton() {
        onView(withId(R.id.adminButton)).check(ViewAssertions.matches(withEffectiveVisibility(GONE)));
        onView(withId(R.id.manageEventsButton)).check(ViewAssertions.doesNotExist());
    }
}