package com.example.auranews.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.auranews.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ReportUtil {

    public static void attachReportButton(
            Button btnReport,
            Activity activity,
            FirebaseFirestore db,
            String articleId,
            String uid
    ) {
        if (btnReport == null || activity == null || db == null) return;
        if (articleId == null || articleId.isEmpty()) {
            btnReport.setEnabled(false);
            return;
        }

        btnReport.setOnClickListener(v ->
                showReportDialog(activity, db, articleId, uid)
        );
    }

    private static void showReportDialog(Activity activity,
                                         FirebaseFirestore db,
                                         String articleId,
                                         String uid) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Báo cáo bài viết (Vui lòng gửi qua Gmail!)");

        final android.view.View dialogView =
                activity.getLayoutInflater().inflate(R.layout.report_dialog, null);
        builder.setView(dialogView);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            EditText edtContent = dialogView.findViewById(R.id.edt_report_content);
            String content = edtContent.getText().toString().trim();

            if (content.isEmpty()) {
                Toast.makeText(activity, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            sendReportEmail(activity, articleId, content);
            saveReportFirestore(db, articleId, uid, content);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private static void sendReportEmail(Activity activity,
                                        String articleId,
                                        String content) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"auranewsreport@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Báo cáo bài viết ID: " + articleId);
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "ID bài viết: " + articleId +
                        "\nNội dung báo cáo:\n" + content);

        try {
            activity.startActivity(Intent.createChooser(emailIntent, "Gửi báo cáo bằng:"));
        } catch (Exception e) {
            Toast.makeText(activity, "Không tìm thấy ứng dụng Email", Toast.LENGTH_SHORT).show();
        }
    }

    private static void saveReportFirestore(FirebaseFirestore db,
                                            String articleId,
                                            String uid,
                                            String content) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("reported_at", FieldValue.serverTimestamp());
        map.put("article_id", articleId);
        map.put("user_id", uid);
        map.put("content", content);

        db.collection("reports").add(map);
    }
}
