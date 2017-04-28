package com.rit.se.treasurehuntvuz;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

// Jeffrey Haines 3/4/17
//    Added final treasure crown image and redirection to EnterHighscoreLayout
// Jeffrey Haines 3/1/17
//    Link to create AnimationDrawable from GIF
//        http://tusharonweb.in/AndroidResourceTools/GifToAnimationDrawable/
//    Link that explains AnimtationDrawable
//        https://developer.android.com/reference/android/graphics/drawable/AnimationDrawable.html
//    Stackoverflow question where Activities must be listed in the AndroidManifest.xml file
//        http://stackoverflow.com/questions/42522956/null-exception-starting-activity-with-intent/
//    Link that explains how to pass information over an intent
//        http://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-on-android
public class ShowTreasureActivity extends AppCompatActivity {
    private AnimationDrawable treasureAnimation;
    private boolean finalTreasure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_treasure);

        int numCollected = getIntent().getIntExtra("NUM_COLLECTED", -1);
        int numTotal = getIntent().getIntExtra("NUM_TOTAL", -1);

        if(numCollected < numTotal) {
            finalTreasure = false;

            // Set the progress string
            String progressString = String.format(getString(R.string.progress_string), numCollected, numTotal);
            TextView progressTextView = (TextView) findViewById(R.id.progressTextView);
            progressTextView.setText(progressString);
            progressTextView.setTextSize(26);

            // Set the treasure animation
            ImageView treasureImage = (ImageView) findViewById(R.id.treasureAnimationView);
            treasureImage.setBackgroundResource(R.drawable.anim_treasure);
            treasureAnimation = (AnimationDrawable) treasureImage.getBackground();
            treasureAnimation.setOneShot(true);
            treasureAnimation.stop();
        }
        else {
            finalTreasure = true;

            // Set the final treasure string
            String progressString = getString(R.string.final_treasure_string);
            TextView progressTextView = (TextView) findViewById(R.id.progressTextView);
            progressTextView.setText(progressString);
            progressTextView.setTextSize(26);

            // Set the crown image
            ImageView crownImage = (ImageView) findViewById(R.id.treasureAnimationView);
            crownImage.setBackgroundResource(R.drawable.crown_treasure);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!finalTreasure) {
            treasureAnimation.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!finalTreasure) {
            treasureAnimation.setVisible(true, false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!finalTreasure) {
            treasureAnimation.stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!finalTreasure) {
            treasureAnimation.stop();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(!finalTreasure) {
            treasureAnimation.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(!finalTreasure) {
            try {
                Intent findTreasureActivityIntent = new Intent(ShowTreasureActivity.this, FindTreasureActivity.class);
                startActivity(findTreasureActivityIntent);
                Log.d("ShowTreasureActivity", "Going to FindTreasureActivity");
                finish();
            } catch (Exception exception) {
                if(exception.getMessage() != null) {
                    Log.e("ShowTreasureActivity", exception.getMessage());
                } else {
                    Log.e("ShowTreasureActivity", "Exception without a message.");
                }
            }
        }
        else {
            try {
                Intent enterHighScoreActivityIntent = new Intent(ShowTreasureActivity.this, EnterHighScoreActivity.class);
                enterHighScoreActivityIntent.putExtra("HIGHSCORE", TreasuresSingleton.getTreasures().getTreasureHuntScore());

                TreasuresSingleton.getTreasures().saveTreasureHuntGame(false);

                startActivity(enterHighScoreActivityIntent);
                Log.d("ShowTreasureActivity", "Going to EnterHighScoreActivity");
                finish();
            } catch (Exception exception) {
                if(exception.getMessage() != null) {
                    Log.e("ShowTreasureActivity", exception.getMessage());
                } else {
                    Log.e("ShowTreasureActivity", "Exception without a message.");
                }
            }
        }
    }
}
