package com.rit.se.treasurehuntvuz;

// Jeffrey HAines 4/15/17
//    Singleton here eliminates any sync issues between acclivities and instant run.

class HighScoresSingleton {
    private static HighScores highScoresSingleton;

    static HighScores getHighScores() {
        if(highScoresSingleton == null) {
            highScoresSingleton = new HighScores();
        }

        return highScoresSingleton;
    }
}
