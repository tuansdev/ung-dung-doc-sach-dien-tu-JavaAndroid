package com.example.ebookreader.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ebookreader.R;
import com.example.ebookreader.databinding.ActivityTextViewerBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TextViewerActivity extends AppCompatActivity {
    private ActivityTextViewerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup BottomNavigationView
        setupBottomNavigation();

        String filePath = getIntent().getStringExtra("FILE_PATH");

        if (filePath != null) {
            try (FileInputStream fis = new FileInputStream(filePath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                binding.tvContent.setText(content.toString());
            } catch (IOException e) {
                Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.nav_books);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, BookListActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_books) {
                startActivity(new Intent(this, FileStorageActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}