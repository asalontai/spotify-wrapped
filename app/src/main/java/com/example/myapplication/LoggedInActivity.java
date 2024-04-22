package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LoggedInActivity extends AppCompatActivity {

    private Button settingsBtn, myAccount;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;
    private String mAccessToken = AuthActivity.accessToken;
    private String mAccessCode = AuthActivity.accessCode;



    private TextView tokenTextView, codeTextView, profileTextView, welcomeMessageName;
    private TextView pastWrap1;
    private TextView pastWrap2;
    private TextView pastWrap3;

    private Timestamp pastWrap1Date;
    private Timestamp pastWrap2Date;
    private Timestamp pastWrap3Date;

    private ImageView profileImage;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        //my account button is to go to account settings
        myAccount = findViewById(R.id.landingToAccount);
        myAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoggedInActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        // Initialize TextViews
        pastWrap1 = findViewById(R.id.pastWrap1);
        pastWrap2 = findViewById(R.id.pastWrap2);
        pastWrap3 = findViewById(R.id.pastWrap3);

        Log.d("Firestore", "");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("wraps");
        // Perform a query to retrieve documents from the collection
        collectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            // Iterate over the documents
            int index = 0;
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                // Access the desired field from each document
                String fieldValue = document.getString("wrapJson");
                String userName = document.getString("user");
                if (userName != null && userName.equals(AuthActivity.userName)) {
                    // Filter out non-alphanumeric characters, commas, colons, and spaces
                    String filteredValue = fieldValue.replaceAll("[^\\p{L}0-9,:\\s]+", "")
                            .replaceAll("(user:[^,]+|timeRange:[^,]+)", "")
                            .replaceAll("artistNames", "Top Artists:")
                            .replaceAll("trackNames", "\nTop Tracks:")
                            .replaceAll("timestamp", "\nTimestamp:");

                    // Do something with the filtered value
                    Log.d("Firestore", "Filtered value: " + filteredValue);
                    switch (index) {
                        case 0:
                            pastWrap1Date = document.getTimestamp("timeStamp");
                            pastWrap1.setText(filteredValue);
                            break;
                        case 1:
                            pastWrap2Date = document.getTimestamp("timeStamp");
                            pastWrap2.setText(filteredValue);
                            break;
                        case 2:
                            pastWrap3Date = document.getTimestamp("timeStamp");
                            pastWrap3.setText(filteredValue);
                            break;
                    }
                    index++;
                }
            }
        }).addOnFailureListener(e -> {
            // Handle failures
            Log.e("Firestore", "Error getting documents", e);
        });

        pastWrap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirstActivity.setWrapDate(pastWrap1Date);
                Intent intent = new Intent(LoggedInActivity.this, FirstActivity.class);
                startActivity(intent);
            }
        });
        pastWrap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirstActivity.setWrapDate(pastWrap2Date);
                Intent intent = new Intent(LoggedInActivity.this, FirstActivity.class);
                startActivity(intent);
            }
        });
        pastWrap3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirstActivity.setWrapDate(pastWrap3Date);
                Intent intent = new Intent(LoggedInActivity.this, FirstActivity.class);
                startActivity(intent);
            }
        });



        //settings button is to go to generate a new wrap page
        settingsBtn = findViewById(R.id.accountBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoggedInActivity.this, MyWrapsActivity.class);
                startActivity(intent);
            }
        });

        // Add any initialization or setup code for your logged-in view

        // Initialize the views
        //tokenTextView = (TextView) findViewById(R.id.token_text_view);
        //codeTextView = (TextView) findViewById(R.id.code_text_view);
        //profileTextView = (TextView) findViewById(R.id.response_text_view);

        // Initialize the buttons
        //Button profileBtn = (Button) findViewById(R.id.profile_btn);

        onGetUserProfileClicked();

        profileImage = findViewById(R.id.profileImage);
        welcomeMessageName = findViewById(R.id.welcomeMessageName);
    }

    public void logoutOnClick(View view) {
        // Clear the access token
        AuthActivity.accessToken = null;
        // Launch the Spotify logout URL in a browser with the custom redirect URI
        String logoutUrl = "https://accounts.spotify.com/logout?continue=spotify-wrapped://logout";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(logoutUrl));
        startActivity(intent);
        finish();
    }

    public void onGetUserProfileClicked() {
        if (AuthActivity.accessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        //setTextAsync(mAccessToken, tokenTextView);
        //setTextAsync(mAccessCode, codeTextView);

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(LoggedInActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            private void loadImage(String imageUrl, ImageView imageView) {
                Picasso.get().load(imageUrl).into(imageView);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    final String jsonData = jsonObject.toString(3);
                    //get profile image
                    JSONArray imagesArray = jsonObject.getJSONArray("images");
                    JSONObject firstImage = imagesArray.getJSONObject(0);
                    String imageUrl = firstImage.getString("url");

                    //get username
                    String displayName = jsonObject.getString("display_name");
                    int followerCount = jsonObject.getJSONObject("followers").getInt("total");
                    String followerCountStr = String.valueOf(followerCount);
                    // Run UI-related operation on the main UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //setTextAsync(jsonData, profileTextView);
                            loadImage(imageUrl, profileImage);
                            setTextAsync("Welcome, " + displayName + "!", welcomeMessageName);
                        }
                    });
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);

                    // Show Toast message on the main UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoggedInActivity.this, "Failed to parse data, watch Logcat for more details",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        });
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
