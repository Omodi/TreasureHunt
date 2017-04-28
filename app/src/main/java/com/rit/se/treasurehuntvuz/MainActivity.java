package com.rit.se.treasurehuntvuz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// Jeffrey Haines 3/6/17
//    Created the main Activity, to get to Start, High Score, and About screens.
// Jeffrey Haines 4/15/17
//    High Score button now takes your to the High Score screen.
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set main menu text view
        String whereToPlayString = getString(R.string.main_menu_string);
        TextView whereToPlayTextView = (TextView) findViewById(R.id.main_menu_text_view);
        whereToPlayTextView.setText(whereToPlayString);
        whereToPlayTextView.setTextSize(30);
        whereToPlayTextView.setPadding(0, 5, 0, 5);

        // Set start button
        String startButtonString = getString(R.string.start_button);
        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setText(startButtonString);
        startButton.setTextSize(26);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onStartButtonClick((Button) v);
            }
        });

        // Set high score button
        String highScoreButtonString = getString(R.string.high_score_button);
        Button highScoreButton = (Button) findViewById(R.id.high_score_button);
        highScoreButton.setText(highScoreButtonString);
        highScoreButton.setTextSize(26);
        highScoreButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onHighScoreButtonClick((Button) v);
            }
        });

        // Set about button
        String aboutButtonString = getString(R.string.about_button);
        Button aboutButton = (Button) findViewById(R.id.about_button);
        aboutButton.setText(aboutButtonString);
        aboutButton.setTextSize(26);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onAboutButtonClick((Button) v);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d("MainActivity", "Going back to Android Dashboard");
        finish();
    }

    private void onStartButtonClick(Button startButton) {
        try {
            Intent startGameActivityIntent = new Intent(MainActivity.this, StartGameActivity.class);
            startActivity(startGameActivityIntent);
            Log.d("MainActivity", "Going to StartGameActivity");
            finish();
        } catch (Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("MainActivity", exception.getMessage());
            } else {
                Log.e("HighScoreLoad", "Exception without a message.");
            }
        }
    }

    private void onHighScoreButtonClick(Button fileButton) {
        try {
            Intent showHighScoreActivityIntent = new Intent(MainActivity.this, ShowHighScoreActivity.class);
            startActivity(showHighScoreActivityIntent);
            Log.d("MainActivity", "Going to HighScoreActivity");
            finish();
        } catch (Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("MainActivity", exception.getMessage());
            } else {
                Log.e("HighScoreLoad", "Exception without a message.");
            }
        }
    }

    private void onAboutButtonClick(Button aboutButton) {
        try {
            Intent aboutActivityIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutActivityIntent);
            Log.d("MainActivity", "Going to AboutActivity");
            finish();
        } catch (Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("MainActivity", exception.getMessage());
            } else {
                Log.e("HighScoreLoad", "Exception without a message.");
            }
        }
    }

}
