package com.example.android.lits;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class PhotoActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        ArrayList<String> urls = getIntent().getStringArrayListExtra("imageUrls");
        if (urls == null) {
            urls = new ArrayList<>();
            String url = getIntent().getStringExtra("imageUrl");
            if (url != null && !url.isEmpty()) urls.add(url);
        }
        if (urls.isEmpty()) { finish(); return; }
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        TextView close = new TextView(this); close.setText("‹  Back to entry");
        close.setTextColor(ThemeManager.color(this, R.attr.litsAccent)); close.setTextSize(16);
        close.setGravity(Gravity.CENTER_VERTICAL); close.setPadding(48, 0, 48, 0);
        close.setOnClickListener(v -> finish());
        root.addView(close, new LinearLayout.LayoutParams(-1, Math.round(56 * getResources().getDisplayMetrics().density)));
        PostPhotos.addPager(root, urls, true, getIntent().getIntExtra("photoIndex", 0));
        setContentView(root);
    }
}
