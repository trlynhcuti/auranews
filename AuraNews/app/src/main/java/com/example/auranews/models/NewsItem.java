package com.example.auranews.models;

import com.google.firebase.firestore.Exclude;

public class NewsItem {
    private String id;
    private String title;
    private String content;
    private String category;
    private String thumbnail; // URL ảnh
    private String preview;
    private long timestamp;
    private long views;
    public NewsItem() { }

    // 2. Constructor đầy đủ
    public NewsItem(String title, String content, String category, String thumbnail, String preview, long timestamp, long views) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.thumbnail = thumbnail;
        this.preview = preview;
        this.timestamp = timestamp;
        this.views = views;
    }

    // 3. Getter & Setter
    @Exclude // Không lưu ID vào field data
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getViews() { return views; }
    public void setViews(long views) { this.views = views; }

    public int getNumberOfVisits() { return (int) views; }
}