package com.example.ebookreader.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ebookreader.R;
import com.example.ebookreader.databinding.ActivityBookListBinding;
import com.example.ebookreader.databinding.ActivityProfileBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private TextView tvUsername, tvEmail;
    private Button btnLogout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Router
        setupBottomNavigation();

        // Khởi tạo SharedPreferences
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Gán các view
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnLogout = findViewById(R.id.btnLogout);

        // Lấy và hiển thị thông tin người dùng
        String username = prefs.getString("username", "Không có tên");
        String email = prefs.getString("email", "Không có email");
        tvUsername.setText("Tên người dùng: " + username);
        tvEmail.setText("Email: " + email);

        // Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            // Xóa thông tin đăng nhập
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Quay lại LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


    }

    //Router
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, BookListActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_books) {
                startActivity(new Intent(this, FileStorageActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

}