package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoggedInActivity extends AppCompatActivity {
    private Button settingsBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        settingsBtn = findViewById(R.id.accountBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoggedInActivity.this, MyWrapsActivity.class);
                startActivity(intent);
            }
        });
        // Add any initialization or setup code for your logged-in view
    }
    public void logoutOnClick(View view) {
        // Launch the Spotify logout URL in a browser
        String logoutUrl = "https://accounts.spotify.com";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(logoutUrl));
        startActivity(intent);
        finish();
    }
}
