package com.soen345.ticketapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.util.regex.Pattern.matches;

import android.widget.Toast;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketapp.R;
import com.soen345.ticketapp.ui.LoginActivity;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Test
    public void activityLaunches() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.toggleAuthIntent)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void signInModeUI() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
            onView(withId(R.id.btnRegister)).check(matches(withEffectiveVisibility(GONE)));
            onView(withId(R.id.layoutLoginRole)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void switchingToRegisterMode() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.btnIntentCreateAccount)).perform(click());

            onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
            onView(withId(R.id.btnLogin)).check(matches(withEffectiveVisibility(GONE)));
            onView(withId(R.id.layoutLoginRole)).check(matches(withEffectiveVisibility(GONE)));
        }
    }

    @Test
    public void phoneModeUI() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.btnModePhone)).perform(click());

            onView(withId(R.id.layoutPhone)).check(matches(isDisplayed()));
            onView(withId(R.id.layoutEmail)).check(matches(withEffectiveVisibility(GONE)));
        }
    }

    @Test
    public void emailModeUI() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.btnModePhone)).perform(click());
            onView(withId(R.id.btnModeEmail)).perform(click());

            onView(withId(R.id.layoutEmail)).check(matches(isDisplayed()));
            onView(withId(R.id.layoutPhone)).check(matches(withEffectiveVisibility(GONE)));
        }
    }
}
