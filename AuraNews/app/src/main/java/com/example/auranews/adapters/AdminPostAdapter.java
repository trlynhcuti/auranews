package com.example.auranews.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.auranews.models.NewsItem;
import com.example.auranews.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdminPostAdapter extends ArrayAdapter<NewsItem> {

    private final Context context;
    private final List<NewsItem> newsList;
    private final OnAdminActionListener listener;

    public interface OnAdminActionListener {
        void onEdit(NewsItem newsItem);
        void onDelete(NewsItem newsItem);
    }

    public AdminPostAdapter(@NonNull Context context, @NonNull List<NewsItem> newsList, OnAdminActionListener listener) {
        super(context, R.layout.item_post_manager, newsList);
        this.context = context;
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_post_manager, parent, false);
            holder = new ViewHolder();
            // Ánh xạ các view từ layout item_post_manager.xml
            holder.img = convertView.findViewById(R.id.imageView_news);
            holder.tvCat = convertView.findViewById(R.id.textView_category);
            holder.tvTitle = convertView.findViewById(R.id.textView_title);
            holder.tvPreview = convertView.findViewById(R.id.textView_preview);
            holder.tvViews = convertView.findViewById(R.id.tv_number_of_visits);
            holder.btnEdit = convertView.findViewById(R.id.btn_edit);
            holder.btnDelete = convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NewsItem item = newsList.get(position);

        // 1. Gán dữ liệu cơ bản
        holder.tvCat.setText(item.getCategory() != null ? "Danh mục: " + item.getCategory() : "Danh mục: Khác");
        holder.tvTitle.setText(item.getTitle() != null ? item.getTitle() : "Không có tiêu đề");
        holder.tvViews.setText(item.getViews() + " lượt xem");

        // 2. XỬ LÝ TÓM TẮT (PREVIEW)
        String textToShow = item.getPreview(); // Lấy dữ liệu tóm tắt từ Firebase

        // Nếu tóm tắt trống -> Lấy nội dung chính cắt ra làm tóm tắt
        if (textToShow == null || textToShow.trim().isEmpty()) {
            String content = item.getContent();
            if (content != null && !content.isEmpty()) {
                // Cắt 50 ký tự đầu tiên
                if (content.length() > 50) {
                    textToShow = content.substring(0, 50) + "...";
                } else {
                    textToShow = content;
                }
            } else {
                textToShow = "Chưa có nội dung.";
            }
        }
        holder.tvPreview.setText(textToShow);

        // 3. LOAD ẢNH (Có xử lý HTTP -> HTTPS)
        String urlAnh = item.getThumbnail();
        if (urlAnh != null && !urlAnh.isEmpty()) {
            if (urlAnh.startsWith("http://")) {
                urlAnh = urlAnh.replace("http://", "https://");
            }
            Picasso.get().load(urlAnh)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .fit().centerCrop()
                    .into(holder.img);
        } else {
            holder.img.setImageResource(R.drawable.ic_image_placeholder);
        }

        // 4. Sự kiện click
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));

        return convertView;
    }

    static class ViewHolder {
        ImageView img;
        TextView tvCat, tvTitle, tvViews, tvPreview; // <-- Đã thêm biến tvPreview
        ImageButton btnEdit, btnDelete;
    }
}