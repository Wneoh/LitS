package com.example.android.lits;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public final class PostPhotos {
    private PostPhotos() {}
    private static int dp(Context c, int value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }
    public static void bindFeed(View card, List<String> urls) {
        ImageView single = card.findViewById(R.id.post_photo);
        LinearLayout grid = card.findViewById(R.id.feed_gallery);
        grid.removeAllViews();
        grid.setVisibility(urls.size() > 1 ? View.VISIBLE : View.GONE);
        bindFeed(single, urls.size() == 1 ? urls.get(0) : null, card);
        if (urls.size() > 1) {
            int shown = Math.min(4, urls.size());
            for (int col = 0; col < 2; col++) {
                LinearLayout column = new LinearLayout(card.getContext());
                column.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -1, 1);
                if (col == 0) cp.setMarginEnd(dp(card.getContext(), 4));
                grid.addView(column, cp);
                int rows = shown == 2 || (shown == 3 && col == 0) ? 1 : 2;
                for (int row = 0; row < rows; row++) {
                    int index = shown == 4 ? col * 2 + row : col == 0 ? 0 : 1 + row;
                    FrameLayout tile = new FrameLayout(card.getContext());
                    LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, 0, 1);
                    if (row == 0 && rows > 1) tp.bottomMargin = dp(card.getContext(), 4);
                    column.addView(tile, tp);
                    ImageView image = new ImageView(card.getContext());
                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    tile.addView(image, new FrameLayout.LayoutParams(-1, -1));
                    load(image, urls.get(index));
                    tile.setContentDescription("Photo " + (index + 1) + " of " + urls.size() + ". Open entry");
                    tile.setOnClickListener(v -> card.performClick());
                    if (index == 3 && urls.size() > 4) {
                        TextView more = new TextView(card.getContext());
                        more.setText("+" + (urls.size() - 4)); more.setTextSize(26);
                        more.setTextColor(android.graphics.Color.WHITE); more.setGravity(Gravity.CENTER);
                        more.setBackgroundColor(0x77000000);
                        tile.addView(more, new FrameLayout.LayoutParams(-1, -1));
                    }
                }
            }
        }
        TextView count = card.findViewById(R.id.photo_count);
        count.setText(urls.size() + " photos");
        count.setVisibility(urls.size() > 1 ? View.VISIBLE : View.GONE);
    }
    public static void bindDetails(View root, List<String> urls) {
        root.findViewById(R.id.post_photo).setVisibility(View.GONE);
        LinearLayout gallery = root.findViewById(R.id.extra_photos);
        gallery.removeAllViews();
        gallery.setVisibility(urls.isEmpty() ? View.GONE : View.VISIBLE);
        if (!urls.isEmpty()) addPager(gallery, urls, false, 0);
    }
    public static void addPager(LinearLayout gallery, List<String> urls, boolean fullScreen, int start) {
        Context context = gallery.getContext();
        ViewPager pager = new ViewPager(context);
        pager.setContentDescription("Post photo gallery");
        gallery.addView(pager, fullScreen ? new LinearLayout.LayoutParams(-1, 0, 1) : new LinearLayout.LayoutParams(-1, dp(context, 240)));
        LinearLayout controls = new LinearLayout(context);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        gallery.addView(controls, new LinearLayout.LayoutParams(-1, dp(context, 48)));
        TextView previous = control(context, "‹", "Previous photo");
        TextView counter = control(context, "", "Photo position");
        TextView next = control(context, "›", "Next photo");
        controls.addView(previous, new LinearLayout.LayoutParams(dp(context, 48), -1));
        controls.addView(counter, new LinearLayout.LayoutParams(0, -1, 1));
        controls.addView(next, new LinearLayout.LayoutParams(dp(context, 48), -1));
        pager.setAdapter(new PagerAdapter() {
            @Override public int getCount() { return urls.size(); }
            @Override public boolean isViewFromObject(View view, Object object) { return view == object; }
            @Override public Object instantiateItem(ViewGroup parent, int position) {
                ImageView image = new RoundedPhotoView(context);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                image.setContentDescription(fullScreen ? "Full post photo" : "Post photo " + (position + 1) + ". Tap to view full screen");
                load(image, urls.get(position));
                if (!fullScreen) image.setOnClickListener(v -> context.startActivity(new Intent(context, PhotoActivity.class)
                        .putStringArrayListExtra("imageUrls", new ArrayList<>(urls)).putExtra("photoIndex", position)));
                parent.addView(image, new ViewGroup.LayoutParams(-1, -1));
                return image;
            }
            @Override public void destroyItem(ViewGroup parent, int position, Object object) {
                ImageView image = (ImageView) object;
                Picasso.with(context).cancelRequest(image); parent.removeView(image);
            }
        });
        Runnable update = () -> {
            counter.setText((pager.getCurrentItem() + 1) + " / " + urls.size() + (urls.size() > 1 ? "   ·   Swipe to browse" : ""));
            previous.setEnabled(pager.getCurrentItem() > 0); previous.setAlpha(previous.isEnabled() ? 1f : .3f);
            next.setEnabled(pager.getCurrentItem() < urls.size() - 1); next.setAlpha(next.isEnabled() ? 1f : .3f);
        };
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int position) { update.run(); }
        });
        previous.setOnClickListener(v -> pager.setCurrentItem(pager.getCurrentItem() - 1));
        next.setOnClickListener(v -> pager.setCurrentItem(pager.getCurrentItem() + 1));
        pager.setCurrentItem(Math.max(0, Math.min(start, urls.size() - 1)), false); update.run();
    }
    private static TextView control(Context context, String text, String description) {
        TextView view = new TextView(context); view.setText(text); view.setContentDescription(description);
        view.setGravity(Gravity.CENTER); view.setTextSize(text.isEmpty() ? 12 : 24);
        view.setTextColor(ThemeManager.color(context, R.attr.litsMuted)); return view;
    }
    private static void load(ImageView image, String url) {
        Picasso.with(image.getContext()).load(url).resize(1400, 1400).centerInside()
                .error(android.R.drawable.ic_menu_report_image).into(image);
    }
    public static void bindFeed(ImageView image, String url, View card) {
        bind(image, url); image.setContentDescription("Post photo. Tap to read entry");
        image.setOnClickListener(v -> card.performClick());
    }
    public static void bind(ImageView image, String url) {
        Picasso.with(image.getContext()).cancelRequest(image); image.setImageDrawable(null); image.setOnClickListener(null);
        boolean visible = url != null && !url.trim().isEmpty(); image.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) return;
        load(image, url);
        image.setOnClickListener(v -> image.getContext().startActivity(new Intent(image.getContext(), PhotoActivity.class).putExtra("imageUrl", url)));
    }
}
