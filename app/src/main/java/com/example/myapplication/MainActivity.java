package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    //public static final String REDIRECT_URI = "SPOTIFY-SDK://auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onLoginButtonClick(View view) {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }
<<<<<<< Updated upstream
=======


>>>>>>> Stashed changes
}