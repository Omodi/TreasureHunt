package com.rit.se.treasurehuntvuz;

import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.*;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;

// Jeffrey Haines 3/4/17
//    (Old) Made FindTreasureActivity a single instance so we can kill it when player finds all the treasure etc.
//        http://stackoverflow.com/questions/10379134/finish-an-activity-from-another-activity
// Jeffrey Haines 4/22/17
//    Implemented FindTreasureActivity using a thread + handler message system
public class FindTreasureActivity extends AppCompatActivity {
    // configuration
    private static final long GPS_UPDATE_TIME = 5;
    private static final float GPS_UPDATE_DISTANCE = 0;
    private static final long FIND_TREASURE_THREAD_SLEEP = 1000;
    private static final float TREASURE_FOUND_DISTANCE = 15.0f;

    private TextView playerBearingHintsTextView;
    private ImageView playerDistanceHintsImageView;
    private int currentAnimationImageResource;
    private AnimationDrawable playerDistanceHintsAnimation;
    private Handler handler;
    private FindTreasureRunnable findTreasureRunnable;
    private Thread findTreasureThread;
    private LocationManager manager;
    private LocationListener listener;

    private class FindTreasureRunnable implements Runnable {
        private final Object mPauseLock;
        private boolean mPaused;
        private boolean mFinished;
        private Location mPlayerLocation;

        FindTreasureRunnable() {
            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;
            mPlayerLocation = null;
        }

        public void run() {
            int tickCount = 0;

            // find treasure loop
            while (!mFinished) {
                Log.v("FindTreasureActivity", String.format("findTreasureThread tick: %d", tickCount));

                if(mPlayerLocation != null) {
                    // get the closest treasure
                    Treasures.Treasure closestTreasure = null;
                    float closestDistance = Float.MAX_VALUE;
                    for (Treasures.Treasure treasure : TreasuresSingleton.getTreasures().getTreasureList()) {
                        if (!treasure.getFound()) {
                            float nextClosestDistance = mPlayerLocation.distanceTo(treasure.getLocation());
                            if (nextClosestDistance < closestDistance) {
                                closestTreasure = treasure;
                                closestDistance = nextClosestDistance;
                            }
                        }
                    }

                    // check found treasure criteria
                    if (closestTreasure != null) {
                        Log.v("FindTreasureActivity", String.format("Locked on Treasure: %f %f",
                                closestTreasure.getLocation().getLongitude(),
                                closestTreasure.getLocation().getLatitude()));

                        // treasure is found
                        if (closestDistance < TREASURE_FOUND_DISTANCE) {
                            Log.d("FindTreasureActivity", "Found nearby treasure");
                            TreasuresSingleton.getTreasures().foundTreasure(closestTreasure.getLocation());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateCoins();
                                    pickUpTreasure();
                                }
                            });
                            mFinished = true;
                        }
                        // update player hints
                        else {
                            final float closetBearingFinal = Math.abs(mPlayerLocation.bearingTo(
                                    closestTreasure.getLocation()) - mPlayerLocation.getBearing());
                            final float closestDistanceFinal = closestDistance;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updatePlayerHints(closestDistanceFinal, closetBearingFinal);
                                }
                            });
                        }
                    }
                }
                // not receiving user location
                else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updatePlayerHints(-1.0f, -1.0f);
                        }
                    });
                }

                try {
                    Thread.sleep(FIND_TREASURE_THREAD_SLEEP);
                } catch (InterruptedException e) {
                }

                // block thread when in pause state
                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                tickCount++;
            }
        }

        void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }

        void onFinish() {
            // signal findTreasureThread to terminate
            mFinished = true;
        }

        void onLocationChanged(Location newLocation) {
            mPlayerLocation = newLocation;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findtreasure);

        // setup player hints image and text view
        playerBearingHintsTextView = (TextView) findViewById(R.id.player_bearing_hints_text_view);
        playerDistanceHintsImageView = (ImageView) findViewById(R.id.player_distant_hints_image_view);
        currentAnimationImageResource = R.drawable.anim_gps;
        playerDistanceHintsImageView.setBackgroundResource(currentAnimationImageResource);
        playerDistanceHintsAnimation = (AnimationDrawable) playerDistanceHintsImageView.getBackground();

        handler = new Handler(getApplicationContext().getMainLooper());

        // setup findTreasureThread
        findTreasureRunnable = new FindTreasureRunnable();
        findTreasureThread = new Thread()
        {
            @Override
            public void run() {
                findTreasureRunnable.run();
            }
        };

        // setup location manager and listener
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location != null) {
                    Log.v("FindTreasureActivity", String.format("Location update: %f, %f", location.getLongitude(), location.getLatitude()));
                    // pass location to findTreasureThread
                    findTreasureRunnable.onLocationChanged(new Location(location));
                }
                else {
                    Log.d("FindTreasureActivity", "Null location");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.v("FindTreasureActivity", String.format("Location provider status changed, %s id:%d", provider, status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.v("FindTreasureActivity", String.format("Location provider enabled, %s", provider));
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.v("FindTreasureActivity", String.format("Lost location provider, %s", provider));
                findTreasureRunnable.onLocationChanged(null);
            }
        };

        // request location permissions
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            Log.d("FindTreasureActivity", "Location permission denied");
            if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("FindTreasureActivity", "Gps enabled");
            }
            else {
                Log.d("FindTreasureActivity", "Gps disabled");
            }

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("FindTreasureActivity", "Should show explanation");
            }
        }
    }

    @Override
    protected void onStart() {
        // TODO: Fix application crash when device sleeps. Thread.start() already called.
        Log.v("FindTreasureActivity", "Starting FindTreasureActivity");
        findTreasureThread.start();
        playerDistanceHintsAnimation.start();
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.v("FindTreasureActivity", "Resuming FindTreasureActivity");
        updateCoins();
        findTreasureRunnable.onResume();
        startLocationUpdates();
        playerDistanceHintsAnimation.setVisible(true, false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.v("FindTreasureActivity", "Pausing FindTreasureActivity");
        manager.removeUpdates(listener);
        findTreasureRunnable.onPause();
        playerDistanceHintsAnimation.stop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v("FindTreasureActivity", "Stopping FindTreasureActivity");
        manager.removeUpdates(listener);
        findTreasureRunnable.onFinish();
        playerDistanceHintsAnimation.stop();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.v("FindTreasureActivity", "Restarting FindTreasureActivity");
        manager.removeUpdates(listener);
        findTreasureRunnable.onFinish();
        playerDistanceHintsAnimation.start();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.v("FindTreasureActivity", "Destroying FindTreasureActivity");
        manager.removeUpdates(listener);
        findTreasureRunnable.onFinish();
        playerDistanceHintsAnimation.stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        try {
            Intent saveGameActivityIntent = new Intent(this, SaveGameActivity.class);
            startActivity(saveGameActivityIntent);
            findTreasureRunnable.onFinish();
            finish();
            Log.d("FindTreasureActivity", "Going to SaveGameActivity");
        }
        catch(Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("FindTreasureActivity", exception.getMessage());
            } else {
                Log.e("FindTreasureActivity", "Exception without a message.");
            }
        }
    }

    private boolean startLocationUpdates() {
        try {
            // start location updates
            Log.d("FindTreasureActivity", "Requesting location updates");
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_TIME, GPS_UPDATE_DISTANCE, listener);
            return true;
        } catch (SecurityException exception) {
            if(exception.getMessage() != null) {
                Log.e("FindTreasureActivity", exception.getMessage());
            }
            else {
                Log.e("FindTreasureActivity", "Exception without a message.");
            }
        }
        return false;
    }

    private void updateCoins() {
        String playerCoinsString = String.format(getString(R.string.player_coins_string),
                TreasuresSingleton.getTreasures().getNumCoins());
        TextView playerCoinsTextView = (TextView) findViewById(R.id.player_coin_text_view);
        playerCoinsTextView.setText(playerCoinsString);
        playerCoinsTextView.setTextSize(26);
    }

    private void updatePlayerHints(float distanceToTreasure, float bearingToTreasure) {
        Log.v("FindTreasureActivity", String.format("Distance: %f Bearing: %f", distanceToTreasure, bearingToTreasure));

        int nextAnimationImageResource;
        String bearingHint = "and getting ";

        if(distanceToTreasure != -1.0f && bearingToTreasure != -1.0f) {
            // set the distance hint image
            if (distanceToTreasure < 30) {
                nextAnimationImageResource = R.drawable.anim_blazing;
            } else if (distanceToTreasure < 60) {
                nextAnimationImageResource = R.drawable.anim_hot;
            } else if (distanceToTreasure < 120) {
                nextAnimationImageResource = R.drawable.anim_cold;
            } else {
                nextAnimationImageResource = R.drawable.anim_freezing;
            }

            // set the bearing hint string
            if (bearingToTreasure < 15) {
                bearingHint += "blazinger";
            } else if (bearingToTreasure < 30) {
                bearingHint += "hoter";
            } else if (bearingToTreasure < 60) {
                bearingHint += "colder";
            } else {
                bearingHint += "frezzinger";
            }
        }
        else {
            nextAnimationImageResource = R.drawable.anim_gps;
            bearingHint = "Looking for GPS...";
        }

        if(nextAnimationImageResource != currentAnimationImageResource) {
            currentAnimationImageResource = nextAnimationImageResource;
            playerDistanceHintsImageView.setBackgroundResource(currentAnimationImageResource);
            playerDistanceHintsAnimation = (AnimationDrawable) playerDistanceHintsImageView.getBackground();
            playerDistanceHintsAnimation.start(); // animation resource changed, restart animation.
        }
        playerBearingHintsTextView.setText(bearingHint);
    }

    private boolean pickUpTreasure() {
        try {
            Intent showTreasureIntent = new Intent(FindTreasureActivity.this, ShowTreasureActivity.class);

            showTreasureIntent.putExtra("NUM_COLLECTED", TreasuresSingleton.getTreasures().getNumCollected());
            showTreasureIntent.putExtra("NUM_TOTAL", TreasuresSingleton.getTreasures().getNumTotal());

            startActivity(showTreasureIntent);
            findTreasureRunnable.onFinish();
            finish();
            Log.d("FindTreasureActivity", "Going to ShowTreasureActivity");
        }
        catch(Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("FindTreasureActivity", exception.getMessage());
            } else {
                Log.e("FindTreasureActivity", "Exception without a message.");
            }
            return false;
        }
        return true;
    }
}
