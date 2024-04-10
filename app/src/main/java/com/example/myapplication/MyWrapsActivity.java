package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyWrapsActivity extends AppCompatActivity {

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call caCall, ctCall, saCall, stCall, yaCall, ytCall;
    private String mAccessCode = AuthActivity.accessCode;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9, image10;
    private TextView text1, text2, text3, text4, text5, text6, text7, text8, text9, text10;
    private String[] artistNames = new String[5];
    private String[] artistImageUrls = new String[5];

    private String[] trackNames = new String[5];
    private String[] trackImageUrls = new String[5];
    private Button toMyAccountBtn;
    private int completedCalls = 0;
    private Button year, sixMonths, current;
    private String listeningHistory = "";


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
            getTopTracks("https://api.spotify.com/v1/me/top/tracks?time_range=long_term&limit=5&offset=0", ytCall); // Pass the callback
            getArtist("https://api.spotify.com/v1/me/top/artists?time_range=long_term&limit=5&offset=0", yaCall); // Pass the callback
        });

        sixMonths.setOnClickListener((v) -> {
            completedCalls = 0; // Reset completed calls counter
            getTopTracks("https://api.spotify.com/v1/me/top/tracks?time_range=medium_term&limit=5&offset=0", stCall); // Pass the callback
            getArtist("https://api.spotify.com/v1/me/top/artists?time_range=medium_term&limit=5&offset=0", saCall); // Pass the callback
        });

        current.setOnClickListener((v) -> {
            completedCalls = 0; // Reset completed calls counter
            getTopTracks("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=5&offset=0", ctCall); // Pass the callback
            getArtist("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=5&offset=0", caCall); // Pass the callback
        });
    }

    private void getArtist(String url, Call call) {
        if (AuthActivity.accessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Log.d("Token", AuthActivity.accessToken);
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AuthActivity.accessToken)
                .build();

        Headers headers = request.headers();
        for (String name : headers.names()) {
            Log.d("Request Header", name + ": " + headers.get(name));
        }

        cancelCall(call);
        call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    MyWrapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("HTTP", "Failed to fetch data: " + e);
                            Toast.makeText(MyWrapsActivity.this, "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    Log.d("API response: Artist", responseData);
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray items = jsonObject.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            String artistName = item.getString("name");
                            artistNames[i] = artistName;

                            JSONArray images = item.getJSONArray("images");
                            JSONObject image = images.getJSONObject(0); // Assuming the first image is the one to be displayed
                            String imageUrl = image.getString("url");
                            artistImageUrls[i] = imageUrl;
                        }
                        Log.d("HTTP", "before fetched1");
                        completedCalls++;
                        onDataFetched(); // Call callback
                        Log.d("HTTP", "after fetched1");
                    } catch (JSONException e) {
                        Log.d("JSON", "Failed to parse data: " + e);
                        runOnUiThread(() -> Toast.makeText(MyWrapsActivity.this, "Failed to parse data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Handle unsuccessful response
                    runOnUiThread(() -> Toast.makeText(MyWrapsActivity.this, "Unsuccessful response", Toast.LENGTH_SHORT).show());
                }
            }

        });
    }

    private void getTopTracks(String url, Call call) {
        Log.d("HTTP: Tracks", "Track method reached");
        if (AuthActivity.accessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Log.d("Token", AuthActivity.accessToken);
        }

        // Create a request to get the user's top tracks
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AuthActivity.accessToken)
                .build();

        cancelCall(call);
        call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    MyWrapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("HTTP", "Failed to fetch data: " + e);
                            Toast.makeText(MyWrapsActivity.this, "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    Log.d("API response: Tracks", responseData);
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray items = jsonObject.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            String trackName = item.getString("name");
                            trackNames[i] = trackName;

                            JSONObject album = item.getJSONObject("album");
                            JSONArray images = album.getJSONArray("images");
                            JSONObject image = images.getJSONObject(0); // Assuming the first image is the one to be displayed
                            String imageUrl = image.getString("url");
                            trackImageUrls[i] = imageUrl;
                            Log.d("Array", trackNames[i]);
                        }
                        Log.d("HTTP", "before fetched2");
                        completedCalls++;
                        onDataFetched(); // Call callback
                        Log.d("HTTP", "after fetched2");
                    } catch (JSONException e) {
                        Log.d("JSON", "Failed to parse data: " + e);
                        runOnUiThread(() -> Toast.makeText(MyWrapsActivity.this, "Failed to parse data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Handle unsuccessful response
                    runOnUiThread(() -> Toast.makeText(MyWrapsActivity.this, "Unsuccessful response", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void loadImage(String imageUrl, ImageView imageView) {
        Picasso.get().load(imageUrl).into(imageView);
    }

    private void onDataFetched() {
        if (completedCalls == 2) {
            StringBuilder listeningHistoryBuilder = new StringBuilder();

            // Concatenate the track names
            for (String trackName : trackNames) {
                if (trackName != null) {
                    listeningHistoryBuilder.append(trackName).append("\n");
                }
            }

            // Convert StringBuilder to String
            listeningHistory = listeningHistoryBuilder.toString().trim();
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

    private void cancelCall(Call mCall) {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        cancelCall(stCall);
        super.onDestroy();
    }
    public void onGemButtonClick(View view) {
        // Start LLMActivity when the button is clicked
        Intent intent = new Intent(this, LLMActivity.class);
        intent.putExtra("listeningHistory", listeningHistory);
        startActivity(intent);
    }
}
