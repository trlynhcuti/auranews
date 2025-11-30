package com.example.auranews.utils;

import android.app.Activity;
import android.app.AlertDialog;

import com.example.auranews.models.AppNotification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class NotificationUtil {

    public static void checkUnreadNotifications(Activity activity,
                                                FirebaseFirestore db,
                                                String uid) {

        if (db == null || uid == null || uid.isEmpty()) return;

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        return;
                    }

                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();

                    StringBuilder sb = new StringBuilder();
                    for (DocumentSnapshot doc : docs) {
                        AppNotification noti = doc.toObject(AppNotification.class);
                        if (noti == null) continue;

                        String type = noti.getType();
                        String fromName = noti.getFromUserName();

                        if ("comment_reply".equals(type)) {
                            sb.append("• ")
                                    .append(fromName)
                                    .append(" đã trả lời bình luận của bạn.\n");
                        } else if ("new_post".equals(type)) {
                            sb.append("• Có bài viết mới từ admin.\n");
                        } else {
                            sb.append("• Bạn có thông báo mới.\n");
                        }
                    }
                });
    }
}
