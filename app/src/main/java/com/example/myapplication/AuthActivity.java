package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    // Define your client ID and redirect URI
    private static final String CLIENT_ID = "c6c05fc4b9de4437a16fab409bd6953a";
    private static final String REDIRECT_URI = "spotify-wrapped://callback";

    // Request code for authentication
    private static final int AUTH_REQUEST_CODE = 123;
    public static String accessToken;
    public static String accessCode;

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
        builder.setScopes(new String[]{"user-top-read", "playlist-read-private"});
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
                            String userName = response.getString("display_name");

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
        // Create a new document in the "users" collection with the user's name
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", userName);
        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .add(userInfo)
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
    }
}