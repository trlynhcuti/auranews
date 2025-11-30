package com.example.auranews.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.auranews.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FavoriteUtil {

    private static final String TAG = "FavoriteUtil";

    public static void initFavoriteButton(
            ImageButton btnFavorite,
            FirebaseFirestore db,
            String uid,
            String articleId,
            Context context
    ) {
        if (btnFavorite == null || db == null) return;

        if (uid == null || uid.isEmpty() || articleId == null || articleId.isEmpty()) {
            btnFavorite.setEnabled(false);
            Log.e(TAG, "uid hoặc articleId rỗng -> không bật được nút yêu thích");
            return;
        }

        // Load trạng thái ban đầu
        btnFavorite.setEnabled(false);

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(articleId)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean isFavorite = doc.exists();
                    btnFavorite.setTag(isFavorite);

                    btnFavorite.setImageResource(
                            isFavorite ? R.drawable.news_favorite : R.drawable.favorite
                    );

                    btnFavorite.setEnabled(true);
                    Log.d(TAG, "Load favorite state = " + isFavorite);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi load favorite: ", e);
                    Toast.makeText(context, "Không thể tải trạng thái yêu thích", Toast.LENGTH_SHORT).show();
                    btnFavorite.setEnabled(true);
                });

        // Click tim để toggle
        btnFavorite.setOnClickListener(v -> {
            btnFavorite.setEnabled(false); // chống spam

            Object tag = btnFavorite.getTag();
            boolean isFavorite = tag instanceof Boolean && (Boolean) tag;

            if (!isFavorite) {
                addFavorite(btnFavorite, db, uid, articleId, context);
            } else {
                removeFavorite(btnFavorite, db, uid, articleId, context);
            }
        });
    }

    private static void addFavorite(ImageButton btnFavorite,
                                    FirebaseFirestore db,
                                    String uid,
                                    String articleId,
                                    Context context) {

        Map<String, Object> map = new HashMap<>();
        map.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(articleId)
                .set(map)
                .addOnSuccessListener(unused -> {
                    btnFavorite.setTag(true);
                    btnFavorite.setImageResource(R.drawable.news_favorite);
                    Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    btnFavorite.setEnabled(true);
                    Log.d(TAG, "Thêm favorite thành công: " + articleId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi thêm favorite: ", e);
                    Toast.makeText(context, "Lỗi khi thêm yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnFavorite.setEnabled(true);
                });
    }

    private static void removeFavorite(ImageButton btnFavorite,
                                       FirebaseFirestore db,
                                       String uid,
                                       String articleId,
                                       Context context) {

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(articleId)
                .delete()
                .addOnSuccessListener(unused -> {
                    btnFavorite.setTag(false);
                    btnFavorite.setImageResource(R.drawable.favorite);
                    Toast.makeText(context, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    btnFavorite.setEnabled(true);
                    Log.d(TAG, "Xoá favorite thành công: " + articleId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi xoá favorite: ", e);
                    Toast.makeText(context, "Lỗi khi bỏ yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnFavorite.setEnabled(true);
                });
    }
}
