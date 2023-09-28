package com.android.sun2meg.earn;
import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class EmailPasswordActivity extends BasicActivity {

//    private EditText mEdtEmail, mEdtPassword;
    private Button regBtn,login;
    private TextView forgotPassword;
//    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private EditText mEdtEmail, mEdtPassword;
    private ImageView mImageView;
    private TextView mTextViewProfile;

    private TextInputLayout mLayoutEmail, mLayoutPassword;
private EditText frgtPswd;
    private static final String TAG = "EmailPasswordActivity";


    private int usercoin = 0;
    FirebaseDatabase database;
    DatabaseReference myRef;
    public SharedPreferences coins;
    SharedPreferences.Editor coinsEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        coins = getSharedPreferences("Rewards", MODE_PRIVATE);
        database =  FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeUI();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                } else {
//                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                }
                updateUI(user);
            }
        };



        regBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                registerNewUser();

            }
        });


        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                signIn(mEdtEmail.getText().toString(), mEdtPassword.getText().toString());
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);

            }
        });

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


    private void registerNewUser() {

            if (!validateForm()) {
                return;
            }
            showProgressDialog();

              mAuth.createUserWithEmailAndPassword(mEdtEmail.getText().toString(), mEdtPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mTextViewProfile.setTextColor(Color.DKGRAY);
                                //////////////////////////////////////////////
                                FirebaseDatabase database =  FirebaseDatabase.getInstance();
                                mAuth = FirebaseAuth.getInstance();
                                FirebaseUser user1 =  mAuth.getCurrentUser();
                                String userId = user1.getUid();
                                usercoin = 0;
                                myRef =  database.getReference().child("Users").child(userId);
                                myRef.child("Users").child(user1.getUid()).child("Coins").setValue(usercoin);

                                coinsEdit = coins.edit();
                                coinsEdit.putString("Coins", String.valueOf(usercoin));
                                coinsEdit.apply();

                                //////////////////////////////////////////////////
                                updateUI(user1);
                            } else {
                                        mTextViewProfile.setTextColor(Color.RED);
                    mTextViewProfile.setText(task.getException().getMessage());
                            }
                            hideProgressDialog();
                        }
                    });
        }




    private void initializeUI() {

        mEdtEmail = findViewById(R.id.edt_email);
        mEdtPassword = findViewById(R.id.edt_password);

//        emailTV = findViewById(R.id.e);
//        passwordTV = findViewById(R.id.password);
        login = findViewById(R.id.email_sign_in_button);
        regBtn = findViewById(R.id.email_create_account_button);
//        progressBar = findViewById(R.id.progressBar);
        mImageView = findViewById(R.id.logo);
        mLayoutEmail = findViewById(R.id.layout_email);
        mLayoutPassword = findViewById(R.id.layout_password);
        mTextViewProfile = findViewById(R.id.profile);
        mEdtEmail = findViewById(R.id.edt_email);
        mEdtPassword = findViewById(R.id.edt_password);
        forgotPassword=findViewById(R.id.forget);

    }

    private void signIn(String email, String password) {
        DatabaseReference mDatabase = database.getReference();
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    mTextViewProfile.setTextColor(Color.RED);
                    mTextViewProfile.setText(task.getException().getMessage());
                } else {
                    mTextViewProfile.setTextColor(Color.DKGRAY);
//                    FirebaseUser user = mAuth.getCurrentUser();
                    ////////////////////////////////////////////
                    FirebaseUser user = mAuth.getCurrentUser();
//                    // Increment user's coin value by 10 on successful login
//                    mDatabase.child("Users").child(user.getUid()).child("Coins").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                            usercoins = Integer.parseInt(dataSnapshot.getValue(String.class));
//                            usercoin = dataSnapshot.getValue(Integer.class);
//
//                            mDatabase.child("Users").child(user.getUid()).child("Coins").setValue(usercoin);
//                            // Display user's coin value with Toast message
//                            Toast.makeText(getApplicationContext(), "Your coins: " + usercoin, Toast.LENGTH_SHORT).show();
//                            // Save user's coin value to SharedPreferences
//
//                    coinsEdit = coins.edit();
//                    coinsEdit.putString("Coins", String.valueOf(usercoin));
//                    coinsEdit.apply();
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//                                Toast.makeText(getApplicationContext(), String.valueOf(databaseError), Toast.LENGTH_SHORT).show();
//                                // Handle errors here
//                            }
//                        });
                     updateUI(user);
                }
                hideProgressDialog();
            }
        });
    }


    private boolean validateForm() {
        if (TextUtils.isEmpty(mEdtEmail.getText().toString())) {
            mLayoutEmail.setError("Required.");
            return false;
        } else if (TextUtils.isEmpty(mEdtPassword.getText().toString())) {
            mLayoutPassword.setError("Required.");
            return false;
        } else {
            mLayoutEmail.setError(null);
            mLayoutPassword.setError(null);
            return true;
        }
    }

//    private void updateUI(FirebaseUser userf) {
//        DatabaseReference mDatabase = database.getReference();
//        if (userf != null) {
//
//            FirebaseUser user = mAuth.getCurrentUser();
//
//            // Increment user's coin value by 10 on successful login
//            mDatabase.child("Users").child(user.getUid()).child("Coins").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    // Retrieve the user's coins value from the database
//                    if (dataSnapshot.exists()) {
////                        usercoin = dataSnapshot.getValue(Integer.class);
//                        usercoin = 1500;
//                    } else {
//                        // If the coins node does not exist in the database, create it with an initial value of 0
//                        usercoin = 0;
//                        mDatabase.child("Users").child(user.getUid()).child("Coins").setValue(usercoin);
//                    }
//
//                    // Save user's coin value to SharedPreferences
//                    coinsEdit = coins.edit();
//                    coinsEdit.putInt("Coins", usercoin);
//                    coinsEdit.apply();
//
//                    // Display user's coin value with Toast message on the main UI thread
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Your coins: " + usercoin, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    // Proceed to the next activity (ChoiceSelection or ExampleActivity) on the main UI thread
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
////                            startNextActivity();
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    // Handle errors here
//                }
//            });
//            startNextActivity();
//        }
//    }
//
//    private void startNextActivity() {
//        Bundle bundle = new Bundle();
//        Intent intent = new Intent(getApplicationContext(), ChoiceSelection.class);
//        intent.putExtras(bundle);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        finish();
//    }




//String.valueOf(usercoin)
    private void updateUI(FirebaseUser user) {
        DatabaseReference mDatabase = database.getReference();
        if (user != null) {


//
        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user1 =  mAuth.getCurrentUser();
            String userId = user.getUid();
//        String userId = user1.getUid();
//        String userId = user1.getEmail();
        myRef =  database.getReference().child("Users").child(userId);
          myRef.child("Coins").addListenerForSingleValueEvent(new ValueEventListener() {

//            FirebaseUser user = mAuth.getCurrentUser();
//            mDatabase =  database.getReference().child("Users").child(user.getUid());
//            // Increment user's coin value by 10 on successful login
//            mDatabase.child("Coins").addListenerForSingleValueEvent(new ValueEventListener() {
//            mDatabase.child("Users").child(user.getUid()).child("Coins").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        usercoin = Integer.parseInt(dataSnapshot.getValue(String.class));
//                        usercoin = dataSnapshot.getValue(Integer.class);
//                        Toast.makeText(getApplicationContext(), "Exists" + String.valueOf(usercoin), Toast.LENGTH_SHORT).show();

                    } else {
//                        Toast.makeText(getApplicationContext(), "Doesnt Exist: " + usercoin, Toast.LENGTH_SHORT).show();

                        // If the coins node does not exist in the database, create it with an initial value of 0
                        usercoin = 0;
//                        mDatabase.child("Users").child(user.getUid()).child("Coins").setValue(usercoin);
                    }
////                            usercoins = Integer.parseInt(dataSnapshot.getValue(String.class));
//                    usercoin = dataSnapshot.getValue(Integer.class);
//                    mDatabase.child("Users").child(user.getUid()).child("Coins").setValue(usercoin);
//                    // Display user's coin value with Toast message


//                    Toast.makeText(getApplicationContext(), "Your coins: " + usercoin, Toast.LENGTH_SHORT).show();

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
