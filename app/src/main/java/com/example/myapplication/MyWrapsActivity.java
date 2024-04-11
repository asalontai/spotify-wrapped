package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import io.grpc.internal.JsonParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

interface ResponseLogic {
    void handleResponse(Response response) throws IOException;
}

public class MyWrapsActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private String sToken = AuthActivity.accessToken;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9, image10;
    private TextView text1, text2, text3, text4, text5, text6, text7, text8, text9, text10;
    private String[] artistNames = new String[5], artistImageUrls = new String[5];
    private String[] trackNames = new String[5], trackImageUrls = new String[5], trackURIs = new String[5];
    private String[] trackClipUrls = new String[5];
    private Button toMyAccountBtn, year, sixMonths, current;
    private MediaPlayer m;
    private boolean isStreaming = false;
    private int completedCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopPlaying();
        setContentView(R.layout.activity_my_wraps);
        toMyAccountBtn = findViewById(R.id.MyAccountBtn);
        toMyAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyWrapsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);
        text4 = findViewById(R.id.text4);
        text5 = findViewById(R.id.text5);
        text6 = findViewById(R.id.text6);
        text7 = findViewById(R.id.text7);
        text8 = findViewById(R.id.text8);
        text9 = findViewById(R.id.text9);
        text10 = findViewById(R.id.text10);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);
        image10 = findViewById(R.id.image10);

        Button year = findViewById(R.id.year);
        Button sixMonths = findViewById(R.id.sixMonth);
        Button current = findViewById(R.id.current);

        year.setOnClickListener((v) -> {
            completedCalls = 0; // Reset completed calls counter
            getTracks("https://api.spotify.com/v1/me/top/tracks?time_range=long_term&limit=5&offset=0"); // Pass the callback
            getArtist("https://api.spotify.com/v1/me/top/artists?time_range=long_term&limit=5&offset=0");
            stopPlaying();
        });
        sixMonths.setOnClickListener((v) -> {
            completedCalls = 0; // Reset completed calls counter
            getTracks("https://api.spotify.com/v1/me/top/tracks?time_range=medium_term&limit=5&offset=0"); // Pass the callback
            getArtist("https://api.spotify.com/v1/me/top/artists?time_range=medium_term&limit=5&offset=0");
            stopPlaying();
        });
        current.setOnClickListener((v) -> {
            completedCalls = 0; // Reset completed calls counter
            getTracks("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=5&offset=0"); // Pass the callback
            getArtist("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=5&offset=0");
            stopPlaying();
        });

        imageClick(image6, 0);
        imageClick(image7, 1);
        imageClick(image8, 2);
        imageClick(image9, 3);
        imageClick(image10, 4);
    }
    public void imageClick(ImageView image, int i) {
        image.setOnClickListener((v) -> {
            stopPlaying();
            isStreaming = false;

            if (isStreaming) {
                stopPlaying();
                isStreaming = false;
            } else {
                startAudioStream(trackClipUrls[i]);
                isStreaming = true;
            }
        });
    }
    private void getArtist(String url) {
        fetchRequest(
                new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + AuthActivity.accessToken)
                        .build(),
                response -> {
                    if (response.isSuccessful()) {
                        final String responseData = response.body().string();

                        try {
                            JSONObject obj = new JSONObject(responseData);
                            JSONArray items = obj.getJSONArray("items");

                            for(int i = 0; i < items.length(); i++) {
                                JSONObject curr = items.getJSONObject(i);
                                artistNames[i] = curr.getString("name");

                                JSONArray images = curr.getJSONArray("images");
                                JSONObject image = images.getJSONObject(0); // Assuming the first image is the one to be displayed
                                artistImageUrls[i] = image.getString("url");
                            }

                            completedCalls++;
                            onDataFetched();
                        } catch(JSONException j) {
                            Log.d("JSON", "Failed to parse data: " + j);
                        }
                    } else {
                        Log.d("Artist", "Unsuccessful: " + response.code());
                    }
                });
    }
    private void getTracks(String url) {
        fetchRequest(
                new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + AuthActivity.accessToken)
                        .build(),
                response -> {
                    if (response.isSuccessful()) {
                        final String responseData = response.body().string();

                        try {
                            JSONObject obj = new JSONObject(responseData);
                            JSONArray items = obj.getJSONArray("items");

                            for(int i = 0; i < items.length(); i++){
                                JSONObject curr = items.getJSONObject(i);
                                trackNames[i] = curr.getString("name");

                                trackClipUrls[i] = curr.getString("preview_url");

                                JSONObject album = curr.getJSONObject("album");
                                JSONArray images = album.getJSONArray("images");
                                JSONObject image = images.getJSONObject(0);
                                trackImageUrls[i] = image.getString("url");

                                trackURIs[i] = curr.getString("uri");
                            }
                            completedCalls++;
                            onDataFetched();
                        } catch (JSONException j){
                            Log.d("JSON", "Failed to parse data: " + j);
                        }
                    } else {
                        Log.d("Tracks", "Unsuccessful: " + response.code());
                    }
                });
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
    private void stopPlaying() {
        if (m != null && m.isPlaying()) {
            m.stop();
            m.release();
            m = new MediaPlayer();
            m.reset();
        }
    }
    private void loadImage(String imageUrl, ImageView imageView) {
        Picasso.get().load(imageUrl).into(imageView);
    }
    private void onDataFetched() {
        if (completedCalls == 2) {
            runOnUiThread(() -> {
                // Update UI elements with artist and track data
                text1.setText(artistNames[0]);
                text2.setText(artistNames[1]);
                text3.setText(artistNames[2]);
                text4.setText(artistNames[3]);
                text5.setText(artistNames[4]);

                loadImage(artistImageUrls[0], image1);
                loadImage(artistImageUrls[1], image2);
                loadImage(artistImageUrls[2], image3);
                loadImage(artistImageUrls[3], image4);
                loadImage(artistImageUrls[4], image5);

                text6.setText(trackNames[0]);
                text7.setText(trackNames[1]);
                text8.setText(trackNames[2]);
                text9.setText(trackNames[3]);
                text10.setText(trackNames[4]);

                loadImage(trackImageUrls[0], image6);
                loadImage(trackImageUrls[1], image7);
                loadImage(trackImageUrls[2], image8);
                loadImage(trackImageUrls[3], image9);
                loadImage(trackImageUrls[4], image10);
            });
        }
    }
    private void fetchRequest(Request req, ResponseLogic rl) {
        if (sToken == null)
            Toast.makeText(this, "You need an access token!", Toast.LENGTH_SHORT).show();
        else
            Log.d("Token", sToken);

        final Request request = req;

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    MyWrapsActivity.this.runOnUiThread(() -> {
                        Log.d("HTTP", "Failed to fetch data: " + e);
                        Toast.makeText(MyWrapsActivity.this, "Failed to fetch data, check Logcat", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            public void onResponse(Call call, Response response) throws IOException {
                rl.handleResponse(response);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
