package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.example.auranews.adapters.CheckRole;
import com.example.auranews.adapters.NewsAdapter;
import com.example.auranews.models.NewsItem;
import com.example.auranews.utils.NetworkUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    ListView lvNews;
    NewsAdapter adapter;

    BottomNavigationView bottomNavigation_view;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    CheckRole checkRole = new CheckRole(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_news);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Kiểm tra mạng
        if (!NetworkUtil.isConnected(this)) {
            Intent intent = new Intent(FavoritesActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        lvNews = findViewById(R.id.lv_favorites);
        bottomNavigation_view = findViewById(R.id.bottom_navigation);

        // Phân quyền Admin
        bottomNavigation_view.getMenu().clear();
        checkRole.getUserRole(role -> {
            bottomNavigation_view.getMenu().clear();

            if ("admin".equalsIgnoreCase(role)) {
                bottomNavigation_view.inflateMenu(R.menu.bottom_nav_menu_admin);
            } else {
                bottomNavigation_view.inflateMenu(R.menu.bottom_nav_menu);
            }
            bottomNavigation_view.setSelectedItemId(R.id.nav_favorites);
        });

        // Tạo adapter với list rỗng
        adapter = new NewsAdapter(this, R.layout.listview_news, new ArrayList<>());
        lvNews.setAdapter(adapter);

        // Click item mở bài báo
        lvNews.setOnItemClickListener((parent, view, position, id) -> {
            NewsItem selectedNewsItem = (NewsItem) adapter.getItem(position);

            Intent detailIntent = new Intent(FavoritesActivity.this, NewsDetailActivity.class);
            detailIntent.putExtra("NEWS_ID", selectedNewsItem.getId());
            startActivity(detailIntent);
        });

        // Bottom navigation
        bottomNavigation_view.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(FavoritesActivity.this, MainActivity.class));
                finish();
                return true;
            }
            if (itemId == R.id.nav_favorites) {
                return true;
            }
            if (itemId == R.id.nav_create) {
                startActivity(new Intent(FavoritesActivity.this, AdminMainActivity.class));
                finish();
                return true;
            }
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(FavoritesActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // Lần đầu vào màn hình load danh sách
        loadFavoriteNews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi lần quay lại màn Yêu thích thì reload
        loadFavoriteNews();
    }

    // load danh sách yêu thích
    private void loadFavoriteNews() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Không tìm thấy UID", Toast.LENGTH_SHORT).show();
            return;
        }

        // List tạm để đổ dữ liệu vào rồi set cho adapter
        List<NewsItem> tempList = new ArrayList<>();
        adapter.updateData(tempList); // clear adapter trước

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener(favoriteDocs -> {
                    if (favoriteDocs.isEmpty()) {
                        Toast.makeText(this,
                                "Bạn chưa yêu thích bài báo nào",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot favDoc : favoriteDocs.getDocuments()) {
                        String articleId = favDoc.getId(); // doc id chính là postId

                        db.collection("posts")
                                .document(articleId)
                                .get()
                                .addOnSuccessListener(newsDoc -> {
                                    if (newsDoc.exists()) {
                                        NewsItem item = newsDoc.toObject(NewsItem.class);
                                        if (item != null) {
                                            item.setId(newsDoc.getId());
                                            tempList.add(item);
                                            // mỗi lần có dữ liệu mới thì cập nhật adapter
                                            adapter.updateData(tempList);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Lỗi khi tải bài yêu thích: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Lỗi khi tải danh sách yêu thích: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
