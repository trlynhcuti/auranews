package com.example.auranews.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.auranews.R;
import com.example.auranews.models.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentUtil {

    // Gọi từ NewsDetailActivity
    public static void initCommentsSection(
            Activity activity,
            FirebaseFirestore db,
            String articleId,
            String uid,
            boolean isAdmin,
            LinearLayout commentsContainer,
            EditText edtComment,
            ImageButton btnSendComment
    ) {
        if (db == null || articleId == null || articleId.isEmpty()) return;

        // Gửi comment gốc (không có parentId)
        if (btnSendComment != null && edtComment != null) {
            btnSendComment.setOnClickListener(v ->
                    sendComment(activity, db, articleId, edtComment, null)
            );
        }

        // Lắng nghe & hiển thị comment + reply
        loadComments(activity, db, articleId, uid, isAdmin, commentsContainer);
    }

    // LOAD COMMENT CHA – CON
    private static void loadComments(Activity activity,
                                     FirebaseFirestore db,
                                     String articleId,
                                     String uid,
                                     boolean isAdmin,
                                     LinearLayout commentsContainer) {

        db.collection("posts")
                .document(articleId)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null || commentsContainer == null) return;

                    commentsContainer.removeAllViews();

                    // Gom comment theo parentId
                    Map<String, List<Comment>> grouped = new HashMap<>();
                    List<Comment> rootComments = new ArrayList<>();

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Comment c = doc.toObject(Comment.class);
                        if (c == null) continue;
                        c.setId(doc.getId());

                        String parentId = c.getParentId();
                        if (parentId == null || parentId.isEmpty()) {
                            rootComments.add(c); // comment gốc
                        } else {
                            if (!grouped.containsKey(parentId)) {
                                grouped.put(parentId, new ArrayList<>());
                            }
                            grouped.get(parentId).add(c); // reply
                        }
                    }

                    // Vẽ từng comment gốc + list reply
                    for (Comment root : rootComments) {
                        addCommentView(activity, db, articleId, uid, isAdmin,
                                commentsContainer, root, grouped);
                    }
                });
    }

    // Vẽ 1 comment gốc
    private static void addCommentView(Activity activity,
                                       FirebaseFirestore db,
                                       String articleId,
                                       String uid,
                                       boolean isAdmin,
                                       LinearLayout commentsContainer,
                                       Comment rootComment,
                                       Map<String, List<Comment>> grouped) {

        android.view.View itemView = activity.getLayoutInflater()
                .inflate(R.layout.item_comment, commentsContainer, false);

        TextView tvUserName = itemView.findViewById(R.id.tvUserName);
        TextView tvContent = itemView.findViewById(R.id.tvCommentContent);
        TextView tvTime = itemView.findViewById(R.id.tvCommentTime);
        TextView tvReply = itemView.findViewById(R.id.tvReply);
        LinearLayout repliesContainer = itemView.findViewById(R.id.repliesContainer);

        if (tvUserName != null) tvUserName.setText(rootComment.getUserName());
        if (tvContent != null) tvContent.setText(rootComment.getContent());
        if (tvTime != null) tvTime.setText(""); // TODO: format thời gian nếu thích

        // Long click để xoá comment gốc
        itemView.setOnLongClickListener(v -> {
            handleCommentLongClick(activity, db, articleId, uid, isAdmin, rootComment);
            return true;
        });

        // Nhấn "Trả lời" -> mở dialog
        if (tvReply != null) {
            tvReply.setOnClickListener(v ->
                    showReplyDialog(activity, db, articleId, rootComment)
            );
        }

        commentsContainer.addView(itemView);

        // Vẽ replies của comment này (nếu có)
        List<Comment> replies = grouped.get(rootComment.getId());
        if (replies != null && repliesContainer != null) {
            for (Comment reply : replies) {
                addReplyView(activity, db, articleId, uid, isAdmin, repliesContainer, reply);
            }
        }
    }

    // Vẽ 1 reply (comment con)
    private static void addReplyView(Activity activity,
                                     FirebaseFirestore db,
                                     String articleId,
                                     String uid,
                                     boolean isAdmin,
                                     LinearLayout repliesContainer,
                                     Comment reply) {

        android.view.View replyView = activity.getLayoutInflater()
                .inflate(R.layout.item_reply_comment, repliesContainer, false);

        TextView tvUserName = replyView.findViewById(R.id.tvUserNameReply);
        TextView tvContent = replyView.findViewById(R.id.tvReplyContent);

        if (tvUserName != null) {
            if (reply.getReplyToUserName() != null && !reply.getReplyToUserName().isEmpty()) {
                tvUserName.setText(reply.getUserName()
                        + " trả lời " + reply.getReplyToUserName());
            } else {
                tvUserName.setText(reply.getUserName());
            }
        }

        if (tvContent != null) tvContent.setText(reply.getContent());

        // Long click để xoá reply
        replyView.setOnLongClickListener(v -> {
            handleCommentLongClick(activity, db, articleId, uid, isAdmin, reply);
            return true;
        });

        repliesContainer.addView(replyView);
    }

    // XOÁ COMMENT (ADMIN / OWNER)
    private static void handleCommentLongClick(Activity activity,
                                               FirebaseFirestore db,
                                               String articleId,
                                               String uid,
                                               boolean isAdmin,
                                               Comment comment) {

        if (!isAdmin && !uid.equals(comment.getUserId())) {
            Toast.makeText(activity,
                    "Bạn chỉ có thể xóa bình luận của chính mình",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("Xóa bình luận")
                .setMessage("Bạn có chắc chắn muốn xóa bình luận này không?")
                .setPositiveButton("Xóa", (dialog, which) ->
                        deleteComment(activity, db, articleId, comment.getId())
                )
                .setNegativeButton("Hủy", null)
                .show();
    }

    private static void deleteComment(Activity activity,
                                      FirebaseFirestore db,
                                      String articleId,
                                      String commentId) {
        if (commentId == null || commentId.isEmpty()) return;

        db.collection("posts")
                .document(articleId)
                .collection("comments")
                .document(commentId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(activity, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                    db.collection("posts")
                            .document(articleId)
                            .update("commentsCount", FieldValue.increment(-1));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity,
                                "Lỗi xóa bình luận: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // GỬI COMMENT GỐC / REPLY
    private static void sendComment(Activity activity,
                                    FirebaseFirestore db,
                                    String articleId,
                                    EditText edtComment,
                                    Comment parentComment) {

        String content = edtComment.getText().toString().trim();
        if (content.isEmpty()) {
            edtComment.setError("Không được để trống");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userId = user.getUid();
        String userName = user.getEmail();
        long now = System.currentTimeMillis();

        String parentId = null;
        String replyToUserId = null;
        String replyToUserName = null;

        if (parentComment != null) {
            parentId = parentComment.getId();
            replyToUserId = parentComment.getUserId();
            replyToUserName = parentComment.getUserName();
        }

        // final cho lambda
        final String finalReplyToUserId = replyToUserId;
        final String finalUserId = userId;
        final String finalUserName = userName;

        Comment comment = new Comment(
                userId, userName, content, now,
                parentId, replyToUserId, replyToUserName
        );

        db.collection("posts")
                .document(articleId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(ref -> {
                    edtComment.setText("");
                    db.collection("posts")
                            .document(articleId)
                            .update("commentsCount", FieldValue.increment(1));

                    // Nếu là reply tới người khác -> tạo notification
                    if (finalReplyToUserId != null
                            && !finalReplyToUserId.isEmpty()
                            && !finalReplyToUserId.equals(finalUserId)) {

                        createNotificationForReply(
                                db,
                                finalReplyToUserId,
                                articleId,
                                ref.getId(),
                                finalUserId,
                                finalUserName
                        );
                    }

                    Toast.makeText(activity,
                            "Đã gửi bình luận!",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity,
                                "Lỗi gửi bình luận: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // Dialog reply
    private static void showReplyDialog(Activity activity,
                                        FirebaseFirestore db,
                                        String articleId,
                                        Comment parentComment) {

        EditText edt = new EditText(activity);
        edt.setHint("Trả lời " + parentComment.getUserName());
        edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        new AlertDialog.Builder(activity)
                .setTitle("Trả lời bình luận")
                .setView(edt)
                .setPositiveButton("Gửi", (dialog, which) ->
                        sendComment(activity, db, articleId, edt, parentComment)
                )
                .setNegativeButton("Hủy", null)
                .show();
    }

    // THÔNG BÁO REPLY LƯU VÀO users/{uid}/notifications
    private static void createNotificationForReply(FirebaseFirestore db,
                                                   String targetUserId,
                                                   String articleId,
                                                   String commentId,
                                                   String fromUserId,
                                                   String fromUserName) {

        HashMap<String, Object> noti = new HashMap<>();
        noti.put("type", "comment_reply");
        noti.put("articleId", articleId);
        noti.put("commentId", commentId);
        noti.put("fromUserId", fromUserId);
        noti.put("fromUserName", fromUserName);
        noti.put("createdAt", System.currentTimeMillis());
        noti.put("isRead", false);

        db.collection("users")
                .document(targetUserId)
                .collection("notifications")
                .add(noti);
    }
}
