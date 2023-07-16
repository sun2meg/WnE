package com.android.sun2meg.earn;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InterstitialActivity extends AppCompatActivity
        implements MaxAdListener
{

    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;
    ScheduledExecutorService scheduler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        createInterstitialAd();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.i("hello", "world");
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(!interstitialAd.isReady()){
                            interstitialAd.loadAd();
                        }
              createInterstitialAd();

                    }
                });
            }
        }, 2, 10, TimeUnit.SECONDS);

    }







    void createInterstitialAd()
    {
        interstitialAd = new MaxInterstitialAd("0860082806f7006e", this );
//        interstitialAd = new MaxInterstitialAd("0860082806f7006e", this );
        interstitialAd.setListener( this );
        // Load the first ad
        interstitialAd.loadAd();



    }

    // MAX Ad Listener
    @Override
    public void onAdLoaded(final MaxAd maxAd)
    {
        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'
interstitialAd.showAd();
        // Reset retry attempt
        retryAttempt = 0;
    }

    @Override
    public void onAdLoadFailed(final String adUnitId, final MaxError error)
    {
        // Interstitial ad failed to load
        // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)

        retryAttempt++;
        long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                interstitialAd.loadAd();
            }
        }, delayMillis );
    }

    @Override
    public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error)
    {
        // Interstitial ad failed to display. AppLovin recommends that you load the next ad.
        interstitialAd.loadAd();
    }

    @Override
    public void onAdDisplayed(final MaxAd maxAd) {
//        Toast.makeText(getApplicationContext(), "Ad completed", Toast.LENGTH_SHORT).show();
//        finish();
        interstitialAd.loadAd();
    }

    @Override
    public void onAdClicked(final MaxAd maxAd) {}

    @Override
    public void onAdHidden(final MaxAd maxAd)
    {
        // Interstitial ad is hidden. Pre-load the next ad
        interstitialAd.loadAd();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}