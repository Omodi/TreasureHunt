package com.rit.se.treasurehuntvuz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set high score string
        String aboutTextString = "";
        aboutTextString += getString(R.string.app_name);
        aboutTextString += "\n\n";
        aboutTextString += getString(R.string.jeffrey_name) + "\n" + getString(R.string.jeffrey_email) + "\n\n";
        aboutTextString += getString(R.string.aziz_name) + "\n" + getString(R.string.aziz_email) + "\n\n";
        aboutTextString += getString(R.string.colton_name) + "\n" + getString(R.string.colton_email) + "\n\n";
        aboutTextString += getString(R.string.rafael_name) + "\n" + getString(R.string.rafael_email) + "\n\n";
        aboutTextString += getString(R.string.huseen_name) + "\n" + getString(R.string.huseen_email) + "\n\n";
        aboutTextString += getString(R.string.steven_name) + "\n" + getString(R.string.steven_email) + "\n\n";
        aboutTextString += getString(R.string.nathen_name) + "\n" + getString(R.string.nathen_email) + "\n\n";

        TextView aboutTextView = (TextView) findViewById(R.id.about_text_view);
        aboutTextView.setMovementMethod(new ScrollingMovementMethod());
        aboutTextView.setText(aboutTextString);
        aboutTextView.setTextSize(26);
    }

    @Override
    public void onBackPressed() {
        try {
            Intent mainActivityIntent = new Intent(AboutActivity.this, MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
        }
        catch(Exception exception) {
            Log.e("AboutActivity", exception.getMessage());
        }
    }
}
