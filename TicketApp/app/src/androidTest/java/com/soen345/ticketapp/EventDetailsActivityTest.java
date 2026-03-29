package com.soen345.ticketapp;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import com.soen345.ticketapp.ui.EventDetailsActivity;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketapp.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityTest {

    private Intent createIntent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        );
        intent.putExtra("eventId", "test123"); // fake id
        return intent;
    }

    @Test
    public void launchActivity() {
        try (ActivityScenario<EventDetailsActivity> scenario =
                     ActivityScenario.launch(createIntent())) {

            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void mainViewsVisible() {
        try (ActivityScenario<EventDetailsActivity> scenario =
                     ActivityScenario.launch(createIntent())) {

            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.tvCategory)).check(matches(isDisplayed()));
            onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTime)).check(matches(isDisplayed()));
            onView(withId(R.id.tvSeats)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void reserveButtonVisible() {
        try (ActivityScenario<EventDetailsActivity> scenario =
                     ActivityScenario.launch(createIntent())) {

            onView(withId(R.id.btnReserve)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void toolbarVisible() {
        try (ActivityScenario<EventDetailsActivity> scenario =
                     ActivityScenario.launch(createIntent())) {

            onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        }
    }
}