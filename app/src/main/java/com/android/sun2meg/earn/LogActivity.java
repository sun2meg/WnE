package com.android.sun2meg.earn;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LogActivity extends AppCompatActivity {

    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private Button mLoginButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    int coins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        mEmailField = findViewById(R.id.textInputEditText_email);
        mPasswordField = findViewById(R.id.textInputEditText_password);
        mLoginButton = findViewById(R.id.button_login);

        mAuth = FirebaseAuth.getInstance();

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Password is required.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();

                            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                            if (user != null) {
                                Toast.makeText(LogActivity.this, "Coins found for user", Toast.LENGTH_SHORT).show();
                                mUserRef.child("Coins").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            coins = dataSnapshot.child("Coins").getValue(Integer.class);
//                                            coins = dataSnapshot.getValue(Integer.class);
                                            Toast.makeText(LogActivity.this, "Coins: " + String.valueOf(coins), Toast.LENGTH_SHORT).show();
                                        } else {
                                            mUserRef.child("Coins").setValue(coins);
                                            Toast.makeText(LogActivity.this, "Coins not found", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.w("LogActivity", "loadCoins:onCancelled", databaseError.toException());
                                    }
                                });
                            }
                            Toast.makeText(getApplicationContext(), "Coins: " + String.valueOf(coins), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LogActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
