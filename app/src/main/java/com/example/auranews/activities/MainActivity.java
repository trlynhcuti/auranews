package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.example.auranews.adapters.CheckRole;
import com.example.auranews.adapters.NewsAdapter;
import com.example.auranews.models.NewsItem;
import com.example.auranews.utils.NetworkUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADMIN_POST = 101;

    ListView lvNews;
    NewsAdapter adapter;

    BottomNavigationView bottomNavigation_view;
    CheckRole checkRole;
    FirebaseFirestore db;

    ImageButton btnNotification;
    EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra internet
        if (!NetworkUtil.isConnected(this)) {
            startActivity(new Intent(MainActivity.this, NoInternetActivity.class));
            finish();
            return;
        }

        // Ánh xạ View
        lvNews = findViewById(R.id.listView_main);
        bottomNavigation_view = findViewById(R.id.bottom_navigation);
        btnNotification = findViewById(R.id.btn_notification);
        edtSearch = findViewById(R.id.search_input);

        db = FirebaseFirestore.getInstance();
        checkRole = new CheckRole(this);

        // Khởi tạo adapter với list rỗng
        adapter = new NewsAdapter(this, R.layout.listview_news, new ArrayList<>());
        lvNews.setAdapter(adapter);

        // Click icon notification -> chỉ mở màn Notification
        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NotificationActivity.class));
        });

        // Lắng nghe số thông báo chưa đọc để đổi icon
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current != null) {
            String uid = current.getUid();

            db.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .whereEqualTo("isRead", false)
                    .addSnapshotListener((snap, e) -> {
                        if (e != null || snap == null) return;

                        if (snap.isEmpty()) {
                            // Không còn noti chưa đọc
                            btnNotification.setImageResource(R.drawable.mail);
                        } else {
                            // Có ít nhất 1 noti chưa đọc
                            btnNotification.setImageResource(R.drawable.new_mail);
                        }
                    });
        } else {
            // Nếu chưa đăng nhập thì để icon mặc định
            btnNotification.setImageResource(R.drawable.mail);
        }

        // Kiểm tra quyền admin để đổi menu
        bottomNavigation_view.getMenu().clear();
        checkRole.getUserRole(role -> {
            bottomNavigation_view.getMenu().clear();
            if ("admin".equalsIgnoreCase(role)) {
                bottomNavigation_view.inflateMenu(R.menu.bottom_nav_menu_admin);
            } else {
                bottomNavigation_view.inflateMenu(R.menu.bottom_nav_menu);
            }
            bottomNavigation_view.setSelectedItemId(R.id.nav_home);
        });

        // Tải tin tức từ Firestore
        loadNewsFromFirebase();

        // Click vào bài báo -> lấy trực tiếp từ adapter
        lvNews.setOnItemClickListener((parent, view, position, id) -> {
            NewsItem selectedNewsItem = (NewsItem) adapter.getItem(position);
            if (selectedNewsItem != null) {
                Intent detailIntent = new Intent(MainActivity.this, NewsDetailActivity.class);
                detailIntent.putExtra("NEWS_ID", selectedNewsItem.getId());
                startActivity(detailIntent);
            }
        });

        // Navigation
        bottomNavigation_view.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_home) {
                loadNewsFromFirebase();
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_favorites) {
                intent = new Intent(MainActivity.this, FavoritesActivity.class);
            } else if (itemId == R.id.nav_create) {
                intent = new Intent(MainActivity.this, AdminMainActivity.class);
                startActivityForResult(intent, REQUEST_ADMIN_POST);
                return true;
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(MainActivity.this, ProfileActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Tìm kiếm realtime bằng adapter
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation_view.setSelectedItemId(R.id.nav_home);
        loadNewsFromFirebase();
    }

    // Nhận kết quả từ AdminMainActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADMIN_POST && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("NEW_POST_CREATED", false)) {
                setNotificationNew();
                loadNewsFromFirebase();
            }
        }
    }

    private void loadNewsFromFirebase() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NewsItem> tempList = new ArrayList<>();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            NewsItem item = document.toObject(NewsItem.class);
                            if (item != null) {
                                item.setId(document.getId());
                                tempList.add(item);
                            }
                        }
                        // Cập nhật cho adapter (newsList + originalList)
                        adapter.updateData(tempList);
                    } else {
                        adapter.updateData(new ArrayList<>());
                        Toast.makeText(MainActivity.this, "Chưa có bài viết nào.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Lỗi tải tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void setNotificationNew() {
        btnNotification.setImageResource(R.drawable.new_mail);
    }
}
