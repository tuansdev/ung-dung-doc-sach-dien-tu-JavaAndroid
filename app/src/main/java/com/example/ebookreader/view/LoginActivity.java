package com.example.ebookreader.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.ebookreader.databinding.ActivityLoginBinding;
import com.example.ebookreader.viewmodel.AuthViewModel;


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo SharedPreferences
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            viewModel.login(email, password).observe(this, user -> {
                if (user != null) {
                    // Lưu thông tin người dùng vào SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    String emails = user.getEmail();
                    String username = email.split("@")[0];
                    editor.putString("email", emails);
                    editor.putString("username",username);
                    editor.apply();

                    // Chuyển đến BookListActivity
                    startActivity(new Intent(this, BookListActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}