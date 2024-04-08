package com.example.mergefault;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);
    public IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void testActivityInView() {
        onView(withId(R.id.main)).check(matches(isDisplayed()));
    }

    @Test
    public void testUIVisibility() {
        onView(withId(R.id.attendeeButton)).check(matches(isDisplayed()));
        onView(withId(R.id.adminButton)).check(matches(isDisplayed()));
        onView(withId(R.id.organizerButton)).check(matches(isDisplayed()));
        onView(withId(R.id.homeScreenLogo)).check(matches(isDisplayed()));
    }

    @Test
    public void testButtonText() {
        onView(withId(R.id.attendeeButton)).check(matches(withText("ATTENDEE")));
        onView(withId(R.id.adminButton)).check(matches(withText("ADMINISTRATOR")));
        onView(withId(R.id.organizerButton)).check(matches(withText("ORGANIZER")));
    }
    @Test
    public void testAttendeeButton() {
        onView(withId(R.id.attendeeButton)).perform(click());
        onView(withId(R.id.attendeeHome)).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerButton() {
        onView(withId(R.id.organizerButton)).perform(click());
        onView(withId(R.id.organizerHome)).check(matches(isDisplayed()));
    }

    // Tests that the admin button is not visible as it should only be visible to the admins
    @Test
    public void testAdminButton() {
        onView(withId(R.id.adminButton)).perform(click());
    }
}