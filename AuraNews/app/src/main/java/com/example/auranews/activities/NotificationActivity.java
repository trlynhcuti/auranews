package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.example.auranews.adapters.NotificationAdapter;
import com.example.auranews.models.AppNotification;
import com.example.auranews.utils.NetworkUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String uid;

    private ListView lvNotifications;
    private TextView tvEmpty;

    private List<AppNotification> notiList;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Kiểm tra internet
        if (!NetworkUtil.isConnected(this)) {
            startActivity(new Intent(NotificationActivity.this, NoInternetActivity.class));
            finish();
            return;
        }

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            finish();
            return;
        }
        uid = current.getUid();

        db = FirebaseFirestore.getInstance();

        lvNotifications = findViewById(R.id.listView_notifications);
        tvEmpty = findViewById(R.id.tvNotificationsEmpty);

        notiList = new ArrayList<>();

        // Adapter với callback xoá + dialog xác nhận
        adapter = new NotificationAdapter(this, notiList, noti -> {
            if (noti.getId() == null) return;

            new AlertDialog.Builder(NotificationActivity.this)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc muốn xoá thông báo này không?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        db.collection("users")
                                .document(uid)
                                .collection("notifications")
                                .document(noti.getId())
                                .delete()
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(NotificationActivity.this,
                                                "Đã xoá thông báo", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(err ->
                                        Toast.makeText(NotificationActivity.this,
                                                "Lỗi xoá: " + err.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        lvNotifications.setAdapter(adapter);

        loadNotifications();

        // CLICK VÀO ITEM -> luôn mở NewsDetailActivity
        lvNotifications.setOnItemClickListener((parent, view, position, id) -> {
            AppNotification noti = notiList.get(position);
            if (noti == null) return;

            // Đánh dấu đã đọc
            if (noti.getId() != null) {
                db.collection("users")
                        .document(uid)
                        .collection("notifications")
                        .document(noti.getId())
                        .update("isRead", true);
            }

            // MỞ BÀI VIẾT
            if (noti.getArticleId() != null && !noti.getArticleId().isEmpty()) {
                Intent i = new Intent(NotificationActivity.this, NewsDetailActivity.class);
                i.putExtra("NEWS_ID", noti.getArticleId());
                startActivity(i);
            } else {
                Toast.makeText(NotificationActivity.this,
                        "Thông báo không chứa ID bài viết", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadNotifications() {
        db.collection("users")
                .document(uid)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    notiList.clear();

                    if (snap.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    final int[] pending = {snap.size()};

                    for (QueryDocumentSnapshot doc : snap) {
                        AppNotification n = doc.toObject(AppNotification.class);
                        if (n == null) {
                            pending[0]--;
                            continue;
                        }

                        n.setId(doc.getId());
                        String articleId = n.getArticleId();

                        // Không gắn bài viết -> add
                        if (articleId == null || articleId.isEmpty()) {
                            notiList.add(n);
                            pending[0]--;
                            if (pending[0] == 0) {
                                adapter.notifyDataSetChanged();
                                tvEmpty.setVisibility(notiList.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                            continue;
                        }

                        // Có articleId -> kiểm tra bài còn không
                        db.collection("posts")
                                .document(articleId)
                                .get()
                                .addOnSuccessListener(postDoc -> {
                                    if (postDoc.exists()) {
                                        // Bài viết còn -> hiển thị noti
                                        notiList.add(n);
                                    } else {
                                        // Bài bị xoá -> xoá luôn notification trong Firestore
                                        if (n.getId() != null) {
                                            db.collection("users")
                                                    .document(uid)
                                                    .collection("notifications")
                                                    .document(n.getId())
                                                    .delete();
                                        }
                                    }

                                    pending[0]--;
                                    if (pending[0] == 0) {
                                        adapter.notifyDataSetChanged();
                                        tvEmpty.setVisibility(notiList.isEmpty() ? View.VISIBLE : View.GONE);
                                    }
                                })
                                .addOnFailureListener(err -> {
                                    // Nếu lỗi khi check bài
                                    notiList.add(n);
                                    pending[0]--;
                                    if (pending[0] == 0) {
                                        adapter.notifyDataSetChanged();
                                        tvEmpty.setVisibility(notiList.isEmpty() ? View.VISIBLE : View.GONE);
                                    }
                                });
                    }
                });
    }
}
