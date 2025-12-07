package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> imageUrls = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();

    // Add this method inside the MainActivity class definition:
    public void celebChosen(View view) {
        // Required to prevent the app from crashing if your layout XML uses android:onClick="celebChosen"

        // Game logic for checking the answer will go here later.
    }

    // Helper method to extract the first IMDb Image URL found
    public static String extractImageUrl(String text) {
        String regex = "src=\"(https://m\\.media-amazon\\.com/images/[^\"]+\\.jpg)\"";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Helper method to extract the first celebrity name found
    public static String extractName(String text) {
        String regex = "alt=\"([^\"]+)\"";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    // ----------------------------------------------------------------------
    // DownloadTask: Handles background network operation and UI updates
    // ----------------------------------------------------------------------

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // Runs on a background thread
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Add timeouts for robustness (optional but recommended)
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1) {
                    result.append((char) data);
                    data = reader.read();
                }
                return result.toString();

            } catch (Exception e) {
                Log.e("DownloadTask", "Network or IO error: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Runs on the Main/UI Thread after doInBackground is complete.
            imageUrls.clear();
            names.clear();

            // Check for download failure FIRST
            if (result == null) {
                return;
            }

            Log.i("Contents of URL", "Download complete. Starting parsing.");

            try {
                // We will skip the initial split and search the ENTIRE result,
                // as the split point might also be changing. Searching the whole string
                // with specific patterns is sometimes more reliable.
                String content = result;

                // -----------------------------------------------------------
                // 1. Extract and STORE ALL image URLs
                //    (Pattern looks for 'loadlate', the attribute IMDb uses for lazy-loaded images)
                // -----------------------------------------------------------
                Pattern pImage = Pattern.compile("src=\"(https://m\\.media-amazon\\.com/images/[^\"]+\\.jpg)\"");
                Matcher mImage = pImage.matcher(content);

                while (mImage.find()) {
                    imageUrls.add(mImage.group(1)); // Store the image URL
                }

                // -----------------------------------------------------------
                // 2. Extract and STORE ALL names
                //    (Pattern looks for the name inside the <h3> link with its specific class)
                // -----------------------------------------------------------
                // [^<]+ matches the name (any character except '<')
                Pattern pName = Pattern.compile("alt=\"([^\"]+)\"");
                Matcher mName = pName.matcher(content);

                while (mName.find()) {
                    names.add(mName.group(1).trim()); // Store and trim the name
                }

                // check foe the number are equal in both
                // Ensure the lists match
                if (names.size() > imageUrls.size()) {
                    names.remove(names.size() - 1); // Remove the extra name (usually "Get the IMDb App")
                }

                try {
                    ImageView imageView = findViewById(R.id.imageView);
                    Random random = new Random();
                    int celebIndex = random.nextInt(imageUrls.size()); // random celebrity

                    // Download the image from URL
                    new AsyncTask<String, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(String... urls) {
                            try {
                                URL url = new URL(urls[0]);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setDoInput(true);
                                connection.connect();
                                InputStream input = connection.getInputStream();
                                return android.graphics.BitmapFactory.decodeStream(input);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    }.execute(imageUrls.get(celebIndex));

                } catch (Exception e) {
                    e.printStackTrace();
                }


                Random random = new Random();
                ImageView imageView = findViewById(R.id.imageView);
                int celebIndex = random.nextInt(names.size()); // Random index for the celebrity image
                imageView.setImageBitmap(bitmaps.get(celebIndex)); // Set the image



                // -----------------------------------------------------------
                // 3. Final Toast to confirm the count
                // -----------------------------------------------------------

                for (int i = 0; i < names.size(); i++) {
                    Log.d("Celebrity:", "Name: " + names.get(i));
                    Log.d("Celebrity:", "Image URL: " + imageUrls.get(i) );
                }



                Log.i("DataCount", "Images: " + imageUrls.size() + ", Names: " + names.size());

            } catch (Exception e) {
                Log.e("ParseError", "Error during content parsing: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ----------------------------------------------------------------------
    // onCreate: Initiates the background task and nothing else
    // ----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🛑 CRITICAL FIX: DO NOT USE .get()
        // We simply execute the task and let onPostExecute handle the result.
        DownloadTask task = new DownloadTask();
        task.execute("https://www.imdb.com/list/ls051438203/");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
    }
}