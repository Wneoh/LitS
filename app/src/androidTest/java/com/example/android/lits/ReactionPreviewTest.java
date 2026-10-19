package com.example.android.lits;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class ReactionPreviewTest {
    @Test public void readerCanAddSwitchAndRemoveReaction() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DesignPreviewActivity.class)
                .putExtra("screen", "reader").putExtra("entry", "Reaction test " + System.nanoTime());
        try (ActivityScenario<DesignPreviewActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withContentDescription("Love, 2 reactions. Tap to react")).perform(click());
            onView(withContentDescription("Love, 3 reactions, selected. Tap to remove")).check(matches(isSelected()));
            onView(withContentDescription("Like, 0 reactions. Tap to react")).perform(click());
            onView(withContentDescription("Love, 2 reactions. Tap to react")).check(matches(isDisplayed()));
            onView(withContentDescription("Like, 1 reactions, selected. Tap to remove")).perform(click());
            onView(withContentDescription("Like, 0 reactions. Tap to react")).check(matches(isDisplayed()));
        }
    }
}
