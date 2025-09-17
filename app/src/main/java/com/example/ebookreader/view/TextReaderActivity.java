package com.example.ebookreader.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ebookreader.R;
import com.example.ebookreader.databinding.ActivityPdfReaderBinding;
import com.example.ebookreader.databinding.ActivityTextReaderBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class TextReaderActivity extends AppCompatActivity {
    private ActivityTextReaderBinding binding;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Router
        setupBottomNavigation();

        tvContent = findViewById(R.id.tvContent);

        String url = getIntent().getStringExtra("URL");
        if (url != null) {
            new Thread(() -> {
                try {
                    URL bookUrl = new URL(url);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(bookUrl.openStream()));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    reader.close();

                    runOnUiThread(() -> tvContent.setText(content.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> tvContent.setText("Không thể tải nội dung."));
                }
            }).start();
        } else {
            tvContent.setText("Không có URL nội dung.");
        }
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_books) {
                startActivity(new Intent(this, FileStorageActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }
}
