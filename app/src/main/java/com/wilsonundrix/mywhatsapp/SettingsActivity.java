package com.wilsonundrix.mywhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        RetrieveUserInfo();

        updateAccSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

    }

    private void UpdateSettings() {
        String user_name = userName.getText().toString().trim();
        String user_status = userStatus.getText().toString().trim();

        if (TextUtils.isEmpty(user_name)) {
            userName.setError("Enter a Username");
        }
        if (TextUtils.isEmpty(user_status)) {
            userStatus.setError("Enter your Status...");
        } else {
            HashMap<String, String> profile_map = new HashMap<>();
            profile_map.put("user_ID", currentUserId);
            profile_map.put("user_name", user_name);
            profile_map.put("user_status", user_status);
            rootRef.child("Users").child(currentUserId).setValue(profile_map)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Profile Update FAILED: "
                                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void InitializeFields() {
        updateAccSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_prof_image);
    }

    private void RetrieveUserInfo() {
        rootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) &&
                                (dataSnapshot.hasChild("user_name") &&
                                        (dataSnapshot.hasChild("user_image")))) {
                            String user__name = dataSnapshot.child("user_name").getValue().toString();
                            String user__status = dataSnapshot.child("user_status").getValue().toString();
                            String user__image = dataSnapshot.child("user_image").getValue().toString();
                            userName.setText(user__name);
                            userStatus.setText(user__status);

                        } else if ((dataSnapshot.exists()) &&
                                (dataSnapshot.hasChild("user_name"))) {
                            String user__name = dataSnapshot.child("user_name").getValue().toString();
                            String user__status = dataSnapshot.child("user_status").getValue().toString();
                            userName.setText(user__name);
                            userStatus.setText(user__status);

                        } else {
                            Toast.makeText(SettingsActivity.this, "Please Update your profile Info", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
