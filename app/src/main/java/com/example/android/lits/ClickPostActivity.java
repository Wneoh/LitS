package com.example.android.lits;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClickPostActivity extends BaseActivity {


    private TextView Post;
    private Button DeletePost, EditPost;

    private DatabaseReference ClickPostRef;
    private ValueEventListener postListener;
    private Posts loadedPost;
    private boolean deleting;
    private PostComments comments;
    private java.util.List<String> displayedPhotos;
    private FirebaseAuth mAuth;

    private String PostKey, currentUserID, databaseUserId, user_post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        findViewById(R.id.read_back).setOnClickListener(view -> finish());

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) { finish(); return; }
        currentUserID = mAuth.getCurrentUser().getUid();

        Intent keyIntent = getIntent();
        PostKey = keyIntent.getStringExtra(MainActivity.POST_KEY);
        if (PostKey == null || PostKey.isEmpty()) { finish(); return; }


        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        comments = new PostComments(this, ClickPostRef);


        Post = findViewById(R.id.click_post);
        DeletePost = findViewById(R.id.delete_post);
        EditPost = findViewById(R.id.edit_post);

        DeletePost.setVisibility(View.GONE);
        EditPost.setVisibility(View.GONE);


        DeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteSelectedPost();
            }
        });


        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    loadedPost = dataSnapshot.getValue(Posts.class);
                    ReactionBar.bindFirebase(findViewById(R.id.post_reactions), loadedPost, ClickPostRef);
                    if (!loadedPost.photos().equals(displayedPhotos)) {
                        displayedPhotos = new java.util.ArrayList<>(loadedPost.photos());
                        PostPhotos.bindDetails(findViewById(android.R.id.content), displayedPhotos);
                    }
                    ((TextView) findViewById(R.id.reader_author)).setText(loadedPost.getFullname());
                    ((TextView) findViewById(R.id.reader_date)).setText(loadedPost.getTime());
                    String name = loadedPost.getFullname() == null ? "" : loadedPost.getFullname().trim();
                    ((TextView) findViewById(R.id.reader_initial)).setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(java.util.Locale.getDefault()));
                    Post.setText(dataSnapshot.child("post").getValue().toString());
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();


                    if (currentUserID.equals(databaseUserId)) {
                        DeletePost.setVisibility(View.VISIBLE);
                        EditPost.setVisibility(View.VISIBLE);

                    }
                    EditPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditSelectedPost(Post.getText().toString());

                        }
                    });
                } else if (!deleting) { finish(); }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ClickPostRef.addValueEventListener(postListener);
    }

    @Override protected void onDestroy() {
        if (comments != null) comments.close();
        if (ClickPostRef != null && postListener != null) ClickPostRef.removeEventListener(postListener);
        super.onDestroy();
    }

    private void EditSelectedPost(String post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit post");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(post);
        builder.setView(inputField);

        builder.setPositiveButton("Update", null);
        /* Save after validation, leaving the dialog open when the caption is invalid. */
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog editDialog = builder.create();
        editDialog.setOnShowListener(ignored -> editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String value = inputField.getText().toString().trim();
            if (value.isEmpty() && (loadedPost == null || loadedPost.photos().isEmpty())) {
                inputField.setError("Write something for this entry."); return;
            }
            editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            ClickPostRef.child("post").setValue(value).addOnCompleteListener(task -> {
                if (isDestroyed()) return;
                editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if (task.isSuccessful()) { editDialog.dismiss(); Toast.makeText(this, "Entry updated", Toast.LENGTH_SHORT).show(); }
                else inputField.setError("Couldn't save. Please try again.");
            });
        }));
        editDialog.show();
    }

    private void DeleteSelectedPost() {
        new AlertDialog.Builder(this).setTitle("Delete this entry?")
                .setMessage("This removes the entry, comments and reactions. This cannot be undone.")
                .setNegativeButton("Keep entry", null).setPositiveButton("Delete", (dialog, which) -> {
                    deleting = true;
                    DeletePost.setEnabled(false);
                    java.util.List<String> paths = loadedPost == null ? java.util.Collections.emptyList() : loadedPost.photoPaths();
                    ClickPostRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Only clean up images inside the signed-in author's post folder.
                            for (String path : paths) if (path != null && path.startsWith("/Post Images/" + currentUserID + "/"))
                                com.google.firebase.storage.FirebaseStorage.getInstance().getReference().child(path).delete();
                            if (!isDestroyed()) finish();
                        } else if (!isDestroyed()) {
                            deleting = false;
                            DeletePost.setEnabled(true);
                            Toast.makeText(this, "Couldn't delete this entry. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                }).show();
    }

}
