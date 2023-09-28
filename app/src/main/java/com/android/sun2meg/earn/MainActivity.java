package com.android.sun2meg.earn;

import androidx.appcompat.app.AppCompatActivity;


//import com.applovin.enterprise.apps.demoapp.R;
//import com.applovin.enterprise.apps.demoapp.ui.BaseAdActivity;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// MainActivity.java
public class MainActivity extends AppCompatActivity {
    private TextView tvCoins;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCoins = findViewById(R.id.tvCoins);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Retrieve coin value from SharedPreferences
        int coins = sharedPreferences.getInt("coins", 0);
        tvCoins.setText("Coins: " + coins);
    }
}