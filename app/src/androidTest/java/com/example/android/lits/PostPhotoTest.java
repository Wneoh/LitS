package com.example.android.lits;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class PostPhotoTest {
    private String photo() { return "android.resource://com.example.android.lits/" + R.drawable.header; }
    @Test public void pickedPhotoEnablesPublishSurvivesRecreationAndCanBeRemoved() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor picker = new Instrumentation.ActivityMonitor() {
            @Override public Instrumentation.ActivityResult onStartActivity(Intent intent) {
                if (Intent.ACTION_OPEN_DOCUMENT.equals(intent.getAction())) {
                    android.content.ClipData clips = android.content.ClipData.newRawUri("photos", Uri.parse(photo()));
                    clips.addItem(new android.content.ClipData.Item(Uri.parse("android.resource://com.example.android.lits/" + R.drawable.profile)));
                    Intent result = new Intent();
                    result.setClipData(clips);
                    return new Instrumentation.ActivityResult(Activity.RESULT_OK, result);
                }
                return null;
            }
        };
        instrumentation.addMonitor(picker);
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DesignPreviewActivity.class).putExtra("screen", "editor");
        try (ActivityScenario<DesignPreviewActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.post_para)).check(matches(not(isEnabled())));
            onView(withId(R.id.add_photo)).perform(click());
            onView(withId(R.id.editor_photo)).check(matches(isDisplayed()));
            onView(withId(R.id.post_para)).check(matches(isEnabled()));
            scenario.recreate();
            onView(withId(R.id.editor_photo)).check(matches(isDisplayed()));
            onView(withId(R.id.post_para)).check(matches(isEnabled()));
            onView(withContentDescription("Select photo 2")).perform(click());
            onView(withId(R.id.remove_photo)).perform(scrollTo(), click());
            onView(withId(R.id.post_para)).check(matches(isEnabled()));
            onView(withId(R.id.remove_photo)).perform(scrollTo(), click());
            onView(withId(R.id.photo_preview_group)).check(matches(withEffectiveVisibility(Visibility.GONE)));
            onView(withId(R.id.post_para)).check(matches(not(isEnabled())));
        } finally { instrumentation.removeMonitor(picker); }
    }
    @Test public void readerPhotoOpensFullScreen() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DesignPreviewActivity.class)
                .putExtra("screen", "reader").putExtra("entry", "A photo and a thought.").putExtra("imageUrl", photo());
        try (ActivityScenario<DesignPreviewActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withContentDescription("Post photo 1. Tap to view full screen")).perform(click());
            onView(org.hamcrest.Matchers.allOf(withContentDescription("Full post photo"), isDisplayed())).check(matches(isDisplayed()));
            onView(withText("‹  Back to entry")).perform(click());
            onView(withId(R.id.reader_author)).check(matches(isDisplayed()));
        }
    }

    @Test public void cancelledPickerKeepsEditorUsable() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor() {
            @Override public Instrumentation.ActivityResult onStartActivity(Intent intent) {
                return Intent.ACTION_OPEN_DOCUMENT.equals(intent.getAction()) ?
                        new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null) : null;
            }
        };
        instrumentation.addMonitor(monitor);
        try (ActivityScenario<DesignPreviewActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), DesignPreviewActivity.class).putExtra("screen", "editor"))) {
            onView(withId(R.id.add_photo)).perform(click());
            onView(withId(R.id.photo_preview_group)).check(matches(withEffectiveVisibility(Visibility.GONE)));
            onView(withId(R.id.para)).perform(replaceText("My draft"), closeSoftKeyboard());
            onView(withId(R.id.post_para)).check(matches(isEnabled()));
        } finally { instrumentation.removeMonitor(monitor); }
    }

    @Test public void readerShowsEveryPhoto() {
        java.util.ArrayList<String> photos = new java.util.ArrayList<>();
        photos.add(photo()); photos.add(photo());
        try (ActivityScenario<DesignPreviewActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), DesignPreviewActivity.class)
                        .putExtra("screen", "reader").putExtra("entry", "Two photos").putStringArrayListExtra("imageUrls", photos))) {
            onView(withContentDescription("Next photo")).perform(click());
            onView(withText("2 / 2   ·   Swipe to browse")).check(matches(isDisplayed()));
            onView(withContentDescription("Post photo 2. Tap to view full screen")).perform(click());
            onView(org.hamcrest.Matchers.allOf(withContentDescription("Full post photo"), isDisplayed())).check(matches(isDisplayed()));
        }
    }
}

