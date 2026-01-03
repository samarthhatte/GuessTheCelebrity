package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> imageUrls = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();

    ImageView imageView;
    Button button1, button2, button3, button4;

    int correctAnswerIndex;
    String[] answers = new String[4];
    int chosenCeleb = 0;

    public void celebChosen(View view) {
        // game logic later
        if (view.getTag().toString().equals(Integer.toString(correctAnswerIndex))) {
            // correct answer
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! It was " + names.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        loadNewQuestion();
    }

    class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1) {
                    result.append((char) data);
                    data = reader.read();
                }

                return result.toString();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Pattern pImage = Pattern.compile("src=\"(https://m\\.media-amazon\\.com/images/[^\"]+\\.jpg)\"");
            Matcher mImage = pImage.matcher(result);

            while (mImage.find()) {
                imageUrls.add(mImage.group(1));
            }

            Pattern pName = Pattern.compile("alt=\"([^\"]+)\"");
            Matcher mName = pName.matcher(result);

            while (mName.find()) {
                names.add(mName.group(1));
            }

            if (names.size() > imageUrls.size()) {
                names.remove(names.size() - 1);
            }

            loadNewQuestion();
        }
    }

    void loadNewQuestion() {
        Random random = new Random();
        chosenCeleb = random.nextInt(imageUrls.size());

//        imageView.setImageResource(R.mipmap.ic_launcher);
//        imageView.setAlpha(1f);
        imageView.setImageDrawable(null);

        new DownloadImageTask().execute(imageUrls.get(chosenCeleb));

        correctAnswerIndex = random.nextInt(4);

        for (int i = 0; i < 4; i++) {
            if (i == correctAnswerIndex) {
                answers[i] = names.get(chosenCeleb);
            } else {
                int wrong;
                do {
                    wrong = new Random().nextInt(names.size());
                } while (wrong == chosenCeleb);

                answers[i] = names.get(wrong);
            }
        }

        button1.setText(answers[0]);
        button2.setText(answers[1]);
        button3.setText(answers[2]);
        button4.setText(answers[3]);
    }


    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {

                // Set image
                imageView.setImageBitmap(bitmap);

                // Fade-in animation
//                imageView.setAlpha(0f);
//                imageView.animate()
//                        .alpha(1f)
//                        .setDuration(400)
//                        .start();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        imageView = findViewById(R.id.imageView);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        imageUrls = CelebRepository.imageUrls;
        names = CelebRepository.names;

        loadNewQuestion();

//        new DownloadTask().execute("https://www.imdb.com/list/ls051438203/");

    }
}
