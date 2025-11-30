package com.example.auranews.models;

public class AppNotification {
    private String id;
    private String type;        // "comment_reply" hoặc "new_post"
    private String articleId;
    private String commentId;
    private String fromUserId;
    private String fromUserName;

    // dùng field "title" để map với Firestore (noti.put("title", ...))
    private String title;

    private long createdAt;
    private boolean isRead;

    public AppNotification() {

    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getArticleId() { return articleId; }

    public void setArticleId(String articleId) { this.articleId = articleId; }

    public String getCommentId() { return commentId; }

    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getFromUserId() { return fromUserId; }

    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserName() { return fromUserName; }

    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }

    public String getTitle() {      // 🔴 thay cho getPostTitle()
        return title;
    }

    public void setTitle(String title) {  // 🔴 thay cho setPostTitle()
        this.title = title;
    }

    public long getCreatedAt() { return createdAt; }

    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }
}
