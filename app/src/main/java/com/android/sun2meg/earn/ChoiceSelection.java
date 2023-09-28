package com.android.sun2meg.earn;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.VideoListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChoiceSelection extends AppCompatActivity implements RewardedVideoAdListener {

    private TextView coins2;
    private boolean connected;
    public SharedPreferences coins;
    private String currentCoins;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mRef;
    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd mInterstitialAd;

    private MaxInterstitialAd interstitialMaxAd;
    private int retryAttempt;
    StartAppAd start;
    private MaxRewardedAd rewardedMaxAd;
    ScheduledExecutorService scheduler;
    private Handler handlerRetryAd;
    String dbCoin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_selection);
        dbCoin =null;
        coins = getSharedPreferences("Rewards", MODE_PRIVATE);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        AppLovinSdk.getInstance( this ).setMediationProvider( "max" );
        AppLovinSdk.initializeSdk( this, new AppLovinSdk.SdkInitializationListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration)
            {
                // AppLovin SDK is initialized, start loading ads

                createInterstitialAd();

            }
        } );

        createRewardedAd();
        handlerRetryAd = new Handler();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                if (null != activeNetwork) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) { }
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) { }
                } else {

                    Intent intent = new Intent(ChoiceSelection.this, NoInternetActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();

                }
                handler.postDelayed(this, 2000);
            }
        };
        start = new StartAppAd(ChoiceSelection.this);
        mAuth = FirebaseAuth.getInstance();

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713"); // google

//        MobileAds.initialize(this, "ca-app-pub-4156752697881993~3026085465");  //my id
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");  // google
//        mInterstitialAd.setAdUnitId("ca-app-pub-4156752697881993/6186955975");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        ///////////////////////////////////////////////////////////////////////


        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }

        final Handler handler2 = new Handler();
        final int delay = 1000; //milliseconds
        handler2.postDelayed(new Runnable(){
            public void run(){
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    connected = true;
                }
                else
                    connected = false;
                    handler2.postDelayed(this, delay);
            }
        }, delay);



        ImageView settingbtn = (ImageView) findViewById(R.id.imageView9);
        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentActivity(v);
            }
        });



        coins2 = (TextView) findViewById(R.id.textViewCoins);
        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user =  mAuth.getCurrentUser();
//        Toast.makeText(ChoiceSelection.this, mAuth.getCurrentUser().toString(), Toast.LENGTH_SHORT).show();
        String userId = user.getUid();
        mRef =  database.getReference().child("Users").child(userId);

        currentCoins = coins.getString("Coins", "0");


        coins2.setText(currentCoins);

        mRef.child("Coins").setValue(currentCoins);
        coins2 = (TextView) findViewById(R.id.textViewCoins);

        CardView cardsmallads = findViewById(R.id.smallads);
        cardsmallads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//////////////////////////////////////////////////////////////////////////////////////////
//                Intent intent = new Intent(ChoiceSelection.this, BannerActivity.class);
//                startActivity(intent);

                Toast.makeText(ChoiceSelection.this, "please wait", Toast.LENGTH_SHORT).show();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    Toast.makeText(ChoiceSelection.this, "Admob", Toast.LENGTH_SHORT).show();
                }
                else {
//                    Toast.makeText(ChoiceSelection.this, "The Ads wasn't loaded yet. Switching Ad channel", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
//                    createInterstitialAd();
//                    interstitialAd.loadAd();


                    /////////////
//                    if(!interstitialMaxAd.isReady()){
//                        interstitialMaxAd.loadAd();}
//                    showInterstitialAd();
//                    interstitialAd.loadAd();
                }




                }

        });






        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                int coinCount = Integer.parseInt(coins.getString("Coins", "0"));
                coinCount = coinCount + 20;
                SharedPreferences.Editor coinsEdit = coins.edit();
                coinsEdit.putString("Coins", String.valueOf(coinCount));
                coinsEdit.apply();
                coins2.setText(String.valueOf(coinCount));
            }
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        final Handler handler1 = new Handler();
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                int coinCount = Integer.parseInt(coins.getString("Coins", "0"));
                coins2.setText(String.valueOf(coinCount));
                Log.d("Handlers", "Called on main thread");
                handler1.postDelayed(this, 2000);
            }
        };
        handler.post(runnableCode);
    } // end onCreate

    void createInterstitialAd()
    {
        interstitialMaxAd = new MaxInterstitialAd("0860082806f7006e", this );
        interstitialMaxAd.setListener( applovindListener );

        // Load the first ad
        interstitialMaxAd.loadAd();
    }



    void createRewardedAd()
    {
        rewardedMaxAd = MaxRewardedAd.getInstance( "a1e99c49e704d3cb", this );
        rewardedMaxAd.setListener( applovindReListener );

//        rewardedAd.loadAd();
    }

    public void execInter(){
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.i("hello", "world");
                runOnUiThread(new Runnable() {
                    public void run() {
                        interstitialMaxAd.loadAd();

                        createInterstitialAd();

                    }
                });
            }
        }, 10, 10, TimeUnit.SECONDS);
    }




    public void presentActivity(View view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "transition");
        int revealX = (int) (view.getX() + view.getWidth() / 2);
        int revealY = (int) (view.getY() + view.getHeight() / 2);
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(SettingsActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }
    public void startVideo(View view) {
        if(mRewardedVideoAd.isLoaded()){
            mRewardedVideoAd.show();
        }  else {
//            Toast.makeText(ChoiceSelection.this, "The Video wasn't loaded yet.Switching Ad Channel", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "The mRewardedVideoAd wasn't loaded yet.");
//            createRewardedAd();
//            rewardedMaxAd.loadAd();
//            showRewardAd();
        }
//////////////////////////////////////////////////////////////////////////////////////////////
//        showRewardedVideo(view);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        if(rewardedAd.isReady()){
//          rewardedAd.showAd();
//        }  else {
//            Toast.makeText(ChoiceSelection.this, "The applvn Video wasn't loaded yet. Try again later", Toast.LENGTH_SHORT).show();
//            Log.d("TAG", "The mRewardedVideoAd wasn't loaded yet.");}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    public void showRewardedVideo(View view) {
        final StartAppAd rewardedVideo = new StartAppAd(this);

        rewardedVideo.setVideoListener(new VideoListener() {
            @Override
            public void onVideoCompleted() {

                int coinCount = Integer.parseInt(coins.getString("Coins", "0"));
                coinCount = coinCount + 50;
                SharedPreferences.Editor coinsEdit = coins.edit();
                coinsEdit.putString("Coins", String.valueOf(coinCount));
                coinsEdit.apply();
                coins2.setText(String.valueOf(coinCount));

                Toast.makeText(getApplicationContext(), "50 coin reward received", Toast.LENGTH_SHORT).show();
            }
        });

        rewardedVideo.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, new AdEventListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                rewardedVideo.showAd();
            }

            @Override
            public void onFailedToReceiveAd(Ad ad) {
                Toast.makeText(getApplicationContext(), "Can't show rewarded video", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void instruction(View view) {
        Intent openInstructions = new Intent(getApplicationContext(), Instructions.class);
        startActivity(openInstructions);
    }
    public void redeem(View view) {
        Intent openRedeem = new Intent(getApplicationContext(), Redeem.class);
        startActivity(openRedeem);
    }
    public void aboutus(View view) {
        String url = "https://www.codester.com/micodes?ref=micodes";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    public void contact(View view) {
        Intent contact = new Intent(Intent.ACTION_SENDTO);
        contact.setData(Uri.parse("mailto:sun2meg@gmail.com"));
        startActivity(contact);
    }
    public void dailyCheck(View view) {
        Intent openDailyChecks = new Intent(getApplicationContext(), DailyCheckins.class);
        startActivity(openDailyChecks);
    }
    public void luckyWheel(View view) {
        Intent openLuckyWheel = new Intent(getApplicationContext(), LuckyWheel.class);
        startActivity(openLuckyWheel);
        //////////////////////////////////////////////////////////////////////////////
//        applvinterAd.showAd();
//        StartAppAd.showAd(this);
    }
    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
    @Override
    public void onRewarded(RewardItem reward) {
        int coinCount = Integer.parseInt(coins.getString("Coins", "0"));
        coinCount = coinCount + 50;
        SharedPreferences.Editor coinsEdit = coins.edit();
        coinsEdit.putString("Coins", String.valueOf(coinCount));
        coinsEdit.apply();
        coins2.setText(String.valueOf(coinCount));
        Toast.makeText(ChoiceSelection.this, "admob on reward 50 coins received", Toast.LENGTH_SHORT).show();
    }
    private void loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded()){
            mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build()); //google
//            mRewardedVideoAd.loadAd("ca-app-pub-4156752697881993/4626888296", new AdRequest.Builder().build());
        }
    }


    private void loadApplvnRewardedVideoAd() {
        if (!rewardedMaxAd.isReady()){
            rewardedMaxAd.loadAd();
//            createApplovinRewardAd();
        }

    }


    @Override
    public void onRewardedVideoAdLeftApplication() {}
    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }
    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {}
    @Override
    public void onRewardedVideoAdLoaded() {}
    @Override
    public void onRewardedVideoAdOpened() {}
    @Override
    public void onRewardedVideoStarted() {}
    @Override
    public void onRewardedVideoCompleted() {

        int coinCount = Integer.parseInt(coins.getString("Coins", "0"));
        coinCount = coinCount + 50;
        SharedPreferences.Editor coinsEdit = coins.edit();
        coinsEdit.putString("Coins", String.valueOf(coinCount));
        coinsEdit.apply();
        coins2.setText(String.valueOf(coinCount));

        Toast.makeText(ChoiceSelection.this, "admob on Comp50 coins received", Toast.LENGTH_SHORT).show();
         loadRewardedVideoAd();
    }
    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }
    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }
    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }

    MaxRewardedAdListener applovindReListener= new MaxRewardedAdListener() {



        // MAX Ad Listener
        @Override
        public void onAdLoaded(final MaxAd maxAd)
        {
            // Rewarded ad is ready to be shown. rewardedAd.isReady() will now return 'true'
//            rewardedAd.showAd();
            // Reset retry attempt
            retryAttempt = 0;
        }

        @Override
        public void onAdLoadFailed(final String adUnitId, final MaxError error)
        {
            // Rewarded ad failed to load
            // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

            retryAttempt++;
            long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    rewardedMaxAd.loadAd();
                }
            }, delayMillis );
        }

        @Override
        public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error)
        {
            // Rewarded ad failed to display. We recommend loading the next ad
            rewardedMaxAd.loadAd();
        }

        @Override
        public void onAdDisplayed(final MaxAd maxAd) {
            rewardedMaxAd.loadAd();
        }

        @Override
        public void onAdClicked(final MaxAd maxAd) {}

        @Override
        public void onAdHidden(final MaxAd maxAd)
        {
            // rewarded ad is hidden. Pre-load the next ad
            rewardedMaxAd.loadAd();
        }

        @Override
        public void onRewardedVideoStarted(final MaxAd maxAd) {
            Toast.makeText(ChoiceSelection.this, "watch to end for coins", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRewardedVideoCompleted(final MaxAd maxAd) {

            int coinCount = Integer.parseInt(coins.getString("Coins", "0"));
            coinCount = coinCount + 50;
            SharedPreferences.Editor coinsEdit = coins.edit();
            coinsEdit.putString("Coins", String.valueOf(coinCount));
            coinsEdit.apply();
            coins2.setText(String.valueOf(coinCount));

            Toast.makeText(ChoiceSelection.this, "50 coins received", Toast.LENGTH_SHORT).show();
            rewardedMaxAd.loadAd();
            rewardedMaxAd.destroy();

        }


        @Override
        public void onUserRewarded(final MaxAd maxAd, final MaxReward maxReward)
        {
            // Rewarded ad was displayed and user should receive the reward
        }


    };

    MaxAdListener applovindListener= new MaxAdListener() {
        // MAX Ad Listener
        @Override
        public void onAdLoaded(final MaxAd maxAd)
        {
            // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'
//            interstitialAd.showAd();
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
                    interstitialMaxAd.loadAd();
                }
            }, delayMillis );
        }

        @Override
        public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error)
        {
            Toast.makeText(ChoiceSelection.this, "The Ads display failed.", Toast.LENGTH_SHORT).show();
            // Interstitial ad failed to display. AppLovin recommends that you load the next ad.
            interstitialMaxAd.loadAd();
        }



        @Override
        public void onAdHidden(final MaxAd maxAd)
        {
            // Interstitial ad is hidden. Pre-load the next ad
            interstitialMaxAd.loadAd();

        }

        @Override
        public void onAdDisplayed(final MaxAd maxAd) {

            int coinCount = Integer.parseInt(coins.getString("Coins", "0"));
            coinCount = coinCount + 20;
            SharedPreferences.Editor coinsEdit = coins.edit();
            coinsEdit.putString("Coins", String.valueOf(coinCount));
            coinsEdit.apply();
            coins2.setText(String.valueOf(coinCount));
            Toast.makeText(ChoiceSelection.this, "20 coins received", Toast.LENGTH_SHORT).show();
//            finish();
//            interstitialAd.destroy();
//            Intent setIntent = new Intent(Intent.ACTION_MAIN);
//            setIntent.addCategory(Intent.CATEGORY_HOME);
//            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(setIntent);

            interstitialMaxAd.destroy();
            destroy();

        }

        @Override
        public void onAdClicked(final MaxAd maxAd) {}



    };

        public void destroy() {
            handlerRetryAd.removeCallbacksAndMessages(null);
            interstitialMaxAd.destroy();

        }
    public void showInterstitialAd() {
        if (interstitialMaxAd.isReady()) {
            interstitialMaxAd.showAd();
        } else
            Toast.makeText(ChoiceSelection.this, "Ad not ready", Toast.LENGTH_SHORT).show();
    }

    public void showRewardAd() {
        if (rewardedMaxAd.isReady()) {
            rewardedMaxAd.showAd();
        } else
            Toast.makeText(ChoiceSelection.this, "Ad not ready", Toast.LENGTH_SHORT).show();
    }


}
