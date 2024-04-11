package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MyWrapsActivity extends AppCompatActivity {
    private Button toMyAccountBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wraps);
        toMyAccountBtn = findViewById(R.id.MyAccountBtn);
        toMyAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MyWrapsActivity.this, SettingsActivity.class);

                startActivity(intent);
            }
        });
    }
}