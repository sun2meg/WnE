package com.android.sun2meg.earn;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private int usercoin = 0;
    FirebaseDatabase database;
    DatabaseReference myRef;
    public SharedPreferences coins;
    SharedPreferences.Editor coinsEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmail);

        Button forgotPasswordButton = findViewById(R.id.buttonForgotPassword);
        coins = getSharedPreferences("Rewards", MODE_PRIVATE);
        database =  FirebaseDatabase.getInstance();



        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                updateUI(user);
            }
        };


        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();

                if (!validateForm()) {
                    return;
                }

                // Send a password reset email
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    // Password reset email sent successfully
                                    Toast.makeText(LoginActivity.this, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Failed to send password reset email
                                    Toast.makeText(LoginActivity.this, "Failed to Send Password Reset Email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }


    private boolean validateForm() {
        if (TextUtils.isEmpty(editTextEmail.getText().toString())) {
            editTextEmail.setError("Required.");
            return false;
        } else {
            editTextEmail.setError(null);
            return true;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            updateUI(user);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    private void updateUI(FirebaseUser user) {
        DatabaseReference mDatabase = database.getReference();
        if (user != null) {


//
            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user1 =  mAuth.getCurrentUser();
            String userId = user1.getUid();
//        String userId = user1.getEmail();
            myRef =  database.getReference().child("Users").child(userId);
            myRef.child("Coins").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        usercoin = Integer.parseInt(dataSnapshot.getValue(String.class));
//                        usercoin = dataSnapshot.getValue(Integer.class);
                        Toast.makeText(getApplicationContext(), "Exists" + String.valueOf(usercoin), Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Doesnt Exist: " + usercoin, Toast.LENGTH_SHORT).show();

                        // If the coins node does not exist in the database, create it with an initial value of 0
                        usercoin = 0;
                    }
                    Toast.makeText(getApplicationContext(), "Your coins: " + usercoin, Toast.LENGTH_SHORT).show();
                    // Save user's coin value to SharedPreferences

                    coinsEdit = coins.edit();
                    coinsEdit.putString("Coins", String.valueOf(usercoin));
                    coinsEdit.apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors hereString.valueOf(usercoin)
                    Toast.makeText(getApplicationContext(), String.valueOf(databaseError), Toast.LENGTH_SHORT).show();
                }
            });


////////////////////////////////////////////////////////////////////////////////
            Bundle bundle = new Bundle();
            Intent intent = new Intent(getApplicationContext(), ChoiceSelection.class);
//            Intent intent = new Intent(getApplicationContext(), ExampleActivity.class);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }


}

