package com.example.auranews.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.auranews.R;
import com.example.auranews.models.NewsItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private List<NewsItem> newsList;      // danh sách đang hiển thị
    private List<NewsItem> originalList;  // dữ liệu gốc để tìm kiếm

    public NewsAdapter(Context context, int layout, List<NewsItem> initialList) {
        this.context = context;
        this.layout = layout;

        // Tạo list riêng, KHÔNG dùng chung reference với bên ngoài
        this.newsList = new ArrayList<>();
        this.originalList = new ArrayList<>();

        if (initialList != null) {
            this.newsList.addAll(initialList);
            this.originalList.addAll(initialList);
        }
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        ImageView imgNews;
        TextView txtCategory;
        TextView txtTitle;
        TextView txtPreview;
        TextView txtNumberOfVisits;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_post, parent, false);

            holder = new ViewHolder();
            holder.imgNews = convertView.findViewById(R.id.imageView_news);
            holder.txtCategory = convertView.findViewById(R.id.textView_category);
            holder.txtTitle = convertView.findViewById(R.id.textView_title);
            holder.txtPreview = convertView.findViewById(R.id.textView_preview);
            holder.txtNumberOfVisits = convertView.findViewById(R.id.tv_number_of_visits);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NewsItem newsItem = newsList.get(position);

        // Ảnh
        if (newsItem.getThumbnail() != null && !newsItem.getThumbnail().isEmpty()) {
            Picasso.get()
                    .load(newsItem.getThumbnail())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(holder.imgNews);
        } else {
            holder.imgNews.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Text
        holder.txtCategory.setText("Danh mục: " + newsItem.getCategory());
        holder.txtTitle.setText("Tiêu đề: " + newsItem.getTitle());

        if (newsItem.getPreview() != null && !newsItem.getPreview().isEmpty()) {
            holder.txtPreview.setText("Tóm tắt: " + newsItem.getPreview());
        } else if (newsItem.getContent() != null) {
            String shortContent = newsItem.getContent().length() > 50
                    ? newsItem.getContent().substring(0, 50) + "..."
                    : newsItem.getContent();
            holder.txtPreview.setText("Tóm tắt: " + shortContent);
        } else {
            holder.txtPreview.setText("Tóm tắt: ");
        }

        holder.txtNumberOfVisits.setText(newsItem.getNumberOfVisits() + " lượt truy cập");

        return convertView;
    }

    // Tìm kiếm theo keyword (title, preview, content, category)
    public void filter(String keyword) {
        if (keyword == null) keyword = "";
        String lower = keyword.toLowerCase().trim();

        newsList.clear();

        if (lower.isEmpty()) {
            // Không nhập gì -> trả về full list
            newsList.addAll(originalList);
        } else {
            for (NewsItem item : originalList) {

                boolean match = false;

                // Tiêu đề
                if (item.getTitle() != null &&
                        item.getTitle().toLowerCase().contains(lower)) {
                    match = true;
                }

                // Tóm tắt
                if (!match && item.getPreview() != null &&
                        item.getPreview().toLowerCase().contains(lower)) {
                    match = true;
                }

                // Nội dung
                if (!match && item.getContent() != null &&
                        item.getContent().toLowerCase().contains(lower)) {
                    match = true;
                }

                // Category
                if (!match && item.getCategory() != null &&
                        item.getCategory().toLowerCase().contains(lower)) {
                    match = true;
                }

                if (match) {
                    newsList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Cập nhật lại data (khi load từ Firestore)
    public void updateData(List<NewsItem> newList) {
        if (newList == null) return;

        originalList.clear();
        originalList.addAll(newList);

        newsList.clear();
        newsList.addAll(newList);

        notifyDataSetChanged();
    }
}
