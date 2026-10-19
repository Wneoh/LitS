package com.example.android.lits;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assume.assumeNotNull;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileSaveFeedbackTest {
    @Test public void stalledSaveShowsFeedbackWithoutLeavingSetup() {
        // Uses the emulator's existing session; never creates an account or writes data.
        assumeNotNull(FirebaseAuth.getInstance().getCurrentUser());
        try (ActivityScenario<SetupActivity> scenario = ActivityScenario.launch(SetupActivity.class)) {
            scenario.onActivity(activity -> {
                try {
                    java.lang.reflect.Field pending = SetupActivity.class.getDeclaredField("savePending");
                    pending.setAccessible(true);
                    pending.setBoolean(activity, true);
                    java.lang.reflect.Field timeout = SetupActivity.class.getDeclaredField("saveTimeout");
                    timeout.setAccessible(true);
                    ((Runnable) timeout.get(activity)).run();
                    assertFalse(activity.isFinishing());
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            });
            onView(withText("Profile has not saved yet")).check(matches(isDisplayed()));
        }
    }
}
