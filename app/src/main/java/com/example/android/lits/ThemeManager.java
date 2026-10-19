package com.example.android.lits;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;

public final class ThemeManager {
    private ThemeManager() {}
    public static final String[] NAMES = {"Classic", "Forest", "Sepia", "Midnight"};
    private static final int[] PALETTES = {R.style.PaletteClassic, R.style.PaletteForest,
            R.style.PaletteSepia, R.style.PaletteMidnight};

    public static int selected(Context context) {
        int value = context.getSharedPreferences("appearance", Context.MODE_PRIVATE).getInt("theme", 0);
        return value >= 0 && value < PALETTES.length ? value : 0;
    }

    public static void save(Context context, int value) {
        if (value < 0 || value >= PALETTES.length) throw new IllegalArgumentException("Unknown theme");
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit().putInt("theme", value).apply();
    }

    public static void apply(Activity activity) {
        activity.getTheme().applyStyle(PALETTES[selected(activity)], true);
    }

    public static Context preview(Context context, int index) {
        android.view.ContextThemeWrapper wrapper = new android.view.ContextThemeWrapper(context, R.style.AppTheme);
        wrapper.getTheme().applyStyle(PALETTES[index], true);
        return wrapper;
    }

    public static int color(Context context, int attribute) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attribute, value, true);
        return value.data;
    }
}
