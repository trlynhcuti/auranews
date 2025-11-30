package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.example.auranews.adapters.NewsAdapter;
import com.example.auranews.models.NewsItem;
import com.example.auranews.utils.NetworkUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    ListView lvNews;
    NewsAdapter adapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Kiểm tra mạng
        if (!NetworkUtil.isConnected(this)) {
            Intent intent = new Intent(HistoryActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        lvNews = findViewById(R.id.lv_history);

        // Tạo adapter với list rỗng
        adapter = new NewsAdapter(this, R.layout.listview_news, new ArrayList<>());
        lvNews.setAdapter(adapter);

        // Load danh sách bài đã đọc
        loadHistoryNews();

        // Click item -> mở bài báo
        lvNews.setOnItemClickListener((parent, view, position, id) -> {
            NewsItem selectedNewsItem = (NewsItem) adapter.getItem(position);

            Intent detailIntent = new Intent(HistoryActivity.this, NewsDetailActivity.class);
            detailIntent.putExtra("NEWS_ID", selectedNewsItem.getId());
            startActivity(detailIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi lần quay lại lịch sử thì reload
        loadHistoryNews();
    }

    // load danh sách đã đọc
    private void loadHistoryNews() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Không tìm thấy UID", Toast.LENGTH_SHORT).show();
            return;
        }

        // List tạm để đổ dữ liệu rồi set cho adapter
        List<NewsItem> tempList = new ArrayList<>();
        adapter.updateData(tempList); // clear adapter trước

        db.collection("users")
                .document(uid)
                .collection("read_articles")
                .get()
                .addOnSuccessListener(historyDocs -> {
                    if (historyDocs.isEmpty()) {
                        Toast.makeText(this,
                                "Bạn chưa đọc bài báo nào",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : historyDocs.getDocuments()) {
                        String articleId = doc.getId();

                        db.collection("posts")
                                .document(articleId)
                                .get()
                                .addOnSuccessListener(newsDoc -> {
                                    if (newsDoc.exists()) {
                                        NewsItem item = newsDoc.toObject(NewsItem.class);
                                        if (item != null) {
                                            item.setId(newsDoc.getId());
                                            tempList.add(item);
                                            // mỗi lần có thêm 1 bài thì cập nhật adapter
                                            adapter.updateData(tempList);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Lỗi khi tải bài đã đọc: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Lỗi khi tải danh sách bài báo đã đọc: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

}
