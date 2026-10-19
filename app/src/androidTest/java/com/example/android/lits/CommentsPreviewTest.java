package com.example.android.lits;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class CommentsPreviewTest {
    @Test public void commentsRejectBlankAndAppendReply() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DesignPreviewActivity.class)
                .putExtra("screen", "reader").putExtra("entry", "Comment test " + System.nanoTime());
        try (ActivityScenario<DesignPreviewActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.send_comment)).check(matches(not(isEnabled())));
            onView(withId(R.id.comment_input)).perform(replaceText("   "), closeSoftKeyboard());
            onView(withId(R.id.send_comment)).check(matches(not(isEnabled())));
            onView(withId(R.id.comment_input)).perform(replaceText("Lovely photos!"), closeSoftKeyboard());
            scenario.recreate();
            onView(withId(R.id.comment_input)).check(matches(withText("Lovely photos!")));
            onView(withId(R.id.send_comment)).perform(click());
            onView(withId(R.id.comment_input)).check(matches(withText("")));
            onView(withText("Lovely photos!")).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.comments_heading)).check(matches(withText("Comments (3)")));
            scenario.recreate();
            onView(withText("Lovely photos!")).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }
}
