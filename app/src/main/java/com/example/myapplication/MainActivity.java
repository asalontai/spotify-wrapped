package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    //public static final String REDIRECT_URI = "SPOTIFY-SDK://auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Uri uri = getIntent().getData();
        if (uri != null && "spotify-wrapped".equals(uri.getScheme()) && "logout".equals(uri.getHost())) {
            // This is the logout callback URL, handle it appropriately
            // For example, navigate back to the main page
            // If you have login logic, you can implement it here
            // For now, let's just finish this activity
            finish();
        }
    }

    public void onLoginButtonClick(View view) {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

}