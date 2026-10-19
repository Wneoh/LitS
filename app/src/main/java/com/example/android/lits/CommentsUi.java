package com.example.android.lits;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.*;
import android.view.View;
import android.widget.*;
import java.util.List;

public final class CommentsUi {
    public interface Send { void send(String text); }
    private final Activity activity;
    private final EditText input;
    private final Button button;
    private boolean pending;
    public CommentsUi(Activity activity, Send send) {
        this.activity = activity;
        input = activity.findViewById(R.id.comment_input);
        button = activity.findViewById(R.id.send_comment);
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable text) { update(); }
        });
        button.setOnClickListener(v -> { String text = input.getText().toString().trim(); if (!pending && !text.isEmpty()) send.send(text); });
        update();
    }
    private void update() { button.setEnabled(!pending && !input.getText().toString().trim().isEmpty()); }
    public void pending(boolean value) { pending = value; input.setEnabled(!value); button.setText(value ? "Sending…" : "Send"); update(); }
    public void sent() { input.setText(""); pending(false); }
    public void status(String message) {
        TextView status = activity.findViewById(R.id.comments_status);
        status.setText(message); status.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
    }
    public void render(List<Comment> comments) {
        LinearLayout list = activity.findViewById(R.id.comments_list);
        list.removeAllViews();
        ((TextView) activity.findViewById(R.id.comments_heading)).setText("Comments (" + comments.size() + (comments.size() == 50 ? "+" : "") + ")");
        for (Comment comment : comments) {
            LinearLayout card = new LinearLayout(activity);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12), dp(12), dp(12), dp(12));
            card.setBackgroundResource(R.drawable.card_background);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
            cardParams.topMargin = dp(12);
            list.addView(card, cardParams);
            LinearLayout header = new LinearLayout(activity);
            header.setGravity(android.view.Gravity.CENTER_VERTICAL);
            card.addView(header);
            String author = comment.author == null || comment.author.trim().isEmpty() ? "Member" : comment.author.trim();
            TextView avatar = new TextView(activity);
            avatar.setText(author.substring(0, 1).toUpperCase(java.util.Locale.getDefault()));
            avatar.setTextSize(14); avatar.setGravity(android.view.Gravity.CENTER);
            avatar.setTextColor(ThemeManager.color(activity, R.attr.litsAccent));
            avatar.setBackgroundResource(R.drawable.avatar_background);
            avatar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            header.addView(avatar, new LinearLayout.LayoutParams(dp(32), dp(32)));
            LinearLayout identity = new LinearLayout(activity);
            identity.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams identityParams = new LinearLayout.LayoutParams(0, -2, 1);
            identityParams.setMarginStart(dp(10));
            header.addView(identity, identityParams);
            TextView name = new TextView(activity);
            name.setText(author);
            name.setTextSize(14); name.setTypeface(null, Typeface.BOLD);
            name.setTextColor(ThemeManager.color(activity, R.attr.litsText));
            identity.addView(name);
            TextView time = new TextView(activity);
            time.setText(comment.createdAt == null ? "Sending…" : android.text.format.DateUtils.getRelativeTimeSpanString(comment.createdAt, System.currentTimeMillis(), 60000));
            time.setTextSize(11); time.setTextColor(ThemeManager.color(activity, R.attr.litsMuted));
            identity.addView(time);
            TextView body = new TextView(activity); body.setText(comment.text); body.setTextSize(15);
            body.setTextColor(ThemeManager.color(activity, R.attr.litsText)); body.setTextIsSelectable(true);
            body.setPadding(0, dp(10), 0, 0);
            body.setLineSpacing(dp(3), 1f);
            card.addView(body);
        }
        if (!pending) status(comments.isEmpty() ? "Start the conversation. Be the first to comment." : comments.size() == 50 ? "Showing the latest 50 comments." : "");
    }
    private int dp(int value) { return Math.round(value * activity.getResources().getDisplayMetrics().density); }
}
