package com.example.ebookreader.view;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ebookreader.R;
import com.example.ebookreader.api.Book;
import com.example.ebookreader.databinding.ActivityBookDetailBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class BookDetailActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int REQUEST_CODE_SIGN_IN = 101;
    private static final String TAG = "BookDetail";
    private ActivityBookDetailBinding binding;
    private GoogleAccountCredential credential;
    private Drive driveService;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Sửa typo: setContentView thay vì setContentInstanceSet
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Router
        setupBottomNavigation();

        book = (Book) getIntent().getSerializableExtra("BOOK");
        Log.d("BookDetail", "Đã nhận sách: " + (book != null ? book.title : "null"));

        if (book != null) {
            binding.tvTitle.setText(book.title != null ? book.title : "No Title");
            binding.tvAuthor.setText(book.authors != null && !book.authors.isEmpty() && book.authors.get(0).name != null
                    ? book.authors.get(0).name : "Unknown");
            binding.tvSubjects.setText(book.subjects != null && !book.subjects.isEmpty()
                    ? String.join(", ", book.subjects) : "No Subjects");
            binding.tvSummary.setText(book.summaries != null && !book.summaries.isEmpty()
                    ? book.summaries.get(0) : "No Summary Available");
            List<String> formatsList = new ArrayList<>();
            Book.Formats f = book.formats;

            if (f != null) {
                if (f.applicationPdf != null) formatsList.add("PDF");
                if (f.textPlain != null) formatsList.add("Text");
                if (f.epub != null) formatsList.add("EPUB");
                if (f.html != null) formatsList.add("HTML");
            }

            String formatText = !formatsList.isEmpty()
                    ? "Định dạng: " + String.join(", ", formatsList)
                    : "Định dạng: Không có";

            binding.tvFormat.setText(formatText);
        } else {
            Toast.makeText(this, "Dữ liệu sách không có sẵn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnReadOnline.setOnClickListener(v -> {
            if (book != null && book.formats != null) {
                String realUrl = null;
                if (book.formats.applicationPdf != null) {
                    realUrl = book.formats.applicationPdf;
                    Intent intent = new Intent(this, PdfReaderActivity.class);
                    intent.putExtra("URL", realUrl);
                    startActivity(intent);
                } else if (book.formats.textPlain != null) {
                    int bookId = book.id;
                    String correctedUrl = "https://www.gutenberg.org/cache/epub/" + bookId + "/pg" + bookId + ".txt";
                    Intent intent = new Intent(this, TextReaderActivity.class);
                    intent.putExtra("URL", correctedUrl);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Không có định dạng có thể đọc", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Thông tin sách không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
        credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(DriveScopes.DRIVE_FILE));
        binding.btnImport.setOnClickListener(v -> {
            Log.d(TAG, "Nút Import được nhấn");
            if (credential.getSelectedAccountName() == null) {
                Log.d(TAG, "Chưa chọn tài khoản, yêu cầu đăng nhập");
                requestSignIn();
            } else if (initializeDriveService()) {
                Log.d(TAG, "Tài khoản đã chọn, bắt đầu import");
                importToDrive();
            } else {
                Log.d(TAG, "Lỗi khởi tạo Drive service");
                Toast.makeText(this, "Lỗi khởi tạo Google Drive", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnImport.setOnClickListener(v -> {
            if (initializeDriveService()) {
                importToDrive();
            } else {
                requestSignIn();
            }
        });
    }

    private boolean checkStoragePermission() {
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        Log.d("BookDetail", "Kiểm tra quyền bộ nhớ: " + (hasPermission ? "Được cấp" : "Bị từ chối"));
        return hasPermission;
    }

    private void requestStoragePermission() {
        Log.d("BookDetail", "Yêu cầu quyền bộ nhớ. Nên hiển thị hộp thoại.");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // Chưa có quyền, yêu cầu người dùng cấp
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    STORAGE_PERMISSION_CODE);
        } else {
            // Đã có quyền
            Toast.makeText(this, "Đã có quyền GET_ACCOUNTS", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == STORAGE_PERMISSION_CODE) {
            Log.d("BookDetail", "Permission result received");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("BookDetail", "Permission granted, starting import");
                if (initializeDriveService()) {
                    importToDrive();
                } else {
                    requestSignIn();
                }
            } else {
                Log.d("BookDetail", "Permission denied");
                Toast.makeText(this, "Quyền bộ nhớ bị từ chối. Không thể import.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestSignIn() {
        Log.d(TAG, "Yêu cầu chọn tài khoản Google");
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_CODE_SIGN_IN);
    }

    private boolean initializeDriveService() {
        Log.d(TAG, "Khởi tạo Drive service");
        if (driveService == null && (credential == null || credential.getSelectedAccountName() == null)) {
            Log.d(TAG, "Credential hoặc tài khoản null, không khởi tạo được");
            return false;
        }
        if (driveService == null) {
            try {
                NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                driveService = new Drive.Builder(transport, JacksonFactory.getDefaultInstance(), credential)
                        .setApplicationName("EBookReader")
                        .build();
                Log.d(TAG, "Drive service khởi tạo thành công");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khởi tạo Drive: " + e.getMessage());
                Toast.makeText(this, "Lỗi khởi tạo Google Drive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Log.d(TAG, "Drive service đã tồn tại");
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK && data != null) {
            Account account = data.getParcelableExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (account == null) {
                // Hoặc dùng cách này:
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    credential.setSelectedAccountName(accountName);
                    Log.d(TAG, "Đã đặt tài khoản: " + accountName);
                } else {
                    Log.d(TAG, "Tài khoản null");
                    Toast.makeText(this, "Chưa chọn tài khoản Google", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                credential.setSelectedAccount(account);
                Log.d(TAG, "Đã đặt tài khoản: " + account.name);
            }

            if (initializeDriveService()) {
                importToDrive();
            } else {
                Log.d(TAG, "Khởi tạo Drive thất bại sau khi chọn tài khoản");
                Toast.makeText(this, "Lỗi khởi tạo Google Drive sau đăng nhập", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Chưa chọn tài khoản sau khi mở intent hoặc bị hủy");
            Toast.makeText(this, "Chưa chọn tài khoản Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void importToDrive() {
        if (driveService == null) {
            Log.d(TAG, "Drive service null, không thể import");
            Toast.makeText(this, "Vui lòng chọn tài khoản Google", Toast.LENGTH_SHORT).show();
            return;
        }

        if (book.formats == null || (book.formats.applicationPdf == null && book.formats.textPlain == null)) {
            Log.d(TAG, "Không có định dạng để import");
            Toast.makeText(this, "Không có định dạng có thể import", Toast.LENGTH_SHORT).show();
            return;
        }

        String url;
        String mimeType;
        if (book.formats.applicationPdf != null) {
            url = book.formats.applicationPdf;
            mimeType = "application/pdf";
        } else if (book.formats.textPlain != null) {
            // Sửa URL cho bản text để không bị redirect
            url = "https://www.gutenberg.org/cache/epub/" + book.id + "/pg" + book.id + ".txt";
            mimeType = "text/plain";
        } else {
            Log.d(TAG, "Không có định dạng để import");
            runOnUiThread(() -> Toast.makeText(this, "Không có định dạng có thể import", Toast.LENGTH_SHORT).show());
            return;
        }
        String fileName = book.title != null ? book.title.replaceAll("[^a-zA-Z0-9.-]", "_") : "book";
        Log.d(TAG, "Bắt đầu import: URL=" + url + ", MimeType=" + mimeType + ", FileName=" + fileName);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Tải tệp từ URL xuống bộ nhớ tạm
                URL downloadUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();

                java.io.File tempFile = new File(getCacheDir(), fileName + (mimeType.equals("application/pdf") ? ".pdf" : ".txt"));
                FileOutputStream outputStream = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                inputStream.close();
                Log.d(TAG, "Tải tệp tạm thành công: " + tempFile.getAbsolutePath());

                // Tạo metadata cho Google Drive
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(fileName + (mimeType.equals("application/pdf") ? ".pdf" : ".txt"));

                // Tải lên Google Drive
                java.io.File filePath = tempFile;
                FileContent mediaContent = new FileContent(mimeType, filePath);
                com.google.api.services.drive.model.File file = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id, name")
                        .execute();
                tempFile.delete(); // Xóa tệp tạm
                Log.d(TAG, "Upload lên Drive thành công: " + file.getName());
                runOnUiThread(() -> Toast.makeText(this, "Import vào Google Drive hoàn tất: " + file.getName(), Toast.LENGTH_LONG).show());
             } catch (UserRecoverableAuthIOException e) {
            Log.e(TAG, "Cần thêm quyền từ người dùng, mở hộp thoại xác nhận", e);
            Intent recover = e.getIntent();
            runOnUiThread(() -> startActivityForResult(recover, REQUEST_CODE_SIGN_IN));
        } catch (Exception e) {
            Log.e(TAG, "Lỗi import vào Drive: " + Log.getStackTraceString(e));
            runOnUiThread(() ->
                    Toast.makeText(this, "Import thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
        });
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