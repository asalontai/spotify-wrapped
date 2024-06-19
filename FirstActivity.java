package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.Arrays;

public class FirstActivity extends AppCompatActivity {

    private TextView text1, text2;
    private ImageView image1, image2;

    private Button next1;
    private MediaPlayer m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        text1 = findViewById(R.id.text1);
        image1 = findViewById(R.id.image1);

        text2 = findViewById(R.id.text2);
        image2 = findViewById(R.id.image2);

        next1 = findViewById(R.id.next1);

        Intent intent = getIntent();
        String[] artistNames = intent.getStringArrayExtra("artistNames");
        String[] artistImageUrls = intent.getStringArrayExtra("artistImageUrls");
        String[] trackNames = intent.getStringArrayExtra("trackNames");
        String[] trackImageUrls = intent.getStringArrayExtra("trackImageUrls");
        String listeningHistory = intent.getStringExtra("listeningHistory");

        Log.d("tracks", Arrays.toString(trackNames));
        Log.d("names", Arrays.toString(trackImageUrls));

        // Assuming you want to display the first element
        if (artistNames != null && artistImageUrls != null && trackNames != null && trackImageUrls != null) {
            loadImage(trackImageUrls[0], image1);
            text1.setText(trackNames[0]);
            loadImage(artistImageUrls[0], image2);
            text2.setText(artistNames[0]);
        }

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle image click event
            }
        });

        next1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
                // Pass data as extras to the intent
                intent.putExtra("artistNames", artistNames);
                intent.putExtra("artistImageUrls", artistImageUrls);
                intent.putExtra("trackNames", trackNames);
                intent.putExtra("trackImageUrls", trackImageUrls);
                intent.putExtra("listeningHistory", listeningHistory);
                startActivity(intent);
            }
        });
    }

    private void loadImage(String imageUrl, ImageView imageView) {
        Picasso.get().load(imageUrl).into(imageView);
    }

    public void startAudioStream(String url) {
        if (m == null)
            m = new MediaPlayer();
        try {
            m.setDataSource(url);
            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(false);
            m.start();
        } catch (Exception e) {
            Log.d("AUDIO", "Error playing in SoundHandler: " + e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
