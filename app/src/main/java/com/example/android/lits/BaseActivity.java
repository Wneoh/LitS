package com.example.android.lits;

import androidx.appcompat.app.AppCompatActivity;

/** Keeps every screen clear of system bars, display cutouts, and the keyboard. */
public abstract class BaseActivity extends AppCompatActivity {
    private int appliedTheme;

    @Override
    protected void onCreate(android.os.Bundle state) {
        appliedTheme = ThemeManager.selected(this);
        ThemeManager.apply(this);
        super.onCreate(state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appliedTheme != ThemeManager.selected(this)) recreate();
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        ScreenInsets.apply(this);
    }
}
