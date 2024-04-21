package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

interface ResponseLogic {
    void handleResponse(Response response) throws IOException;
}

public class MyWrapsActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private String sToken = AuthActivity.accessToken;
    private String[] artistNames = new String[5], artistImageUrls = new String[5];
    private String[] trackNames = new String[5], trackImageUrls = new String[5], trackURIs = new String[5];
    private String[] trackClipUrls = new String[5];
    private Button toMyAccountBtn, year, sixMonths, current;
    private MediaPlayer m;
    private boolean isStreaming = false;
    private int completedCalls = 0;
    private String listeningHistory = "";

    public static Date currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopPlaying();
        setContentView(R.layout.activity_my_wraps);
        currentTime = Calendar.getInstance().getTime();
        toMyAccountBtn = findViewById(R.id.MyAccountBtn);
        toMyAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaying();
                Intent intent = new Intent(MyWrapsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        year = findViewById(R.id.year);
        sixMonths = findViewById(R.id.sixMonth);
        current = findViewById(R.id.current);


        year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completedCalls = 0; // Reset completed calls counter
                getTracks("https://api.spotify.com/v1/me/top/tracks?time_range=long_term&limit=5&offset=0"); // Pass the callback
                getArtist("https://api.spotify.com/v1/me/top/artists?time_range=long_term&limit=5&offset=0");
            }
        });

        sixMonths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completedCalls = 0; // Reset completed calls counter
                getTracks("https://api.spotify.com/v1/me/top/tracks?time_range=medium_term&limit=5&offset=0"); // Pass the callback
                getArtist("https://api.spotify.com/v1/me/top/artists?time_range=medium_term&limit=5&offset=0");
            }
        });
        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completedCalls = 0; // Reset completed calls counter
                getTracks("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=5&offset=0"); // Pass the callback
                getArtist("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=5&offset=0");
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
                            Log.d("ArtistName", Arrays.toString(artistNames));
                            Log.d("ArtistImages", Arrays.toString(artistImageUrls));
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
                Log.d("ArtistName", Arrays.toString(artistNames));
                Log.d("ArtistImages", Arrays.toString(artistImageUrls));
                Log.d("Tracknames", Arrays.toString(trackNames));
                Log.d("TrackImages", Arrays.toString(trackImageUrls));
                createWrap("current", artistNames, trackNames);
                Intent intent = new Intent(MyWrapsActivity.this, FirstActivity.class);
                intent.putExtra("artistNames", artistNames);
                intent.putExtra("artistImageUrls", artistImageUrls);
                intent.putExtra("trackNames", trackNames);
                intent.putExtra("trackImageUrls", trackImageUrls);
                startActivity(intent);
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
    public void onGemButtonClick(View view) {
        // Start LLMActivity when the button is clicked
        Intent intent = new Intent(this, LLMActivity.class);
        intent.putExtra("listeningHistory", listeningHistory);
        startActivity(intent);
    }

    private void createWrap(String timeRange, String[] artistNames, String[] trackNames) {
        // Create a Gson instance
        Gson gson = new Gson();
        // Serialize wrap information into JSON
        String wrapJson = gson.toJson(new Wrap(AuthActivity.userName, timeRange, Calendar.getInstance().getTime(), artistNames, trackNames));

        // Get a reference to the Firestore collection for wraps
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference wrapsCollection = db.collection("wraps");

        // Add the wrap JSON string to Firestore
        wrapsCollection.add(new HashMap<String, Object>() {{
                    put("wrapJson", wrapJson);
                    put("user", AuthActivity.userName);
                    put("timeStamp", Calendar.getInstance().getTime());
                }})
                .addOnSuccessListener(documentReference -> Log.d("Firebase", "Wrap added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("Firebase", "Error adding wrap", e));
    }

    class Wrap {
        String user;
        String timeRange;
        Date timestamp;
        String[] artistNames;
        String[] trackNames;

        public Wrap(String user, String timeRange, Date timestamp, String[] artistNames, String[] trackNames) {
            this.user = user;
            this.timeRange = timeRange;
            this.timestamp = timestamp;
            this.artistNames = artistNames;
            this.trackNames = trackNames;
        }
    }
}
