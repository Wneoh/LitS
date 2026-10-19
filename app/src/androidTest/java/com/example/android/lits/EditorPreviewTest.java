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
public class EditorPreviewTest {
    @Test
    public void editorCountsWordsAndRejectsBlankEntries() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DesignPreviewActivity.class)
                .putExtra("screen", "editor");
        try (ActivityScenario<DesignPreviewActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.post_para)).check(matches(not(isEnabled())));
            onView(withId(R.id.para)).perform(click(), replaceText("A thought worth sharing"), closeSoftKeyboard());
            onView(withId(R.id.para)).check(matches(withText("A thought worth sharing")));
            onView(withId(R.id.word_count)).check(matches(withText("4 words")));
            onView(withId(R.id.post_para)).check(matches(isEnabled()));
            scenario.recreate();
            onView(withId(R.id.para)).check(matches(withText("A thought worth sharing")));
            onView(withId(R.id.word_count)).check(matches(withText("4 words")));
            onView(withId(R.id.para)).perform(replaceText("   "), closeSoftKeyboard());
            onView(withId(R.id.word_count)).check(matches(withText("0 words")));
            onView(withId(R.id.post_para)).check(matches(not(isEnabled())));
        }
    }
}
