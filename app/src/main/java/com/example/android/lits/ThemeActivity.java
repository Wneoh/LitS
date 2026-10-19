package com.example.android.lits;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.card.MaterialCardView;

public class ThemeActivity extends BaseActivity {
    private final String[] descriptions = {"Clean neutrals, signature burgundy", "Soft greens and a calmer feel",
            "Warm paper tones for reading", "Deep surfaces, soft lavender"};

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_theme);
        Toolbar toolbar = findViewById(R.id.theme_toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.getNavigationIcon().setTint(ThemeManager.color(this, R.attr.litsText));
        toolbar.setNavigationContentDescription("Back");
        toolbar.setNavigationOnClickListener(view -> finish());
        LinearLayout options = findViewById(R.id.theme_options);
        for (int i = 0; i < ThemeManager.NAMES.length; i++) {
            final int index = i;
            Context palette = ThemeManager.preview(this, i);
            int accent = ThemeManager.color(palette, R.attr.litsAccent);
            boolean selected = ThemeManager.selected(this) == i;
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(12);
            card.setLayoutParams(params);
            card.setRadius(dp(16));
            card.setCardElevation(0);
            card.setCardBackgroundColor(ThemeManager.color(palette, R.attr.litsBackground));
            card.setStrokeColor(selected ? accent : ThemeManager.color(palette, R.attr.litsBorder));
            card.setStrokeWidth(dp(selected ? 2 : 1));
            card.setCheckable(true);
            card.setChecked(selected);
            card.setFocusable(true);
            card.setContentDescription(ThemeManager.NAMES[i] + ", " + descriptions[i] + (selected ? ", selected" : ""));
            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dp(20), dp(16), dp(20), dp(16));
            body.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            TextView title = new TextView(this);
            title.setText(ThemeManager.NAMES[i] + (selected ? "  ✓" : ""));
            title.setTextSize(18);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextColor(ThemeManager.color(palette, R.attr.litsText));
            body.addView(title);
            TextView subtitle = new TextView(this);
            subtitle.setText(descriptions[i]);
            subtitle.setTextSize(13);
            subtitle.setTextColor(ThemeManager.color(palette, R.attr.litsMuted));
            subtitle.setPadding(0, dp(4), 0, dp(12));
            body.addView(subtitle);
            LinearLayout swatches = new LinearLayout(this);
            for (int attribute : new int[]{R.attr.litsAccent, R.attr.litsSoftAccent, R.attr.litsSurface, R.attr.litsText}) {
                View swatch = new View(this);
                android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                shape.setCornerRadius(dp(8));
                shape.setColor(ThemeManager.color(palette, attribute));
                shape.setStroke(dp(1), ThemeManager.color(palette, R.attr.litsBorder));
                swatch.setBackground(shape);
                LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(44), dp(24));
                size.setMarginEnd(dp(8));
                swatches.addView(swatch, size);
            }
            body.addView(swatches);
            card.addView(body);
            card.setOnClickListener(view -> {
                if (ThemeManager.selected(this) != index) {
                    ThemeManager.save(this, index);
                    recreate();
                }
            });
            options.addView(card);
        }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
