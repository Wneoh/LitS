package com.example.android.lits;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AuthenticationScreensTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> login =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void registrationRemainsReachableWithKeyboardOpen() {
        onView(withId(R.id.NeedNewAccountLink)).perform(scrollTo(), click());
        onView(withId(R.id.register_email)).perform(click(), typeText("preview@example.com"));
        onView(withId(R.id.register_confirm_password)).perform(scrollTo(), click(), typeText("preview-only"));
        // A real IME reduces the available space. The bottom action must remain reachable.
        onView(withId(R.id.register_create_account)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.register_confirm_password)).perform(closeSoftKeyboard());
        // Deliberately do not submit: this test does not create a Firebase account.
    }

    @Test
    public void loginKeepsEnteredEmailAfterActivityRecreation() {
        onView(withId(R.id.login_email)).perform(click(), typeText("preview@example.com"), closeSoftKeyboard());
        login.getScenario().recreate();
        onView(withId(R.id.login_email)).check(matches(withText("preview@example.com")));
        onView(withId(R.id.login)).perform(scrollTo()).check(matches(isDisplayed()));
    }
}
