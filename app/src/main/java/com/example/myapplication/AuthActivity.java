package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
public class AuthActivity extends AppCompatActivity {

    // Define your client ID and redirect URI
    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    private Call caCall, ctCall, saCall, stCall, yaCall, ytCall;
    private int completedCalls = 0;
    private String[] artistNames = new String[5];
    private String[] artistImageUrls = new String[5];

    private String[] trackNames = new String[5];
    private String[] trackImageUrls = new String[5];
    private static final String CLIENT_ID = "c6c05fc4b9de4437a16fab409bd6953a";
    private static final String REDIRECT_URI = "spotify-wrapped://callback";

    // Request code for authentication
    private static final int AUTH_REQUEST_CODE = 123;
    public static String accessToken;
    public static String accessCode;

    public static String userName;

    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if the access token is null
        if (accessToken == null) {
            // Start the authentication process
            startAuthentication();
        } else {
            // Start LoggedInActivity if the access token is not null
            Intent loggedInIntent = new Intent(this, LoggedInActivity.class);
            startActivity(loggedInIntent);
            finish(); // Finish AuthActivity to prevent returning to it with back button
        }
    }

    private void startAuthentication() {
        // Construct the authentication request
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming", "user-top-read", "playlist-read-private"});
        builder.setShowDialog(true); // Show dialog for log out option
        AuthorizationRequest request = builder.build();

        // Open a web browser for authentication
        AuthorizationClient.openLoginInBrowser(this, request);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            // Check if the callback URL matches the logout URL
            if ("spotify-wrapped".equals(uri.getScheme()) && "logout".equals(uri.getHost())) {
                // This is the logout callback URL, handle it appropriately
                // For example, navigate back to the main page
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                finish(); // Optionally finish this activity
            } else {
                // Handle the authentication response
                AuthorizationResponse response = AuthorizationResponse.fromUri(uri);

                switch (response.getType()) {
                    // Response was successful and contains auth token
                    case TOKEN:
                        // Handle successful response
                        accessToken = response.getAccessToken();
                        accessCode = response.getCode();
                        completedCalls = 0; // Reset completed calls counter
                        getTopTracks("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=5&offset=0", ctCall); // Pass the callback
                        getArtist("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=5&offset=0", caCall); // Pass the callback
                        // Fetch user information and store it in Firestore
                        fetchAndStoreUserInfo(accessToken);
                        // Start LoggedInActivity
                        Intent loggedInIntent = new Intent(this, LoggedInActivity.class);
                        startActivity(loggedInIntent);
                        // Finish AuthActivity to prevent returning to it with back button
                        finish();
                        break;

                    // Auth flow returned an error
                    case ERROR:
                        // Handle error response
                        String errorMessage = response.getError();
                        // Show an error message to the user
                        // Handle the error gracefully without redirecting to MainActivity
                        break;

                    // Most likely auth flow was cancelled
                    default:
                        break;
                }
            }
        }
    }

    private void fetchAndStoreUserInfo(String accessToken) {
        // Construct the Spotify API request URL
        String apiUrl = "https://api.spotify.com/v1/me";

        // Make a request to the Spotify API to fetch user information
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse the response JSON to extract user information
                            userName = response.getString("display_name");

                            // Store user information in Firestore
                            storeUserInfoInFirestore(userName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                // Set the authorization header with the access token
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        // Add the request to the RequestQueue
        queue.add(request);
    }

    private void storeUserInfoInFirestore(String userName) {
        // Get a reference to the "users" collection in Firestore
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");

        // Check if a document with the user's name already exists
        usersCollection.whereEqualTo("name", userName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // If no documents with the user's name exist, add the user to Firestore
                            if (task.getResult().isEmpty()) {
                                Map<String, Object> userInfo = new HashMap<>();
                                userInfo.put("name", userName);
                                // Add user's top artists and top tracks to the userInfo map
                                for (int i = 0; i < 5; i++) {
                                    userInfo.put("topArtist" + i, artistNames[i]);
                                    userInfo.put("topArtistImageUrl" + i, artistImageUrls[i]);
                                    userInfo.put("topTrack" + i, trackNames[i]);
                                    userInfo.put("topTrackImageUrl" + i, trackImageUrls[i]);
                                }

                                usersCollection.add(userInfo)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d("Firestore", "User information stored successfully!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Firestore", "Error storing user information", e);
                                            }
                                        });
                            } else {
                                Log.d("Firestore", "User already exists in the database.");
                            }
                        } else {
                            Log.w("Firestore", "Error getting documents.", task.getException());
                        }
                    }
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
        final okhttp3.Request request = new okhttp3.Request.Builder()
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
                    AuthActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("HTTP", "Failed to fetch data: " + e);
                            Toast.makeText(AuthActivity.this, "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
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
                        //onDataFetched(); // Call callback
                        Log.d("HTTP", "after fetched1");
                    } catch (JSONException e) {
                        Log.d("JSON", "Failed to parse data: " + e);
                        runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Failed to parse data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Handle unsuccessful response
                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Unsuccessful response", Toast.LENGTH_SHORT).show());
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
        final okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + AuthActivity.accessToken)
                .build();

        cancelCall(call);
        call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    AuthActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("HTTP", "Failed to fetch data: " + e);
                            Toast.makeText(AuthActivity.this, "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
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
                        //onDataFetched(); // Call callback
                        Log.d("HTTP", "after fetched2");
                    } catch (JSONException e) {
                        Log.d("JSON", "Failed to parse data: " + e);
                        runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Failed to parse data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Handle unsuccessful response
                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Unsuccessful response", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

//    private void loadImage(String imageUrl, ImageView imageView) {
//        Picasso.get().load(imageUrl).into(imageView);
//    }
//
//    private void onDataFetched() {
//        if (completedCalls == 2) {
//            runOnUiThread(() -> {
//                // Update UI elements with artist and track data
//                text1.setText(artistNames[0]);
//                text2.setText(artistNames[1]);
//                text3.setText(artistNames[2]);
//                text4.setText(artistNames[3]);
//                text5.setText(artistNames[4]);
//
//                loadImage(artistImageUrls[0], image1);
//                loadImage(artistImageUrls[1], image2);
//                loadImage(artistImageUrls[2], image3);
//                loadImage(artistImageUrls[3], image4);
//                loadImage(artistImageUrls[4], image5);
//
//                text6.setText(trackNames[0]);
//                text7.setText(trackNames[1]);
//                text8.setText(trackNames[2]);
//                text9.setText(trackNames[3]);
//                text10.setText(trackNames[4]);
//
//                loadImage(trackImageUrls[0], image6);
//                loadImage(trackImageUrls[1], image7);
//                loadImage(trackImageUrls[2], image8);
//                loadImage(trackImageUrls[3], image9);
//                loadImage(trackImageUrls[4], image10);
//            });
//        }
//    }
//
    private void cancelCall(Call mCall) {
        if (mCall != null) {
            mCall.cancel();
        }
    }
//
//    @Override
//    protected void onDestroy() {
//        cancelCall(stCall);
//        super.onDestroy();
//    }

}