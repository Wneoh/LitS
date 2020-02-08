package com.example.android.lits;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

public class ClickPostActivity extends AppCompatActivity {


    private TextView Post;
    private Button DeletePost, EditPost;

    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, currentUserID, databaseUserId, user_post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        Intent keyIntent = getIntent();
        PostKey = keyIntent.getStringExtra(MainActivity.POST_KEY);


        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);


        Post = findViewById(R.id.click_post);
        DeletePost = findViewById(R.id.delete_post);
        EditPost = findViewById(R.id.edit_post);

        DeletePost.setVisibility(View.INVISIBLE);
        EditPost.setVisibility(View.INVISIBLE);


        DeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteSelectedPost();
            }
        });


        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void EditSelectedPost(String post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit post");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(post);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ClickPostRef.child("post").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post updated sucessfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_red_light);
    }

    private void DeleteSelectedPost() {
        ClickPostRef.removeValue();
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
        Toast.makeText(this, "Your amazing post has been deleted.", Toast.LENGTH_SHORT).show();
    }

}

