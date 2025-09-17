package com.example.ebookreader.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ebookreader.R;
import com.example.ebookreader.databinding.ActivityPdfReaderBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PdfReaderActivity extends AppCompatActivity {
    private ActivityPdfReaderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Router
        setupBottomNavigation();

        String url = getIntent().getStringExtra("URL");
        if (url != null) {
            new Thread(() -> {
                try {
                    PDFView pdfView = binding.pdfView;
                    pdfView.fromUri(android.net.Uri.parse(url))
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .load();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    //Router
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