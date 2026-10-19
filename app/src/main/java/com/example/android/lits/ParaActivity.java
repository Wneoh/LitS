package com.example.android.lits;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ParaActivity extends BaseActivity {
    private PostPhotoEditor photoEditor;
    private boolean publishing;
    private ProgressDialog progress;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ValueEventListener profileListener;
    private DatabaseReference profileRef;
    private UploadTask upload;
    private final List<StorageReference> uploadedPhotos = new ArrayList<>();
    private boolean writeStarted;
    private int attempt;
    private final Runnable timeout = () -> {
        if (isFinishing() || isDestroyed() || !publishing) return;
        progress.dismiss();
        if (!writeStarted) {
            fail("The server is not responding. Your draft is still here. Please try again when Firebase is available.");
        } else {
            new AlertDialog.Builder(this).setTitle("Waiting to publish")
                    .setMessage("The database has not confirmed this post yet. Keep this screen open; it will return to the feed once confirmed.")
                    .setPositiveButton("OK", null).show();
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_para);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class)); finish(); return;
        }
        EditorUi.bind(this);
        photoEditor = new PostPhotoEditor(this, state);
        progress = new ProgressDialog(this);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(false);
        progress.setButton(android.content.DialogInterface.BUTTON_NEGATIVE, "Cancel upload", (dialog, which) -> cancelOrLeave());
        progress.setOnCancelListener(dialog -> cancelOrLeave());
        Toolbar toolbar = findViewById(R.id.update_para_toolbar);
        toolbar.setTitle("New entry");
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationContentDescription("Back to feed");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        findViewById(R.id.post_para).setOnClickListener(v -> publish());
    }

    private void publish() {
        if (publishing) return;
        String text = ((EditText) findViewById(R.id.para)).getText().toString().trim();
        List<Uri> photos = photoEditor.getUris();
        if (text.isEmpty() && photos.isEmpty()) return;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) { fail("Please sign in again."); return; }
        publishing = true;
        final int request = ++attempt;
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        writeStarted = false;
        uploadedPhotos.clear();
        findViewById(R.id.post_para).setEnabled(false);
        progress.setTitle("Publishing entry");
        progress.setMessage("Preparing your post…");
        progress.show();
        progress.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setText("Cancel upload");
        handler.postDelayed(timeout, 45000);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference entry = FirebaseDatabase.getInstance().getReference("Posts").push();
        profileRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        profileListener = new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot data) {
                if (!publishing || request != attempt || isDestroyed()) return;
                String name = data.child("fullname").getValue(String.class);
                if (name == null || name.trim().isEmpty()) { fail("Complete your profile before publishing."); return; }
                Posts post = new Posts(text, name, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()), uid);
                post.setImageUrls(new ArrayList<>()); post.setImagePaths(new ArrayList<>());
                uploadNext(photos, 0, entry, post);
            }
            @Override public void onCancelled(DatabaseError error) { if (request == attempt) fail("Couldn't load your profile. Please try again."); }
        };
        profileRef.addListenerForSingleValueEvent(profileListener);
    }

    private void uploadNext(List<Uri> photos, int index, DatabaseReference entry, Posts post) {
        if (index == photos.size()) { save(entry, post); return; }
        Uri uri = photos.get(index);
        final int request = attempt;
        progress.setMessage("Preparing photo…");
        new Thread(() -> {
            try {
                Bitmap bitmap = Picasso.with(getApplicationContext()).load(uri).resize(1600, 1600).centerInside().onlyScaleDown().get();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 82, bytes);
                byte[] content = bytes.toByteArray();
                runOnUiThread(() -> {
                    if (!publishing || request != attempt || isDestroyed()) return;
                    progress.setMessage("Uploading photo " + (index + 1) + " of " + photos.size() + "…");
                    StorageReference file = FirebaseStorage.getInstance().getReference().child("Post Images").child(post.uid).child(entry.getKey() + "-" + index + ".jpg");
                    uploadedPhotos.add(file);
                    upload = file.putBytes(content, new StorageMetadata.Builder().setContentType("image/jpeg").build());
                    upload.addOnSuccessListener(result -> {
                        if (!publishing || request != attempt || isDestroyed()) { file.delete(); return; }
                        if (result.getDownloadUrl() == null) { fail("Couldn't obtain the photo URL. Please try again."); return; }
                        post.getImageUrls().add(result.getDownloadUrl().toString());
                        post.getImagePaths().add(file.getPath());
                        uploadNext(photos, index + 1, entry, post);
                    }).addOnFailureListener(error -> { if (publishing && request == attempt) fail("Photo upload failed. Your draft is still here."); });
                });
            } catch (Exception error) {
                runOnUiThread(() -> { if (publishing && request == attempt && !isDestroyed()) fail("Couldn't read this photo. Choose another image."); });
            }
        }, "post-photo").start();
    }

    private void save(DatabaseReference entry, Posts post) {
        writeStarted = true;
        progress.setMessage("Saving entry…");
        progress.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setText("Close");
        entry.setValue(post).addOnCompleteListener(task -> {
            if (isDestroyed()) return;
            if (!task.isSuccessful()) { fail("Couldn't publish this entry. Your draft is still here."); return; }
            handler.removeCallbacks(timeout);
            publishing = false;
            progress.dismiss();
            Toast.makeText(this, "Your entry is published", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });
    }

    private void cancelOrLeave() {
        if (writeStarted) {
            Toast.makeText(this, "Your post is still saving in the background.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        stopUpload();
        Toast.makeText(this, "Upload cancelled. Your draft is kept here.", Toast.LENGTH_SHORT).show();
    }
    private void stopUpload() {
        attempt++;
        publishing = false;
        handler.removeCallbacks(timeout);
        if (profileListener != null) profileRef.removeEventListener(profileListener);
        if (upload != null) { upload.cancel(); upload = null; }
        for (StorageReference file : uploadedPhotos) file.delete();
        uploadedPhotos.clear();
        if (progress != null) progress.dismiss();
        if (!isDestroyed()) {
            findViewById(R.id.post_para).setEnabled(true);
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
    private void fail(String message) {
        stopUpload();
        if (isDestroyed() || isFinishing()) return;
        new AlertDialog.Builder(this).setTitle("Couldn't publish").setMessage(message).setPositiveButton("OK", null).show();
    }
    @Override public void onBackPressed() {
        if (publishing) { cancelOrLeave(); return; }
        super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        if (photoEditor != null) photoEditor.save(state);
        super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() {
        handler.removeCallbacks(timeout);
        if (profileListener != null) profileRef.removeEventListener(profileListener);
        if (publishing && !writeStarted) stopUpload();
        if (progress != null) progress.dismiss();
        super.onDestroy();
    }
}
