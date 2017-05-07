package com.rit.se.treasurehuntvuz;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.*;
import java.util.*;

// Jeffrey HAines 4/15/17
//    Created this high score interface to encapsulate the management of high scores.

class HighScores implements Serializable {

    class HighScore implements Serializable, Comparator<HighScore>, Comparable<HighScore>{
        private final String playerName;
        private final int score;

        HighScore() {
            this.playerName = "XXX";
            this.score = 0;
        }

        HighScore(String playerName, int score) {
            if (score < 0 || score > 9999) {
                throw new IllegalArgumentException("Score must be between 0 and 9999.");
            }
            this.score = score;

            if (playerName.length() != 3) {
                throw new IllegalArgumentException("Player name must be 3 characters long.");
            }
            this.playerName = playerName;
        }

        @Override
        public int compareTo(@NonNull HighScore otherScore) {
            if(this.score == otherScore.score) {
                return 0;
            }
            else {
                return this.score > otherScore.score ? 1 : -1;
            }
        }

        @Override
        public int compare(HighScore score1, HighScore score2) {
            // highest scores first
            return score2.score - score1.score;
        }

        @Override
        public String toString() {
            return this.playerName.toUpperCase() + "   " + this.score;
        }
    }

    private List<HighScore> scores;
    private transient boolean isSynced;

    HighScores() {
        this.scores = new ArrayList<>();
        this.isSynced = false;
        this.load();
    }

    void addHighScore(HighScore newScore) {
        scores.add(newScore);
        Collections.sort(scores, new HighScore());
        // keep top 5
        if(scores.size() > 5) {
            scores = new ArrayList<>(scores.subList(0, 5));
        }
        Log.d("HighScoreAdd", String.format("Added %s", newScore.toString()));
        isSynced = false;
        save();
    }

    List<String> getHighScoreStrings() {
        List<String> highScoreStrings = new ArrayList<>();
        for(HighScore score : this.scores) {
            highScoreStrings.add(score.toString());
        }
        return highScoreStrings;
    }

    private void save() {
        try {
            if(!this.isSynced) {
                // NOTE: Try w/ resources requires android 19
                File appDir = TreasureHuntVuzApp.getApplicationFilesDir();
                File highScoreFile = new File(appDir + "highscores.ser");
                OutputStream file = new FileOutputStream(highScoreFile);
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer);

                // write this to file
                output.writeObject(this);

                output.close();
                buffer.close();
                file.close();

                Log.d("HighScoreSave", "High scores saved!");
                this.isSynced = true;
            }
        } catch (Exception exception) {
            if(exception.getMessage() != null) {
                Log.d("HighScoreSave", exception.getMessage());
            } else {
                Log.e("HighScoreSave", "Exception without a message.");
            }
            this.isSynced = false;
        }
    }

    private void load() {
        try {
            if(!this.isSynced) {
                File appDir = TreasureHuntVuzApp.getApplicationFilesDir();
                File highScoreFile = new File(appDir + "highscores.ser");
                if(!highScoreFile.isFile()) {
                    Log.d("HighScoreLoad", "High scores file not found, saving...");
                    save();
                }

                // NOTE: Try w/ resources requires android 19
                InputStream file = new FileInputStream(highScoreFile);
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer);

                // load this from file
                HighScores highScores = (HighScores) input.readObject();
                // TODO; is a deep copy needed here?
                this.scores = highScores.scores;

                input.close();
                buffer.close();
                file.close();

                for(HighScore score : this.scores) {
                    Log.d("HighScoreLoad", String.format("Loaded: %s", score.toString()));
                }

                Log.d("HighScoreLoad", "High scores loaded!");

                this.isSynced = true;
            }
        } catch (Exception exception) {
            if(exception.getMessage() != null) {
                Log.e("HighScoreLoad", exception.getMessage());
            } else {
                Log.e("HighScoreLoad", "Exception without a message.");
            }
            this.isSynced = false;
        }
    }
}
