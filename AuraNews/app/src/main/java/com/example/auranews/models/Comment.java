package com.example.auranews.models;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String content;
    private long timestamp;

    private String parentId;        // null / "" nếu comment gốc
    private String replyToUserId;   // người bị reply
    private String replyToUserName;

    public Comment() {}

    public Comment(String userId, String userName, String content, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Comment(String userId, String userName, String content,
                   long timestamp, String parentId,
                   String replyToUserId, String replyToUserName) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = timestamp;
        this.parentId = parentId;
        this.replyToUserId = replyToUserId;
        this.replyToUserName = replyToUserName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(String replyToUserId) { this.replyToUserId = replyToUserId; }

    public String getReplyToUserName() { return replyToUserName; }
    public void setReplyToUserName(String replyToUserName) { this.replyToUserName = replyToUserName; }
}
