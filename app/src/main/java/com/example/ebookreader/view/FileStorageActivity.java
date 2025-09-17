package com.example.ebookreader.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebookreader.R;
import com.example.ebookreader.adapter.FileAdapter;
import com.example.ebookreader.databinding.ActivityFileStorageBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FileStorageActivity extends AppCompatActivity {
    private ActivityFileStorageBinding binding;
    private FileAdapter fileAdapter;
    private final List<File> fileList = new ArrayList<>();
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.ebookreader.fileprovider";

    private final ActivityResultLauncher<String[]> pickFileLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    handleFileSelection(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileStorageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup BottomNavigationView
        setupBottomNavigation();

        // Initialize RecyclerView
        fileAdapter = new FileAdapter(fileList, file -> {
            if (file.getName().endsWith(".pdf")) {
                openPdfFile(file);
            } else if (file.getName().endsWith(".txt")) {
                openTxtFile(file);
            }
        });
        binding.rvFiles.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFiles.setAdapter(fileAdapter);

        // Load existing files
        loadFiles();

        // Setup upload button
        binding.btnUpload.setOnClickListener(v -> {
            pickFileLauncher.launch(new String[]{"application/pdf", "text/plain"});
        });
    }

    private void loadFiles() {
        File dir = getFilesDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".pdf") || name.endsWith(".txt"));
        if (files != null) {
            fileList.clear();
            fileList.addAll(Arrays.asList(files));
            fileAdapter.notifyDataSetChanged();
        }
    }

    private void handleFileSelection(Uri uri) {
        try {
            String fileName = getFileNameFromUri(uri);
            if (fileName == null || (!fileName.endsWith(".pdf") && !fileName.endsWith(".txt"))) {
                Toast.makeText(this, "Please select a PDF or TXT file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Copy file to internal storage
            File file = new File(getFilesDir(), fileName);
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // Update file list
            fileList.add(file);
            fileAdapter.notifyItemInserted(fileList.size() - 1);
            Toast.makeText(this, "File uploaded: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error uploading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        }
        return fileName;
    }

    private void openPdfFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTxtFile(File file) {
        Intent intent = new Intent(this, TextViewerActivity.class);
        intent.putExtra("FILE_PATH", file.getAbsolutePath());
        startActivity(intent);
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
                return true;
            }
            return false;
        });
    }
}