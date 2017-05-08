package com.rit.se.treasurehuntvuz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.*;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import java.util.Random;

public class RandomGameActivity extends AppCompatActivity {
    // configuration
    private static final long GPS_UPDATE_TIME = 5;
    private static final float GPS_UPDATE_DISTANCE = 0;
    private static final int RAND_GAME_TREASURES = 3;

    private ImageView randomGameStatusImageView;
    private int currentAnimationImageResource;
    private AnimationDrawable randomGameStatusAnimation;
    private Handler handler;
    private RandomGameRunnable randomGameRunnable;
    private Thread randomGameThread;
    private LocationManager manager;
    private LocationListener listener;

    private enum RandomGameState {
        WAITING_FOR_LOCATION,
        IMPROVING_LOCATION,
        LOCKED_LOCATION,
        GENERATING_GAME,
        DONE
    }

    private class RandomGameRunnable implements Runnable {
        // configuration
        private final int LOCATION_COUNT_UPDATE_MAX = 5;
        private final int RANDOM_GAME_THREAD_SLEEP = 1000;

        private RandomGameState mRandomGameState;
        private final Object mPauseLock;
        private boolean mPaused;
        private boolean mFinished;
        private boolean mLocationUpdated;
        private Location mPlayerLocation;
        private int mLocationUpdateCount;

        RandomGameRunnable() {
            mRandomGameState = RandomGameState.WAITING_FOR_LOCATION;
            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;
            mPlayerLocation = null;
            mLocationUpdateCount = 0;
        }

        public void run() {
            int tickCount = 0;

            // create random treasure game loop
            while (!mFinished) {
                Log.v("RandomGameThread", String.format("Tick: %d", tickCount));

                switch(mRandomGameState) {
                    case WAITING_FOR_LOCATION:
                        if(mPlayerLocation == null) {
                            break;
                        }
                        Log.d("RandomGameThread", "Location found!");
                        mRandomGameState = RandomGameState.IMPROVING_LOCATION;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                currentAnimationImageResource = R.drawable.anim_locking_on;
                                randomGameStatusImageView.setBackgroundResource(currentAnimationImageResource);
                                randomGameStatusAnimation = (AnimationDrawable) randomGameStatusImageView.getBackground();
                                randomGameStatusAnimation.start();
                            }
                        });
                        // fall through
                    case IMPROVING_LOCATION:
                        if(mLocationUpdated && mLocationUpdateCount < LOCATION_COUNT_UPDATE_MAX) {
                            mLocationUpdated = false;
                            mLocationUpdateCount++;
                            break;
                        }
                        Log.d("RandomGameThread", "Location locked on!");
                        mRandomGameState = RandomGameState.LOCKED_LOCATION;
                        // fall through
                    case LOCKED_LOCATION:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                manager.removeUpdates(listener);
                                currentAnimationImageResource = R.drawable.anim_working;
                                randomGameStatusImageView.setBackgroundResource(currentAnimationImageResource);
                                randomGameStatusAnimation = (AnimationDrawable) randomGameStatusImageView.getBackground();
                                randomGameStatusAnimation.start();
                            }
                        });
                        Log.d("RandomGameThread", "Generating game!");
                        mRandomGameState = RandomGameState.GENERATING_GAME;
                        break;
                    case GENERATING_GAME:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // generate random treasures
                                for(int i = 0; i < RAND_GAME_TREASURES; i++) {
                                    Log.d("RandomGameThread",
                                            String.format("Treasure b #%1$d: lat: %2$f lon: %3$f",
                                                    i, mPlayerLocation.getLatitude(), mPlayerLocation.getLongitude()));
                                    Location tmpLocation = new Location("");
                                    tmpLocation.setLatitude(getDistanceOffsetLatitude(mPlayerLocation.getLatitude()));
                                    tmpLocation.setLongitude(getDistanceOffsetLongitude(mPlayerLocation.getLongitude()));
                                    Log.d("RandomGameThread",
                                            String.format("Treasure a #%1$d: lat: %2$f lon: %3$f",
                                                    i, tmpLocation.getLatitude(), tmpLocation.getLongitude()));
                                    TreasuresSingleton.getTreasures().addTreasure(tmpLocation);
                                }
                                startGame();
                            }
                        });
                        mRandomGameState = RandomGameState.DONE;
                        // fall through
                    case DONE:
                        break;
                }

                try {
                    Thread.sleep(RANDOM_GAME_THREAD_SLEEP);
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
            mLocationUpdated = true;
            mPlayerLocation = newLocation;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_game);

        // setup random game status image
        randomGameStatusImageView = (ImageView) findViewById(R.id.random_game_status_image_view);
        currentAnimationImageResource = R.drawable.anim_gps;
        randomGameStatusImageView.setBackgroundResource(currentAnimationImageResource);
        randomGameStatusAnimation = (AnimationDrawable) randomGameStatusImageView.getBackground();

        handler = new Handler(getApplicationContext().getMainLooper());

        // setup randomGameThread
        randomGameRunnable = new RandomGameRunnable();
        randomGameThread = new Thread()
        {
            @Override
            public void run() {
                randomGameRunnable.run();
            }
        };

        // setup location manager and listener
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location != null) {
                    Log.v("RandomGameActivity", String.format("Location update: %f, %f", location.getLongitude(), location.getLatitude()));
                    // pass location to randomGameThread
                    randomGameRunnable.onLocationChanged(new Location(location));
                } else {
                    Log.d("RandomGameActivity", "Null location");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.v("RandomGameActivity", String.format("Location provider status changed, %s id:%d", provider, status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.v("RandomGameActivity", String.format("Location provider enabled, %s", provider));
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.v("RandomGameActivity", String.format("Lost location provider, %s", provider));
                randomGameRunnable.onLocationChanged(null);
            }
        };

        // request location permissions
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            Log.d("RandomGameActivity", "Location permission denied");
            if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("RandomGameActivity", "Gps enabled");
            } else {
                Log.d("RandomGameActivity", "Gps disabled");
            }

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("RandomGameActivity", "Should show explanation");
            }
        }
    }

    @Override
    protected void onStart() {
        // TODO: Fix application crash when device sleeps. Thread.start() already called.
        Log.v("RandomGameActivity", "Starting RandomGameActivity");
        randomGameThread.start();
        randomGameStatusAnimation.start();
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.v("RandomGameActivity", "Resuming RandomGameActivity");
        randomGameRunnable.onResume();
        startLocationUpdates();
        randomGameStatusAnimation.setVisible(true, false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.v("RandomGameActivity", "Pausing FindTreasureActivity");
        manager.removeUpdates(listener);
        randomGameRunnable.onPause();
        randomGameStatusAnimation.stop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v("RandomGameActivity", "Stopping RandomGameActivity");
        manager.removeUpdates(listener);
        randomGameRunnable.onFinish();
        randomGameStatusAnimation.stop();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.v("RandomGameActivity", "Restarting RandomGameActivity");
        manager.removeUpdates(listener);
        randomGameRunnable.onFinish();
        randomGameStatusAnimation.start();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.v("RandomGameActivity", "Destroying RandomGameActivity");
        manager.removeUpdates(listener);
        randomGameRunnable.onFinish();
        randomGameStatusAnimation.stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        try {
            Intent startGameIntent = new Intent(this, StartGameActivity.class);
            startActivity(startGameIntent);
            randomGameRunnable.onFinish();
            finish();
            Log.d("RandomGameActivity", "Going to SaveGameActivity");
        } catch(Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("RandomGameActivity", exception.getMessage());
            } else {
                Log.e("RandomGameActivity", "Exception without a message.");
            }
        }
    }

    private final int RADIUS_EARTH = 6378137;
    private final int BASE_OFFSET_METERS = 50;
    private final int MAX_OFFSET_METERS = 30;
    private final int MAX_VARIABLE_OFFSET_METERS = 100;
    private final int MIN_VARIABLE_OFFSET_METERS = 20;
    private Random random = new Random();

    // TODO: improve treasure generation by adding some random distance
    private double getDistanceOffsetLatitude(double latitude) {
        int number = random.nextInt(MAX_VARIABLE_OFFSET_METERS + 1 -MIN_VARIABLE_OFFSET_METERS) + MIN_VARIABLE_OFFSET_METERS;

        // latitude + base_offset + random_distance
        return latitude + BASE_OFFSET_METERS * 100 / Math.PI + number * 100 / Math.PI;
    }

    // TODO: improve treasure generation by adding some random distance
    private double getDistanceOffsetLongitude(double longitude) {
        int number = random.nextInt(MAX_VARIABLE_OFFSET_METERS + 1 -MIN_VARIABLE_OFFSET_METERS) + MIN_VARIABLE_OFFSET_METERS;

        // latitude + base_offset + random_distance
        return longitude + BASE_OFFSET_METERS / (RADIUS_EARTH * Math.cos(Math.PI * longitude / 180))
                                            + number / (RADIUS_EARTH * Math.cos(Math.PI * longitude / 180));
    }

    private boolean startLocationUpdates() {
        try {
            // start location updates
            Log.d("RandomGameActivity", "Requesting location updates");
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_TIME, GPS_UPDATE_DISTANCE, listener);
            return true;
        } catch (SecurityException exception) {
            if(exception.getMessage() != null) {
                Log.e("RandomGameActivity", exception.getMessage());
            }
            else {
                Log.e("RandomGameActivity", "Exception without a message.");
            }
        }
        return false;
    }

    private boolean startGame() {
        try {
            Intent findTreasureIntent = new Intent(RandomGameActivity.this, FindTreasureActivity.class);
            startActivity(findTreasureIntent);
            randomGameRunnable.onFinish();
            finish();
            Log.d("RandomGameActivity", "Going to FindTreasureActivity");
        } catch(Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("RandomGameActivity", exception.getMessage());
            } else {
                Log.e("RandomGameActivity", "Exception without a message.");
            }
            return false;
        }
        return true;
    }
}
