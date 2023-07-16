package com.android.sun2meg.earn;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import java.util.concurrent.TimeUnit;

public class BannerActivity extends AppCompatActivity implements MaxAdListener {

    private MaxInterstitialAd interstitialAd;
    private Handler handlerRetryAd;
    private int retryAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        initializeAdNetwork(); // initialize ads only once during the app startup
        createInterstitialAd();

        handlerRetryAd = new Handler();

        // Load your first ad
        loadInterstitialAd();
        showInterstitialAd();
    }

    private void initializeAdNetwork() {
        AppLovinSdk.getInstance(getApplicationContext()).setMediationProvider("max");
        AppLovinSdk.initializeSdk(getApplicationContext(), new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
            }
        });
    }

    void createInterstitialAd() {
        interstitialAd = new MaxInterstitialAd("0860082806f7006e", this);
        interstitialAd.setListener(this);
    }

    // Call this method whenever you want to load a new ad
    private void loadInterstitialAd() {
        interstitialAd.loadAd();

    }

    // Call this method to show the ad
    private void showInterstitialAd() {
        if (interstitialAd.isReady()) {
            interstitialAd.showAd();
        } else
            Toast.makeText(BannerActivity.this, "Not loaded", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        handlerRetryAd.removeCallbacksAndMessages(null);
        interstitialAd.destroy();
        super.onDestroy();
    }

    @Override
    public void onAdLoaded(MaxAd ad) {
        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'
        // Reset retry attempt
        retryAttempt = 0;
    }

    @Override
    public void onAdDisplayed(MaxAd ad) {
        // It is called when the ad is shown to the user
        Toast.makeText(BannerActivity.this, "Ad displayed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdHidden(MaxAd ad) {
        // User closed the ad. Pre-load the next ad
        loadInterstitialAd();
    }

    @Override
    public void onAdClicked(MaxAd ad) {
        // User clicked on the ad
    }

    @Override
    public void onAdLoadFailed(String adUnitId, MaxError error) {
        // Interstitial ad failed to load
        // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)

        retryAttempt++;
        long delayMillis =
                TimeUnit.SECONDS.toMillis((long) Math.pow(2.0, Math.min(6.0, retryAttempt)));

        Runnable runnableAd = new Runnable() {
            @Override
            public void run() {
                loadInterstitialAd();
            }
        };

        handlerRetryAd.postDelayed(runnableAd, delayMillis);
    }

    @Override
    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
        // Interstitial ad failed to display. AppLovin recommends that you load the next ad.
        loadInterstitialAd();
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}