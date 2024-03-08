package com.example.mergefault;

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

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AttendeeTest {

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
        // need to complete
    }

    @Test
    public void testBrowsePostedEventsClick() {
        onView(withId(R.id.browseEventsButton)).perform(click());
        onView(withId(R.id.myEventListView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        // please add the tests here which check for the array adapter data
    }

    @Test
    public void profileActivitySwitchCheck() {
        onView(withId(R.id.profileImageView)).perform(click());
        onView(withId(R.id.cancelButton)).perform(click());
        onView(withId(R.id.viewMyEventsButton)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }

}
