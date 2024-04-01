package com.example.auditlogpromptsandresponses;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText promptEditText;
    TextView response;
    ProgressBar progressBar;
    Button save;
    Button viewLogs;
    private String promptTime= null;
    private String responseTime = null;
    DatabaseHelper databaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        promptEditText = findViewById(R.id.prompt);
        Button send = findViewById(R.id.send);
        Button cancel = findViewById(R.id.cancel);
        response = findViewById(R.id.response);
        progressBar = findViewById(R.id.progressBar);
        save = findViewById(R.id.save);
        viewLogs = findViewById(R.id.viewLogs);
        databaseHelper = new DatabaseHelper(this);


        send.setOnClickListener(view -> {
            String prompt = promptEditText.getText().toString().trim();
            promptTime = getCurrentDateTime();
            if (!prompt.isEmpty()) {
                // Send prompt to OpenAI API
                progressBar.setVisibility(View.VISIBLE);
                new GenerateResponseTask().execute(prompt);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a prompt", Toast.LENGTH_SHORT).show();
            }
        });

        cancel.setOnClickListener(view -> {
            promptEditText.setText("");
            response.setText("");
        });

        save.setOnClickListener(view -> {
            String prompt = promptEditText.getText().toString().trim();
            String responseText = response.getText().toString().trim();
            if (!prompt.isEmpty() && !responseText.isEmpty()) {
                // Save prompt and response
                databaseHelper.addAuditPrompt(promptTime, prompt);
                databaseHelper.addResponse(responseTime, responseText);
                Toast.makeText(MainActivity.this, "Prompt and response saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Prompt or response is empty", Toast.LENGTH_SHORT).show();
            }
        });

        viewLogs.setOnClickListener(view -> {
            // Fetch logs from both tables
            String auditPromptLogs = databaseHelper.getAllAuditPromptLogs();
            String responseLogs = databaseHelper.getAllResponseLogs();

            // Display logs
            if (auditPromptLogs != null && responseLogs != null) {
                String allLogs = "Audit Prompt Logs:\n" + auditPromptLogs + "\n\nResponse Logs:\n" + responseLogs;
                // You can display the logs however you want, such as in a dialog or a TextView
                showDialog("Logs", allLogs);
            } else {
                Toast.makeText(MainActivity.this, "Error fetching logs", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private class GenerateResponseTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String prompt = params[0];
            String apiKey = "";
            String apiUrl = "https://api.openai.com/v1/completions";
            String model = "gpt-3.5-turbo-instruct";

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("prompt", prompt);
                jsonRequest.put("model", model);
                jsonRequest.put("max_tokens", 50);

                conn.setDoOutput(true);
                conn.getOutputStream().write(jsonRequest.toString().getBytes(StandardCharsets.UTF_8));

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();

            } catch (IOException | JSONException e) {
                Log.e("EXCEPTION",e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String generatedText = jsonResponse.getJSONArray("choices").getJSONObject(0).getString("text");
                    responseTime = getCurrentDateTime();
                    response.setText(generatedText);
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    Log.e("EXCEPTION",e.toString());
                    response.setText("Error parsing response");
                }
            } else {
                response.setText("Error connecting to server");
            }
        }
    }

    private void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
