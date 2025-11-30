package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.adapters.AdminPostAdapter;
import com.example.auranews.adapters.CheckRole;
import com.example.auranews.models.NewsItem;
import com.example.auranews.R;
import com.example.auranews.utils.NetworkUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PostManagerActivity extends AppCompatActivity {

    private ListView listViewPosts;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private List<NewsItem> postList;
    private AdminPostAdapter adapter;
    private FirebaseFirestore db;
    private CheckRole checkRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_manager);

        // Kiểm tra internet
        if (!NetworkUtil.isConnected(this)) {
            startActivity(new Intent(PostManagerActivity.this, NoInternetActivity.class));
            finish();
            return;
        }

        // 1. Bảo mật
        checkRole = new CheckRole(this);
        checkRole.getUserRole(role -> {
            if (!"admin".equals(role)) {
                Toast.makeText(this, "Không có quyền truy cập!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 2. Ánh xạ
        listViewPosts = findViewById(R.id.listViewPosts);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Nút Back
        View btnBack = findViewById(R.id.btnBack);
        if(btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 3. Setup
        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();

        adapter = new AdminPostAdapter(this, postList, new AdminPostAdapter.OnAdminActionListener() {
            @Override
            public void onEdit(NewsItem newsItem) {
                // Sửa: Chuyển về trang AdminMainActivity
                Intent intent = new Intent(PostManagerActivity.this, AdminMainActivity.class);
                intent.putExtra("POST_ID", newsItem.getId());
                intent.putExtra("POST_TITLE", newsItem.getTitle());
                intent.putExtra("POST_CONTENT", newsItem.getContent());
                intent.putExtra("POST_CATEGORY", newsItem.getCategory());
                intent.putExtra("POST_IMAGE", newsItem.getThumbnail());
                startActivity(intent);
                finish();
            }

            @Override
            public void onDelete(NewsItem newsItem) {
                showDeleteDialog(newsItem);
            }
        });
        listViewPosts.setAdapter(adapter);

        // 4. Tải dữ liệu
        loadPosts();
    }

    private void loadPosts() {
        if(progressBar != null) progressBar.setVisibility(View.VISIBLE);

        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(progressBar != null) progressBar.setVisibility(View.GONE);

                    int count = queryDocumentSnapshots.size();
                    // Báo số lượng tìm thấy
                    Toast.makeText(this, "Tìm thấy: " + count + " bài viết", Toast.LENGTH_SHORT).show();

                    postList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                NewsItem item = doc.toObject(NewsItem.class);
                                if (item != null) {
                                    item.setId(doc.getId());
                                    postList.add(item);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Lỗi đọc 1 bài: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if(tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                    } else {
                        if(tvEmptyState != null) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("Danh sách trống!");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if(progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showDeleteDialog(NewsItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn chắc chắn muốn xóa bài: " + item.getTitle() + "?\nKhông thể khôi phục.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deletePost(item.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deletePost(String id) {
        if(progressBar != null) progressBar.setVisibility(View.VISIBLE);
        db.collection("posts").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                    loadPosts(); // Tải lại danh sách
                })
                .addOnFailureListener(e -> {
                    if(progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}