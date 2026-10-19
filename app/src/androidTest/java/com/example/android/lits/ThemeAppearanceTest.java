package com.example.android.lits;

import android.content.Context;
import android.widget.EditText;
import androidx.core.view.WindowCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ThemeAppearanceTest {
    @Test
    public void themeChoicesPersistAndRecolorNewScreens() {
        Context context = ApplicationProvider.getApplicationContext();
        int original = ThemeManager.selected(context);
        try {
            for (int index = 0; index < ThemeManager.NAMES.length; index++) {
                final int selected = index;
                try (ActivityScenario<ThemeActivity> picker = ActivityScenario.launch(ThemeActivity.class)) {
                    onView(withContentDescription(startsWith(ThemeManager.NAMES[index] + ",")))
                            .perform(scrollTo(), click());
                    picker.recreate();
                    picker.onActivity(activity -> assertEquals(selected, ThemeManager.selected(activity)));
                }
                try (ActivityScenario<LoginActivity> login = ActivityScenario.launch(LoginActivity.class)) {
                    login.onActivity(activity -> {
                        EditText email = activity.findViewById(R.id.login_email);
                        Context palette = ThemeManager.preview(activity, selected);
                        assertEquals(ThemeManager.color(palette, R.attr.litsText), email.getCurrentTextColor());
                        assertEquals(selected != 3, WindowCompat.getInsetsController(
                                activity.getWindow(), activity.getWindow().getDecorView()).isAppearanceLightStatusBars());
                    });
                }
                try (ActivityScenario<DesignPreviewActivity> feed = ActivityScenario.launch(DesignPreviewActivity.class)) {
                    onView(withId(R.id.add_new_para)).check(matches(isDisplayed()));
                    feed.onActivity(activity -> assertEquals(
                            ThemeManager.color(ThemeManager.preview(activity, selected), R.attr.litsBackground),
                            ThemeManager.color(activity, R.attr.litsBackground)));
                }
            }
        } finally {
            ThemeManager.save(context, original);
        }
    }
}
