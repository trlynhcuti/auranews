package com.example.auranews.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.auranews.R;
import com.example.auranews.models.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends BaseAdapter {

    private Context context;
    private List<Comment> comments;
    private LayoutInflater inflater;
    private SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView tvUserName, tvCommentTime, tvCommentContent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_comment, parent, false);
            holder = new ViewHolder();
            holder.tvUserName = convertView.findViewById(R.id.tvUserName);
            holder.tvCommentTime = convertView.findViewById(R.id.tvCommentTime);
            holder.tvCommentContent = convertView.findViewById(R.id.tvCommentContent);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Comment c = comments.get(position);

        holder.tvUserName.setText(c.getUserName());
        holder.tvCommentContent.setText(c.getContent());

        if (c.getTimestamp() > 0) {
            holder.tvCommentTime.setText(sdf.format(new Date(c.getTimestamp())));
        } else {
            holder.tvCommentTime.setText("");
        }

        return convertView;
    }
}
