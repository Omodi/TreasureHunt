package com.rit.se.treasurehuntvuz;

import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class StartGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        // Set where to play text view
        String whereToPlayString = getString(R.string.where_to_play_string);
        TextView whereToPlayTextView = (TextView) findViewById(R.id.where_to_play_text_view);
        whereToPlayTextView.setText(whereToPlayString);
        whereToPlayTextView.setTextSize(30);
        whereToPlayTextView.setPadding(0, 5, 0, 5);

        // Set resume
        String resumeButtonString = getString(R.string.resume_button);
        Button resumeButton = (Button) findViewById(R.id.resume_button);
        resumeButton.setText(resumeButtonString);
        resumeButton.setTextSize(26);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onResumeButtonClick((Button) v);
            }
        });

        // Set random button
        String randomButtonString = getString(R.string.random_button);
        Button randomButton = (Button) findViewById(R.id.random_button);
        randomButton.setText(randomButtonString);
        randomButton.setTextSize(26);
        randomButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onRandomButtonClick((Button) v);
            }
        });

        // Set file button
        String fileButtonString = getString(R.string.file_button);
        Button fileButton = (Button) findViewById(R.id.file_button);
        fileButton.setText(fileButtonString);
        fileButton.setTextSize(26);
        fileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onFileButtonClick((Button) v);
            }
        });

        // Load previous game
        TreasuresSingleton.getTreasures().loadTreasureHuntGame();

        // Set resume visibility
        resumeButton.setEnabled(TreasuresSingleton.getTreasures().getResume());
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
        try {
            Intent mainActivityIntent = new Intent(StartGameActivity.this, MainActivity.class);
            startActivity(mainActivityIntent);
            Log.d("StartGameActivity", "Going to MainActivity");
            finish();
        }
        catch(Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("StartGameActivity", exception.getMessage());
            } else {
                Log.e("StartGameActivity", "Exception without a message.");
            }
        }
    }

    private void onResumeButtonClick(Button resumeButton) {
        if(TreasuresSingleton.getTreasures().getResume()) {
            try {
                Intent findTreasureActivityIntent = new Intent(StartGameActivity.this, FindTreasureActivity.class);
                startActivity(findTreasureActivityIntent);
                Log.d("StartGameActivity", "Going to FindTreasureActivity");
                finish();
            } catch (Exception exception) {
                if(exception.getMessage() != null) {
                    Log.e("StartGameActivity", exception.getMessage());
                } else {
                    Log.e("StartGameActivity", "Exception without a message.");
                }
            }
        }
    }

    private void onRandomButtonClick(Button randomButton) {
        try {
            Intent randomGameActivityIntent = new Intent(StartGameActivity.this, RandomGameActivity.class);
            startActivity(randomGameActivityIntent);
            Log.d("StartGameActivity", "Going to RandomGameActivity");
            finish();
        } catch (Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("StartGameActivity", exception.getMessage());
            } else {
                Log.e("StartGameActivity", "Exception without a message.");
            }
        }
    }

    private void onFileButtonClick(Button fileButton) {
        // TODO: Move this to another activity
        File dir = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File file = new File(dir,"mylocations.txt");
        if(file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                Location newLoc;
                String[] place;
                boolean start = false;
                while((line = br.readLine()) != null){
                    place = line.split(",");
                    if(place.length == 2) {
                        newLoc = new Location("");
                        newLoc.setLongitude(Double.parseDouble(place[0]));
                        newLoc.setLatitude(Double.parseDouble(place[1]));
                        TreasuresSingleton.getTreasures().addTreasure(newLoc);
                        start = true;
                    }
                }

                if(start) {
                    Intent fileActivityIntent = new Intent(StartGameActivity.this, FindTreasureActivity.class);
                    startActivity(fileActivityIntent);
                }else{
                    Toast.makeText(this, "Sorry, this map has no treasures!", Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Sorry, File doesn't exist!", Toast.LENGTH_SHORT).show();
        }
    }
}
