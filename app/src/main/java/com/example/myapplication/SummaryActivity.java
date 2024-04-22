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

public class SummaryActivity extends AppCompatActivity {

    private TextView st1, st2, st3, st4, st5, st6, st7, st8, st9, st10;
    private ImageView si1, si2, si3, si4, si5, si6, si7, si8, si9, si10;

    private Button next6, genButton;
    private MediaPlayer m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        st1 = findViewById(R.id.st1);
        st2 = findViewById(R.id.st2);
        st3 = findViewById(R.id.st3);
        st4 = findViewById(R.id.st4);
        st5 = findViewById(R.id.st5);
        st6 = findViewById(R.id.st6);
        st7 = findViewById(R.id.st7);
        st8 = findViewById(R.id.st8);
        st9 = findViewById(R.id.st9);
        st10 = findViewById(R.id.st10);

        si1 = findViewById(R.id.si1);
        si2 = findViewById(R.id.si2);
        si3 = findViewById(R.id.si3);
        si4 = findViewById(R.id.si4);
        si5 = findViewById(R.id.si5);
        si6 = findViewById(R.id.si6);
        si7 = findViewById(R.id.si7);
        si8 = findViewById(R.id.si8);
        si9 = findViewById(R.id.si9);
        si10 = findViewById(R.id.si10);

        next6 = findViewById(R.id.next6);

        genButton = findViewById(R.id.Gem);

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
                    if (dateStamp != null && dateStamp.equals(FirstActivity.wrapDate)) {
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

                            loadImage(trackImageUrls1[0], si1);
                            loadImage(trackImageUrls1[1], si2);
                            loadImage(trackImageUrls1[2], si3);
                            loadImage(trackImageUrls1[3], si4);
                            loadImage(trackImageUrls1[4], si5);
                            loadImage(artistImageUrls1[0], si6);
                            loadImage(artistImageUrls1[1], si7);
                            loadImage(artistImageUrls1[2], si8);
                            loadImage(artistImageUrls1[3], si9);
                            loadImage(artistImageUrls1[4], si10);
                            st1.setText(trackNames1[0]);
                            st2.setText(trackNames1[1]);
                            st3.setText(trackNames1[2]);
                            st4.setText(trackNames1[3]);
                            st5.setText(trackNames1[4]);
                            st6.setText(artistNames1[0]);
                            st7.setText(artistNames1[1]);
                            st8.setText(artistNames1[2]);
                            st9.setText(artistNames1[3]);
                            st10.setText(artistNames1[4]);
                        }
                    }
                }
            }).addOnFailureListener(e -> {
                // Handle failure
                Log.e(TAG, "Error getting documents: ", e);
            });
        } else {
            loadImage(trackImageUrls[0], si1);
            loadImage(trackImageUrls[1], si2);
            loadImage(trackImageUrls[2], si3);
            loadImage(trackImageUrls[3], si4);
            loadImage(trackImageUrls[4], si5);
            loadImage(artistImageUrls[0], si6);
            loadImage(artistImageUrls[1], si7);
            loadImage(artistImageUrls[2], si8);
            loadImage(artistImageUrls[3], si9);
            loadImage(artistImageUrls[4], si10);
            st1.setText(trackNames[0]);
            st2.setText(trackNames[1]);
            st3.setText(trackNames[2]);
            st4.setText(trackNames[3]);
            st5.setText(trackNames[4]);
            st6.setText(artistNames[0]);
            st7.setText(artistNames[1]);
            st8.setText(artistNames[2]);
            st9.setText(artistNames[3]);
            st10.setText(artistNames[4]);
        }


        next6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, LoggedInActivity.class);
                // Pass data as extras to the intent
                intent.putExtra("artistNames", artistNames);
                intent.putExtra("artistImageUrls", artistImageUrls);
                intent.putExtra("trackNames", trackNames);
                intent.putExtra("trackImageUrls", trackImageUrls);
                intent.putExtra("listeningHistory", listeningHistory);
                startActivity(intent);
            }
        });

        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, LLMActivity.class);
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
