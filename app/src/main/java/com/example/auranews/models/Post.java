package com.example.auranews.models;

import java.util.HashMap;
import java.util.Map;

public class Post {
    private String title;
    private String content;
    private String category;
    private String thumbnail;
    private String authorId;
    private String authorEmail;

    public Post(String title, String content, String category,
                String thumbnail, String authorId, String authorEmail) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.thumbnail = thumbnail;
        this.authorId = authorId;
        this.authorEmail = authorEmail;
    }

    private String buildPreview() {
        if (content == null) return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }

    // Map dùng khi TẠO MỚI
    public Map<String, Object> toCreateMap() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("category", category);
        data.put("thumbnail", thumbnail);
        data.put("preview", buildPreview());
        data.put("authorId", authorId);
        data.put("authorEmail", authorEmail);
        data.put("commentsCount", 0);
        data.put("timestamp", System.currentTimeMillis());
        data.put("views", 0);
        return data;
    }

    // Map dùng khi CẬP NHẬT
    public Map<String, Object> toUpdateMap() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("category", category);
        data.put("thumbnail", thumbnail);
        data.put("preview", buildPreview());
        data.put("authorId", authorId);
        data.put("authorEmail", authorEmail);

        return data;
    }
}
