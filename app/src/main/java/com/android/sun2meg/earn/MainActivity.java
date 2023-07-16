package com.android.sun2meg.earn;

import androidx.appcompat.app.AppCompatActivity;


//import com.applovin.enterprise.apps.demoapp.R;
//import com.applovin.enterprise.apps.demoapp.ui.BaseAdActivity;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button loginButton,rewardButton,bannerButton;

    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton=findViewById(R.id.interButton);
        interstitialAd= new MaxInterstitialAd("b1fe34717056ac5b", this );
        rewardButton=findViewById(R.id.rewardButton);
        bannerButton=findViewById(R.id.BannerButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, InterstitialActivity.class));
                finish();

            }
        });

        rewardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this,RewardActivity.class));
                finish();

            }
        });


        bannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this,BannerActivity.class));
                finish();

            }
        });

        AppLovinSdk.getInstance( getApplicationContext()).setMediationProvider( "max" );
        AppLovinSdk.initializeSdk( getApplicationContext(), new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(AppLovinSdkConfiguration config) {
//interstitialAd.loadAd();
//interstitialAd.showAd();

            }
//            @Override
//            public void onSdkInitialized(final AppLovinSdkConfiguration configuration)
//            {
//                // AppLovin SDK is initialized, start loading ads
//            }
//        } );
        });

    }
}