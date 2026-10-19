package com.example.android.lits;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

/** Separate bounded listener; no success message until Firebase acknowledges the write. */
public final class PostComments {
    private final Activity activity;
    private CommentsUi ui;
    private int generation;
    private final DatabaseReference comments;
    private final Query query;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ValueEventListener listener;
    private ValueEventListener profileListener;
    private DatabaseReference profile;
    private boolean closed, sending, writing;
    private String pendingKey;
    private final List<Comment> visible = new ArrayList<>();
    private final Runnable timeout = () -> {
        if (closed || !sending) return;
        if (!writing) {
            if (profileListener != null) profile.removeEventListener(profileListener);
            generation++;
            sending = false; ui.pending(false);
            ui.status("Couldn't connect. Your comment is kept below; try again.");
        } else ui.status("Still waiting for the database. Your comment hasn't been confirmed yet.");
    };
    public PostComments(Activity activity, DatabaseReference post) {
        this.activity = activity;
        comments = post.child("comments");
        ui = new CommentsUi(activity, this::send);
        query = comments.orderByChild("createdAt").limitToLast(50);
        listener = new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                if (closed) return;
                visible.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.getKey().equals(pendingKey)) continue;
                    Comment comment = child.getValue(Comment.class);
                    if (comment != null) visible.add(comment);
                }
                ui.render(visible);
            }
            @Override public void onCancelled(DatabaseError error) {
                if (!closed) ui.status("Couldn't load comments. Check your connection or reopen this entry.");
            }
        };
        query.addValueEventListener(listener);
    }
    private void send(String text) {
        if (sending || closed) return;
        if (text.length() > 1000) { ui.status("Keep your comment within 1,000 characters."); return; }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) { ui.status("Sign in to leave a comment."); return; }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sending = true; writing = false; ui.pending(true); ui.status("Sending your comment…");
        final int request = ++generation;
        handler.postDelayed(timeout, 15000);
        profile = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        profileListener = new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                if (closed || !sending || request != generation) return;
                String name = snapshot.child("fullname").getValue(String.class);
                if (name == null || name.trim().isEmpty()) { failed("Complete your profile before commenting."); return; }
                Map<String, Object> values = new HashMap<>();
                values.put("uid", uid); values.put("author", name); values.put("text", text);
                values.put("createdAt", ServerValue.TIMESTAMP);
                DatabaseReference entry = comments.push();
                pendingKey = entry.getKey(); writing = true;
                entry.setValue(values).addOnCompleteListener(task -> {
                    if (closed) return;
                    handler.removeCallbacks(timeout);
                    pendingKey = null;
                    if (!task.isSuccessful()) { failed("Couldn't send. Your comment is kept below; try again."); return; }
                    sending = false; ui.sent(); ui.status("");
                    // The local event was hidden while pending; fetch again for the acknowledged value.
                    query.addListenerForSingleValueEvent(listener);
                });
            }
            @Override public void onCancelled(DatabaseError error) { if (!closed && request == generation) failed("Couldn't load your profile. Please try again."); }
        };
        profile.addListenerForSingleValueEvent(profileListener);
    }
    private void failed(String message) {
        handler.removeCallbacks(timeout); sending = false; writing = false;
        ui.pending(false); ui.status(message);
    }
    public void close() {
        closed = true; handler.removeCallbacks(timeout); query.removeEventListener(listener);
        if (profileListener != null) profile.removeEventListener(profileListener);
    }
}
