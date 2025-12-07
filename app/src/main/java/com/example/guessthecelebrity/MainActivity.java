package com.example.guessthecelebrity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // Extract image URL
    public static String extractImageUrl(String text) {
        String regex = "src=\"(https://stat\\d+\\.bollywoodhungama\\.in/wp-content/uploads/\\d{4}/\\d{2}/[^\\s\"]+\\.jpg)\"";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Extract name (from alt="")
    public static String extractName(String text) {
        String regex = "alt=\"([^\"]+)\"";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1) {    // FIXED: -1 means end of stream
                    result.append((char) data);
                    data = reader.read();
                }
                return result.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            result = task.execute("https://www.bollywoodhungama.com/celebrities/top-100/").get();
            Log.i("Contents of URL", result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != null) {
            String imageUrl = extractImageUrl(result);
            String name = extractName(result);

            if (imageUrl != null)
                Toast.makeText(MainActivity.this, imageUrl, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "No Image URL Found", Toast.LENGTH_SHORT).show();

            if (name != null)
                Toast.makeText(MainActivity.this, "Celebrity Name: " + name, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "No Name Found", Toast.LENGTH_SHORT).show();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
