package com.example.guessthecelebrity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guessthecelebrity.CelebRepository;
import com.example.guessthecelebrity.MainActivity;
import com.example.guessthecelebrity.R;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashActivity extends AppCompatActivity {

    boolean animationDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashImage = findViewById(R.id.splashImage);

        splashImage.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_in)
        );

        splashImage.animate()
                .alpha(1f)
                .setDuration(2500)
                .withEndAction(() -> {
                    animationDone = true;
                    goNextIfReady();
                })
                .start();

        // 🔥 START REAL DOWNLOAD HERE
        new DownloadCelebsTask().execute(
                "https://www.imdb.com/list/ls051438203/"
        );
    }

    void goNextIfReady() {
        if (animationDone && CelebRepository.isLoaded) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    class DownloadCelebsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data;
                while ((data = reader.read()) != -1) {
                    result.append((char) data);
                }
            } catch (Exception e) {
                return null;
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            if (result == null) return;

            Pattern pImage = Pattern.compile(
                    "src=\"(https://m\\.media-amazon\\.com/images/[^\"]+\\.jpg)\""
            );
            Matcher mImage = pImage.matcher(result);

            while (mImage.find()) {
                CelebRepository.imageUrls.add(mImage.group(1));
            }

            Pattern pName = Pattern.compile("alt=\"([^\"]+)\"");
            Matcher mName = pName.matcher(result);

            while (mName.find()) {
                CelebRepository.names.add(mName.group(1));
            }

            CelebRepository.isLoaded = true;
            goNextIfReady();
        }
    }
}
