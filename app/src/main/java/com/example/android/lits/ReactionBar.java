package com.example.android.lits;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.Map;

/** Shared themed reaction controls for feed cards and the reader. */
public final class ReactionBar {
    public interface Selection { void select(String key); }
    private ReactionBar() {}

    public static void bind(LinearLayout container, Map<String, String> reactions,
                            String userId, Selection selection) {
        Context context = container.getContext();
        container.removeAllViews();
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(context);
        scroll.addView(row);
        container.addView(scroll);
        for (int i = 0; i < PostReactions.KEYS.length; i++) {
            String key = PostReactions.KEYS[i];
            int count = PostReactions.count(reactions, key);
            boolean selected = reactions != null && userId != null && key.equals(reactions.get(userId));
            TextView chip = new TextView(context);
            chip.setText(PostReactions.EMOJI[i] + (count > 0 ? " " + count : ""));
            chip.setTextSize(13);
            chip.setSingleLine(true);
            chip.setMinWidth(dp(context, 48));
            chip.setGravity(Gravity.CENTER);
            chip.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
            chip.setTextColor(ThemeManager.color(context, selected ? R.attr.litsAccent : R.attr.litsMuted));
            chip.setSelected(selected);
            chip.setContentDescription(PostReactions.LABELS[i] + ", " + count + " reactions" +
                    (selected ? ", selected. Tap to remove" : ". Tap to react"));
            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(dp(context, 12));
            background.setColor(selected ? ThemeManager.color(context, R.attr.litsSoftAccent) : android.graphics.Color.TRANSPARENT);
            chip.setBackground(new android.graphics.drawable.InsetDrawable(background, 0, dp(context, 8), 0, dp(context, 8)));
            chip.setPadding(dp(context, 8), 0, dp(context, 8), 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(context, 48));
            row.addView(chip, params);
            chip.setFocusable(true);
            chip.setOnClickListener(view -> selection.select(key));
        }
    }

    public static void bindFirebase(LinearLayout container, Posts post, DatabaseReference reference) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() == null ? null :
                FirebaseAuth.getInstance().getCurrentUser().getUid();
        bind(container, post.getReactions(), uid, key -> {
            Context context = container.getContext().getApplicationContext();
            if (uid == null) {
                Toast.makeText(context, "Sign in to react to this entry.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Transaction touches only this user's value; other users' reactions cannot be overwritten.
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable pending = () -> Toast.makeText(context,
                    "Reaction is still waiting for the database. Check your connection.", Toast.LENGTH_LONG).show();
            handler.postDelayed(pending, 15000);
            reference.child("reactions").child(uid).runTransaction(new Transaction.Handler() {
                @Override public Transaction.Result doTransaction(MutableData data) {
                    data.setValue(PostReactions.toggle(data.getValue(String.class), key));
                    return Transaction.success(data);
                }
                @Override public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                    handler.removeCallbacks(pending);
                    if (error != null || !committed) Toast.makeText(context,
                            "Couldn't save your reaction. Please try again.", Toast.LENGTH_LONG).show();
                }
            }, false);
        });
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
