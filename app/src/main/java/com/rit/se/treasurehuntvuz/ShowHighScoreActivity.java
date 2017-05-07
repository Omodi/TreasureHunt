package com.rit.se.treasurehuntvuz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ShowHighScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_high_score);

        // set main menu text view
        String highScoreTitleString = getString(R.string.high_score_title_string);
        TextView highScoreTitleTextView = (TextView) findViewById(R.id.high_score_title_text_view);
        highScoreTitleTextView.setText(highScoreTitleString);
        highScoreTitleTextView.setTextSize(30);
        highScoreTitleTextView.setPadding(0, 5, 0, 10);

        // set high score text view
        StringBuilder highScoreString = new StringBuilder();
        HighScores highScores = HighScoresSingleton.getHighScores();
        int rankCount = 1;
        for(String scoreString : highScores.getHighScoreStrings()) {
            highScoreString.append(rankCount);
            highScoreString.append(" )  ");
            highScoreString.append(scoreString);
            highScoreString.append("\n");
            rankCount++;
        }
        TextView progressTextView = (TextView) findViewById(R.id.high_score_text_view);
        progressTextView.setText(highScoreString.toString());
        progressTextView.setTextSize(26);
    }

    @Override
    public void onBackPressed() {
        try {
            Intent mainActivityIntent = new Intent(ShowHighScoreActivity.this, MainActivity.class);
            startActivity(mainActivityIntent);
            Log.d("ShowHighScoreActivity", "Going to MainActivity");
            finish();
        }
        catch(Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("ShowHighScoreActivity", exception.getMessage());
            } else {
                Log.e("ShowHighScoreActivity", "Exception without a message.");
            }
        }
    }
}
