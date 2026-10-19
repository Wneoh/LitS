package com.example.android.lits;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.theartofdev.edmodo.cropper.CropImage;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ProfileCropScreenTest {
    @Test
    public void cropToolbarStaysBelowSystemStatusBar() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Uri image = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.profile);
        Intent intent = CropImage.activity(image).setAspectRatio(1, 1)
                .getIntent(context, ProfileCropActivity.class);
        try (ActivityScenario<ProfileCropActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(com.theartofdev.edmodo.cropper.R.id.cropImageView))
                    .check(matches(isDisplayed()));
            scenario.onActivity(activity -> {
                View toolbar = activity.findViewById(androidx.appcompat.R.id.action_bar);
                assertNotNull(toolbar);
                int[] location = new int[2];
                toolbar.getLocationOnScreen(location);
                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
                assertNotNull(insets);
                int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                        | WindowInsetsCompat.Type.displayCutout()).top;
                assertTrue("Crop toolbar overlaps the status bar", location[1] >= topInset);
            });
        }
    }
}
