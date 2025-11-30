package com.example.auranews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.auranews.R;
import com.example.auranews.adapters.CheckRole;
import com.example.auranews.utils.CommentUtil;
import com.example.auranews.utils.FavoriteUtil;
import com.example.auranews.utils.NetworkUtil;
import com.example.auranews.utils.ReportUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class NewsDetailActivity extends AppCompatActivity {

    String articleId;
    String uid;
    FirebaseFirestore db;

    // Views
    ImageView btnBack;
    ImageButton btnFavorite;
    Button btnReport;
    TextView tvCategory, tvTitle, tvContent;
    ImageView imgNews;

    LinearLayout commentsContainer;
    EditText edtComment;
    ImageButton btnSendComment;

    CheckRole checkRole;
    boolean isAdmin = false;

    private long startReadTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        // Kiểm tra internet
        if (!NetworkUtil.isConnected(this)) {
            startActivity(new Intent(NewsDetailActivity.this, NoInternetActivity.class));
            finish();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        uid = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        articleId = getIntent().getStringExtra("NEWS_ID");
        if (articleId == null || articleId.isEmpty()) {
            finish();
            return;
        }

        initViews();
        bindArticleDataFromIntent();
        loadArticleFromFirestore();
        setupBackButton();
        increaseUserReadArticle();
        startReadTime = System.currentTimeMillis();

        FavoriteUtil.initFavoriteButton(btnFavorite, db, uid, articleId, this);
        ReportUtil.attachReportButton(btnReport, this, db, articleId, uid);
        increaseArticleViewCount();

        checkRole = new CheckRole(this);
        checkRole.getUserRole(role -> {
            isAdmin = "admin".equals(role);
            CommentUtil.initCommentsSection(this, db, articleId, uid, isAdmin, commentsContainer, edtComment, btnSendComment);
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnReport = findViewById(R.id.btn_report);
        tvCategory = findViewById(R.id.textView_category);
        tvTitle = findViewById(R.id.textView_title);
        tvContent = findViewById(R.id.text_news);
        imgNews = findViewById(R.id.imageView_news);
        commentsContainer = findViewById(R.id.commentsContainer);
        edtComment = findViewById(R.id.edtComment);
        btnSendComment = findViewById(R.id.btnSendComment);
    }

    private void bindArticleDataFromIntent() {
        Intent intent = getIntent();
        if (tvTitle != null) tvTitle.setText(intent.getStringExtra("NEWS_TITLE"));
        if (tvCategory != null) tvCategory.setText("Danh mục: " + intent.getStringExtra("NEWS_CATEGORY"));
        if (tvContent != null) tvContent.setText(intent.getStringExtra("NEWS_CONTENT"));
        loadImage(intent.getStringExtra("NEWS_IMAGE"));
    }

    private void loadArticleFromFirestore() {
        db.collection("posts").document(articleId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;
            if (tvTitle != null) tvTitle.setText(doc.getString("title"));
            if (tvCategory != null) tvCategory.setText("Danh mục: " + doc.getString("category"));
            if (tvContent != null) tvContent.setText(doc.getString("content"));
            loadImage(doc.getString("thumbnail"));
        });
    }

    private void loadImage(String imageUrl) {
        if (imgNews != null && imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("http://")) imageUrl = imageUrl.replace("http://", "https://");
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_image_placeholder).error(R.drawable.ic_image_placeholder).fit().centerCrop().into(imgNews);
        }
    }

    private void setupBackButton() {
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
    private void increaseArticleViewCount() {
        db.collection("posts")
                .document(articleId)
                .update("views", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    // Nếu chưa có trường views thì tạo mới
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("views", 1);
                    db.collection("posts")
                            .document(articleId)
                            .set(map, SetOptions.merge());
                });
    }

    // hàm tăng số lượng bài báo đã đọc
    private void increaseUserReadArticle() {

        db.collection("users")
                .document(uid)
                .collection("read_articles")
                .document(articleId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        // User đã đọc bài này KHÔNG tăng số lượng
                        return;
                    }

                    // User CHƯA đọc bài này tăng total_read_articles
                    db.collection("users")
                            .document(uid)
                            .update("total_read_articles", FieldValue.increment(1))
                            .addOnFailureListener(e -> {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("total_read_articles", 1);
                                db.collection("users").document(uid).set(map, SetOptions.merge());
                            });

                    // Lưu dấu đã đọc bài này
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("read", true);
                    map.put("timestamp", System.currentTimeMillis());

                    db.collection("users")
                            .document(uid)
                            .collection("read_articles")
                            .document(articleId)
                            .set(map);
                });
    }

    // hàm lưu thời gian đọc báo
    private void saveReadingTime() {
        if (startReadTime == 0) return;

        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startReadTime) / 1000;  // tính giây đã đọc

        db.collection("users")
                .document(uid)
                .update("total_read_time", FieldValue.increment(seconds))
                .addOnFailureListener(e -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("total_read_time", seconds);
                    db.collection("users").document(uid).set(map, SetOptions.merge());
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveReadingTime();
    }

}