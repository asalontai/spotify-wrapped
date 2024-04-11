package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    //public static final String REDIRECT_URI = "SPOTIFY-SDK://auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Uri uri = getIntent().getData();

        Log.i("This is the uri", String.valueOf(uri));

        //why is this uri != null here when we already have it in the authActivity

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