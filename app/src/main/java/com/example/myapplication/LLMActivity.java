package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.common.shared.HarmBlockThreshold;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LLMActivity extends AppCompatActivity {

    private static final String TAG = "LLMActivity";
    private static final String MODEL_NAME = "gemini-pro"; // Change this to the appropriate model name

    // Access your API key as a Build Configuration variable
    private String apiKey = "AIzaSyCrLmZQjP11qVRdNx_lAq7c4T4i96NW2E8";
    private Button backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llm);

        backButton = findViewById(R.id.backButton);

        String[] artistNames = getIntent().getStringArrayExtra("artistNames");
        String[] artistImageUrls = getIntent().getStringArrayExtra("artistImageUrls");
        String[] trackNames = getIntent().getStringArrayExtra("trackNames");
        String[] trackImageUrls = getIntent().getStringArrayExtra("trackImageUrls");
        String listeningHistory = getIntent().getStringExtra("listeningHistory");
        // Initialize the GenerativeModel
        GenerativeModel gm = new GenerativeModel(MODEL_NAME, apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Create a content object with text-only input
        Content content = new Content.Builder()
                .addText("Given this listening history, write 5 character traits about the person. Start your response with 'Based on your listening history, you are...', and after listing the character traits, in a new line, recommend a few real artists related to those in the listening history." + listeningHistory)
                .build();
        Log.d("content: ", listeningHistory);
        // Create an executor
        Executor executor = Executors.newSingleThreadExecutor();

        // Generate content using the GenerativeModel
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // Handle successful generation
                String resultText = result.getText();
                Log.d(TAG, "Generated Text: " + resultText);
                // Update UI or perform further operations with generated text
                updateGeneratedText(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                // Handle failure
                t.printStackTrace();
                // Handle error or inform user about failure
            }
        }, executor);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LLMActivity.this, SummaryActivity.class);
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

    // Method to update the generated text in the TextView
    private void updateGeneratedText(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView generatedTextView = findViewById(R.id.generatedText);
                generatedTextView.setText(text);
            }
        });
    }

}
