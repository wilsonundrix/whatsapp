package com.wilsonundrix.mywhatsapp;

import android.app.ProgressDialog;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerButton, verifyButton;
    private EditText inputPhone, inputVerCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        InitializeFields();
        mAuth = FirebaseAuth.getInstance();

        sendVerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = inputPhone.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Phone Number is required", Toast.LENGTH_LONG).show();
                } else {

                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("please wait as we authenticate your phone");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number with country code", Toast.LENGTH_LONG).show();
                sendVerButton.setVisibility(View.VISIBLE);
                inputPhone.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                inputVerCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Verification Code sent successfully", Toast.LENGTH_LONG).show();
                sendVerButton.setVisibility(View.INVISIBLE);
                inputPhone.setVisibility(View.INVISIBLE);
                progressDialog.dismiss();
                verifyButton.setVisibility(View.VISIBLE);
                inputVerCode.setVisibility(View.VISIBLE);
            }
        };

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerButton.setVisibility(View.INVISIBLE);
                inputPhone.setVisibility(View.INVISIBLE);

                String verCode = inputVerCode.getText().toString().trim();
                if (TextUtils.isEmpty(verCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter Verification Code", Toast.LENGTH_LONG).show();
                } else {

                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("please wait as we verify your code");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

    }

    private void InitializeFields() {
        sendVerButton = findViewById(R.id.send_ver_code_button);
        verifyButton = findViewById(R.id.verify_button);
        inputVerCode = findViewById(R.id.verification_code_input);
        inputPhone = findViewById(R.id.phone_number_input);
        progressDialog = new ProgressDialog(this);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congrats...verified", Toast.LENGTH_LONG).show();
                            SenduserToMainActivity();
                        } else {
                            // Sign in failed, display a message and update the UI
                            progressDialog.dismiss();
                            String error = task.getException().getMessage();
                            Toast.makeText(PhoneLoginActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void SenduserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
