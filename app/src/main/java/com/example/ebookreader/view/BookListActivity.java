package com.example.ebookreader.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ebookreader.R;
import com.example.ebookreader.databinding.ActivityBookListBinding;
import com.example.ebookreader.viewmodel.BookViewModel;
import com.example.ebookreader.adapter.BookAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BookListActivity extends AppCompatActivity {
    private ActivityBookListBinding binding;
    private BookViewModel viewModel;
    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Router
        setupBottomNavigation();



        viewModel = new ViewModelProvider(this).get(BookViewModel.class);
        adapter = new BookAdapter(book -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra("BOOK", book);
            startActivity(intent);
        });

        binding.rvBooks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBooks.setAdapter(adapter);

        // Setup categories (mapped to topics)
        String[] categories = {"All", "Fiction", "Non-fiction", "Science", "History"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        // Lắng nghe sự kiện thay đổi danh mục
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String topic = getTopicFromCategory(categories[position]);
                String query = binding.etSearch.getText().toString();
                viewModel.getBooks(1, query, topic).observe(BookListActivity.this, books -> adapter.setBooks(books));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì khi không chọn
            }
        });

        // Search and filter
        binding.btnSearch.setOnClickListener(v -> {
            String query = binding.etSearch.getText().toString();
            String category = binding.spinnerCategory.getSelectedItem().toString();
            if (!category.equals("All")) {
                viewModel.getBooks(1, query, category).observe(this, books -> adapter.setBooks(books));
            } else {
                viewModel.getBooks(1, query, "").observe(this, books -> adapter.setBooks(books));
            }
        });

        // Tải sách mặc định khi khởi động
        viewModel.getBooks(1, "", "").observe(this, books -> adapter.setBooks(books));
    }
    private String getTopicFromCategory(String category) {
        switch (category) {
            case "Fiction":
                return "fiction";
            case "Non-fiction":
                return "non-fiction";
            case "Science":
                return "science";
            case "History":
                return "history";
            case "All":
            default:
                return ""; // Không áp dụng topic để lấy tất cả
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