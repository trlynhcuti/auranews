package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.example.auranews.adapters.CheckRole;
import com.example.auranews.utils.NetworkUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvReadCount, tvReadTime, tvAccountName;
    private Button btnLogout;
    private Button btnHistory;
    private BottomNavigationView bottomNavigationView;

    private FirebaseFirestore db;
    private CheckRole checkRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Kiểm tra internet
        if (!NetworkUtil.isConnected(this)) {
            startActivity(new Intent(ProfileActivity.this, NoInternetActivity.class));
            finish();
            return;
        }

        tvReadCount = findViewById(R.id.tv_count_readed_news);
        tvReadTime = findViewById(R.id.tv_time_readed_news);
        tvAccountName = findViewById(R.id.tv_account_name);
        btnLogout = findViewById(R.id.btn_logout);
        btnHistory = findViewById(R.id.btn_history);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        db = FirebaseFirestore.getInstance();
        checkRole = new CheckRole(this);

        // Hiển thị tài khoản đang đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && tvAccountName != null) {
            String name = currentUser.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = currentUser.getEmail();
            }
            tvAccountName.setText("Tài khoản: " + (name != null ? name : "Không xác định"));
        }

        loadUserProfile();
        setupBottomNavigation();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.getMenu().clear();

        checkRole.getUserRole(role -> {
            bottomNavigationView.getMenu().clear();

            if ("admin".equalsIgnoreCase(role)) {
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_admin);
            } else {
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
            }

            bottomNavigationView.setSelectedItemId(R.id.nav_profile);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_profile) return true;

                Intent intent = null;

                if (id == R.id.nav_home) {
                    intent = new Intent(ProfileActivity.this, MainActivity.class);
                } else if (id == R.id.nav_favorites) {
                    intent = new Intent(ProfileActivity.this, FavoritesActivity.class);
                } else if (id == R.id.nav_create) {
                    intent = new Intent(ProfileActivity.this, AdminMainActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        });
    }

    private void loadUserProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long totalArticles = document.getLong("total_read_articles");
                        int count = (totalArticles != null) ? totalArticles.intValue() : 0;
                        tvReadCount.setText(String.valueOf(count) + " bài báo");

                        Long totalTimeSec = document.getLong("total_read_time");
                        long seconds = (totalTimeSec != null) ? totalTimeSec : 0;
                        long minutes = seconds / 60;
                        tvReadTime.setText(String.valueOf(minutes) + " phút");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show()
                );
    }
}
