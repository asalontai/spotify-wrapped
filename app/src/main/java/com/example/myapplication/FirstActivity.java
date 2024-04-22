package com.example.myapplication;

import static android.content.ContentValues.TAG;

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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class FirstActivity extends AppCompatActivity {

    private TextView text1, text2;
    private ImageView image1, image2;

    private Button next1;
    private MediaPlayer m;
    public static Timestamp wrapDate;

    public static void setWrapDate(Timestamp wrapDates) {
        wrapDate = wrapDates;
    }

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


        if (artistNames == null && artistImageUrls == null && trackNames == null && trackImageUrls == null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference collectionRef = db.collection("wraps");
            // Perform a query to retrieve documents from the collection
            collectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                // Iterate over the documents
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Timestamp dateStamp = document.getTimestamp("timeStamp");
                    if (dateStamp != null && dateStamp.equals(wrapDate)) {
                        // Fetch artistNames and trackNames arrays from the wrapJson field
                        String wrapJson = document.getString("wrapJson");
                        if (wrapJson != null) {
                            JSONObject wrapJsonObject = null;
                            try {
                                wrapJsonObject = new JSONObject(wrapJson);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            JSONArray artistNamesJsonArray = null;
                            try {
                                artistNamesJsonArray = wrapJsonObject.getJSONArray("artistNames");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            JSONArray trackNamesJsonArray = null;
                            try {
                                trackNamesJsonArray = wrapJsonObject.getJSONArray("trackNames");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            String[] artistNames1 = new String[artistNamesJsonArray.length()];
                            // Convert artistNames JSONArray to a String array
                            for (int i = 0; i < artistNamesJsonArray.length(); i++) {
                                try {
                                    artistNames1[i] = artistNamesJsonArray.getString(i);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // Convert trackNames JSONArray to a String array
                            String[] trackNames1 = new String[artistNamesJsonArray.length()];
                            for (int i = 0; i < trackNamesJsonArray.length(); i++) {
                                try {
                                    trackNames1[i] = trackNamesJsonArray.getString(i);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            String artistImageURLsString = document.getString("artistImageURLs");
                            String trackImageURLsString = document.getString("trackImageURLs");
                            // Fetch artistImageURLs and trackImageURLs directly from the document
                            artistImageURLsString = artistImageURLsString.replaceAll("\\[|\\]|\"", "");
                            trackImageURLsString = trackImageURLsString.replaceAll("\\[|\\]|\"", "");
                            String[] artistImageUrls1 = artistImageURLsString.split(",\\s*");
                            String[] trackImageUrls1 = trackImageURLsString.split(",\\s*");

                            loadImage(trackImageUrls1[0], image1);
                            text1.setText(trackNames1[0]);
                            loadImage(artistImageUrls1[0], image2);
                            text2.setText(artistNames1[0]);
                        }
                    }
                }
            }).addOnFailureListener(e -> {
                // Handle failure
                Log.e(TAG, "Error getting documents: ", e);
            });
        } else {
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
