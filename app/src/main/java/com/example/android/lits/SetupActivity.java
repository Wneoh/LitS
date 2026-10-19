package com.example.android.lits;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Arrays;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends BaseActivity {

    private EditText Username, Fullname;
    private Button saveInformationbutton;
    private Button Gender, Countryname;
    private CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;
    String currentUserID;
    String[] Countries;
    String[] Genders;
    String Fgender,Fcountry;
    private ProgressDialog loadingBar;
    private final android.os.Handler saveHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean savePending;
    private final Runnable saveTimeout = () -> {
        if (!isFinishing() && !isDestroyed() && savePending) {
            loadingBar.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("Profile has not saved yet")
                    .setMessage("The database is not responding. Check your connection. If this continues, the app's Firebase database may be unavailable. Your save is still pending; the dashboard will open when it is confirmed.")
                    .setPositiveButton("OK", null).show();
        }
    };
    final static int Gallery_Pick =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            SendUserToLoginActivity();
            return;
        }
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID); // get the user's unique id
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        Username = findViewById(R.id.setup_username);
        Fullname = findViewById(R.id.setup_full_name);

        saveInformationbutton = findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView)findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        Gender = findViewById(R.id.setup_gender);
        Genders = getResources().getStringArray(R.array.gender);

        Countryname = findViewById(R.id.setup_country_name);
        Countries = getResources().getStringArray(R.array.countries);
        if (savedInstanceState != null) {
            Fcountry = savedInstanceState.getString("country");
            Fgender = savedInstanceState.getString("gender");
            if (Fcountry != null) Countryname.setText(Fcountry);
            if (Fgender != null) Gender.setText(Fgender);
        }



        Countryname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SetupActivity.this);
                mBuilder.setTitle("Choose your country");
                mBuilder.setSingleChoiceItems(Countries, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Countryname.setText(Countries[i]);
                        Fcountry= Countries[i];
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });

        Gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SetupActivity.this);
                mBuilder.setTitle("Choose your gender");
                mBuilder.setSingleChoiceItems(Genders, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Gender.setText(Genders[i]);
                        Fgender= Genders[i];
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();

            }
        });
        saveInformationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });


        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("ProfileImage")) {
                        String image = dataSnapshot.child("ProfileImage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    } else {
                        Picasso.with(SetupActivity.this).cancelRequest(ProfileImage);
                        ProfileImage.setImageResource(R.drawable.profile);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this, ProfileCropActivity.class);
        }

        if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are storing your profile...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID +".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SetupActivity.this, "Image store sucessfully into Firebase.", Toast.LENGTH_SHORT).show();
                            final String downloadUri = task.getResult().getDownloadUrl().toString();

                            UserRef.child("ProfileImage").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Picasso.with(SetupActivity.this).load(downloadUri).into(ProfileImage);
                                        Toast.makeText(SetupActivity.this, "Store sucessuly into database", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "" + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }
                        else {
                            loadingBar.dismiss();
                            Toast.makeText(SetupActivity.this, "Photo upload failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                Toast.makeText(this, "Error:Image can't be crop.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }



    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(SetupActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    /**
     * Use FirebaseDatabase to store the information about the user and toast simple messages if error occurs
     */
    private void SaveAccountInformation()
    {
        if (savePending) {
            Toast.makeText(this, "Your profile save is still waiting for the database.", Toast.LENGTH_LONG).show();
            return;
        }
        String username =Username.getText().toString().trim();
        String fullname =Fullname.getText().toString().trim();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please write your username.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please write your fullname.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Fcountry==null){
            Toast.makeText(this, "Please select your country.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Fgender==null){
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
            return;
        }else{
            savePending = true;
            saveHandler.postDelayed(saveTimeout, 15000);
            loadingBar.setTitle("Setting information");
            loadingBar.setMessage("Please wait, while we are storing your info...");
            loadingBar.show();
            loadingBar.setCancelable(false);

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", Fcountry);
            userMap.put("status", " Hey there I am using Lits!");
            userMap.put("gender",Fgender);
            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    savePending = false;
                    saveHandler.removeCallbacks(saveTimeout);
                    if (isFinishing() || isDestroyed()) return;
                    loadingBar.dismiss();
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created sucessfully", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        new AlertDialog.Builder(SetupActivity.this).setTitle("Couldn't save profile")
                                .setMessage(message).setPositiveButton("OK", null).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putString("country", Fcountry);
        state.putString("gender", Fgender);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onDestroy() {
        saveHandler.removeCallbacks(saveTimeout);
        if (loadingBar != null) loadingBar.dismiss();
        super.onDestroy();
    }


    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
