package com.rit.se.treasurehuntvuz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EnterHighScoreActivity extends AppCompatActivity {
    private String playerName;
    private int highScore;
    private EditText enterNameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterhighscore);
        highScore = getIntent().getIntExtra("HIGHSCORE", -1);

        // Set high score string
        String scoreString = String.format(getString(R.string.score_string), highScore);
        TextView progressTextView = (TextView) findViewById(R.id.score_text_view);
        progressTextView.setText(scoreString);
        progressTextView.setTextSize(26);

        // set enter score text field
        enterNameField = (EditText) findViewById(R.id.enter_name_field);
        enterNameField.setTextSize(26);

        // set confirm player name button
        String confirmPlayerNameButtonString = getString(R.string.confirm_button);
        Button confirmPlayerNameButton = (Button) findViewById(R.id.confirm_player_name_button);
        confirmPlayerNameButton.setText(confirmPlayerNameButtonString);
        confirmPlayerNameButton.setTextSize(26);
        confirmPlayerNameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onConfirmPlayerNameButtonClick((Button) v);
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
        showHighScoreLayout();
    }

    public void onConfirmPlayerNameButtonClick(Button confirmPlayerNameButton) {
        if(validatedPlayerName()) {
            // Save high score to file
            try {
                HighScores highScores = HighScoresSingleton.getHighScores();
                highScores.addHighScore(highScores.new HighScore(playerName, highScore));
            }
            catch (Exception exception) {
                if(exception.getMessage() != null) {
                    Log.e("EnterHighScoreActivity", exception.getMessage());
                } else {
                    Log.e("EnterHighScoreActivity", "Exception without a message.");
                }
            }

            showHighScoreLayout();
        }
        else {
            // TODO: Player name is invalid, notify player.
        }
    }

    private void showHighScoreLayout() {
        try {
            Intent showHighScoreActivityIntent = new Intent(this, ShowHighScoreActivity.class);
            startActivity(showHighScoreActivityIntent);
            Log.d("EnterHighScoreActivity", "Going to HighScoreActivity");
            finish();
        }
        catch(Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("EnterHighScoreActivity", exception.getMessage());
            } else {
                Log.e("EnterHighScoreActivity", "Exception without a message.");
            }
        }
    }

    private boolean validatedPlayerName() {
        playerName = enterNameField.getText().toString();
        // player name must be 3 characters long
        return playerName.length() == 3;
    }
}
