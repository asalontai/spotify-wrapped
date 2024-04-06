package com.example.myapplication;


//Android Importations
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

//Spotify Importations
import com.example.myapplication.LoggedInActivity;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;


//JSON Importations
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

//Exception Importations
import java.io.IOException;

//HTTP Importations
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class AuthActivity extends AppCompatActivity {

    // Define your client ID and redirect URI
    private static final String CLIENT_ID = "c6c05fc4b9de4437a16fab409bd6953a";
    private static final String REDIRECT_URI = "spotify-wrapped://callback";

    // Request code for authentication
    private static final int AUTH_REQUEST_CODE = 123;

    //I think that this might only be used for the LoginActivity function but we use the openLoginBrowser function
    public static final int AUTH_TOKEN_REQUEST_CODE = 0;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    private String mAccessToken;

    private Call mCall;

    private TextView tokenTextView, responseTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //aftermath of the log-in
        setContentView(R.layout.activity_logged_in);

        //Initialize views
        tokenTextView = (TextView) findViewById(R.id.token_text_view);
        responseTextView = (TextView) findViewById(R.id.response_text_view);


        // Start the authentication process
        startAuthentication();



    }


    private void startAuthentication() {
        // Construct the authentication request
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private"});
        builder.setShowDialog(true); // Show dialog for log out option
        builder.setCampaign("your-campaign-token");
        AuthorizationRequest request = builder.build();

        // Open a web browser for authentication
        AuthorizationClient.openLoginInBrowser(this, request);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();



        if (uri != null) {
            AuthorizationResponse response = AuthorizationResponse.fromUri(uri);

            Log.d("This is the response", String.valueOf(response));

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    mAccessToken = response.getAccessToken();

                    setTextAsync("This is the access token from the response : " + mAccessToken, tokenTextView);
                    // Start LoggedInActivity
                    Intent loggedInIntent = new Intent(this, LoggedInActivity.class);

                    //set the token to the loggedInActivity so that it can be displayed to the users



                    startActivity(loggedInIntent);
                    // Finish AuthActivity to prevent returning to it with back button
                    finish();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    String errorMessage = response.getError();
                    // Show an error message to the user - throw a Log.d function for the error
                    Log.d("This is the error message", errorMessage);
                    // Handle the error gracefully without redirecting to MainActivity
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d("The flow was cancelled", null);
                    break;
            }
        } else
        {
            Log.d("There is no valid uri, what do i do", null);
        }
    }


    /**
     * Creates a UI thread to update a TextView in the background
     * Reduces UI latency and makes the system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }


}