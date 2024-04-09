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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyWrapsActivity extends AppCompatActivity {

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;
    private String mAccessToken = AuthActivity.accessToken;
    private String mAccessCode = AuthActivity.accessCode;
    private ImageView image1, image2, image3, image4, image5;
    private TextView text1, text2, text3, text4, text5;
    private String[] artistNames = new String[5];
    private String[] imageUrls = new String[5];
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

        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);
        text3 = (TextView) findViewById(R.id.text3);
        text4 = (TextView) findViewById(R.id.text4);
        text5 = (TextView) findViewById(R.id.text5);

        image1 = (ImageView) findViewById(R.id.image1);
        image2 = (ImageView) findViewById(R.id.image2);
        image3 = (ImageView) findViewById(R.id.image3);
        image4 = (ImageView) findViewById(R.id.image4);
        image5 = (ImageView) findViewById(R.id.image5);

        getWraps();
}

    private void getWraps() {
        if (AuthActivity.accessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }


        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?limit=5&offset=0")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MyWrapsActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONArray artistsArray = jsonResponse.getJSONArray("items");

                    for (int i = 0; i < artistsArray.length(); i++) {
                        JSONObject artist = artistsArray.getJSONObject(i);
                        artistNames[i] = artist.getString("name");
                        imageUrls[i] = artist.getJSONArray("images").getJSONObject(0).getString("url");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Set artist names to TextViews
                            text1.setText(artistNames[0]);
                            text2.setText(artistNames[1]);
                            text3.setText(artistNames[2]);
                            text4.setText(artistNames[3]);
                            text5.setText(artistNames[4]);

                            // Load images into ImageViews
                            loadImage(imageUrls[0], image1);
                            loadImage(imageUrls[1], image2);
                            loadImage(imageUrls[2], image3);
                            loadImage(imageUrls[3], image4);
                            loadImage(imageUrls[4], image5);
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyWrapsActivity.this, "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.d("HTTP", "Failed to fetch data: " + e);
                }
            }
        });
    }

    private void loadImage(String imageUrl, ImageView imageView)
    {
        Picasso.get().load(imageUrl).into(imageView);
    }

    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}