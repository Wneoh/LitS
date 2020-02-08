package com.example.android.lits;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.SimpleTimeZone;

public class ParaActivity extends AppCompatActivity {

    private EditText post;
    private Button UpdatePost;
    private Toolbar mToolbar;
    private String idea;
    private StorageReference PostReference;
    private String saveCurrentDate, saveCurrentTime, postTime;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference UserRef, PostRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_para);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PostReference = FirebaseStorage.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        post = (EditText) findViewById(R.id.para);
        UpdatePost = (Button) findViewById(R.id.post_para);
        loadingBar = new ProgressDialog(this);



        mToolbar = (Toolbar) findViewById(R.id.update_para_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update your post");


        UpdatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckIfEmpty();
            }
        });
    }

    private void CheckIfEmpty() {
        idea = post.getText().toString();

        if (TextUtils.isEmpty(idea)) {
            Toast.makeText(this, "Please write something before posting.", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Update");
            loadingBar.setMessage("Please wait, while we are updating your wonderful post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            SaveIdeaToPost();
        }

    }

    private void SaveIdeaToPost()
    {
        //Calendar Date = Calendar.getInstance();
       // SimpleDateFormat currentDate = new SimpleDateFormat("MMMM-dd-yyyy");
        //saveCurrentDate = currentDate.format(Date.getTime());

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String time = df.format(Calendar.getInstance().getTime());

        //final String difTime = saveCurrentTime+saveCurrentDate;

        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                  String userFullname = dataSnapshot.child("fullname").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", currentUserID);
                    postsMap.put("fullname",userFullname);
                    //postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", time);
                    postsMap.put("post",idea);
                    PostRef.child(currentUserID +time).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful()){
                                Toast.makeText(ParaActivity.this, "Post is updated sucessfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                SendUserToMainActivity();
                            }
                            else{
                                String message =task.getException().getMessage();
                                Toast.makeText(ParaActivity.this, "Error Occured"+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
                }else{
                    Toast.makeText(ParaActivity.this, "Error Occured, can't find your information", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ParaActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}
