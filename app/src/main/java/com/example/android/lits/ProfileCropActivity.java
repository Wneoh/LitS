package com.example.android.lits;

import android.os.Bundle;

import com.theartofdev.edmodo.cropper.CropImageActivity;

/** Applies the same Android 16 safe area to the third-party crop screen. */
public class ProfileCropActivity extends CropImageActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        ScreenInsets.apply(this);
    }
}
