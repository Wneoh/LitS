package com.example.android.lits;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.concurrent.RecursiveAction;

import de.hdodenhof.circleimageview.CircleImageView;



public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private android.support.v7.widget.Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton NewPara;

    public static final String POST_KEY = "postKey";
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,PostRef;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        NewPara = findViewById(R.id.add_new_para);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_mage);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        NewPara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

        DisplayAllPosts();

    }

    private void DisplayAllPosts()
    {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_post_layout,
                                PostsViewHolder.class,
                                PostRef.orderByChild("time")
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {
                        final String PostKey = getRef(position).getKey();


                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setPost(model.getPost());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                                clickPostIntent.putExtra(POST_KEY,PostKey);
                                startActivity(clickPostIntent);
                            }
                        });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView =itemView;
        }

        public void setFullname(String fullname){
            TextView username = (TextView) mView.findViewById(R.id.post_profile_name);
            username.setText(fullname);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }


        public void setPost(String post)
        {
            TextView userPost = (TextView) mView.findViewById(R.id.user_post);
            userPost.setText(post);
        }
    }


    private void SendUserToPostActivity()
    {
        Intent postIntent = new Intent(MainActivity.this, ParaActivity.class);
        startActivity(postIntent);
    }


    @Override
        protected void onStart() {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser!=null){
                currentUserID = mAuth.getCurrentUser().getUid();
                CheckUserExistence();
                UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("fullname")) {
                                String fullname = dataSnapshot.child("fullname").getValue().toString();
                                NavProfileUserName.setText(fullname);
                            }
                            if (dataSnapshot.hasChild("ProfileImage")) {
                                String image = dataSnapshot.child("ProfileImage").getValue().toString();
                                Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                            } else {
                                Toast.makeText(MainActivity.this, "You don't have a profile pic.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }else{
                SendUserToLoginActivity();
            }
            super.onStart();
        }



    private void CheckUserExistence() {
            final String current_user_id = mAuth.getCurrentUser().getUid();

            UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(current_user_id)) {
                        SendUserToSetupActivity();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        private void SendUserToSetupActivity() {
            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(setupIntent);
            finish();
        }


        private void SendUserToLoginActivity() {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }


        private void UserMenuSelector(MenuItem item) {
            switch (item.getItemId()) {

                case  R.id.nav_post:
                    SendUserToPostActivity();
                    break;

                case R.id.nav_home:
                    SendUserToMainActivity();
                    break;


                case R.id.nav_logout:
                    mAuth.signOut();
                    SendUserToLoginActivity();
                    break;
            }
        }

    private void SendUserToSetup1Activity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(setupIntent);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

