package com.example.auranews.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.auranews.R;
import com.example.auranews.models.AppNotification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends ArrayAdapter<AppNotification> {

    public interface OnNotificationDeleteListener {
        void onDelete(AppNotification noti);
    }

    private final LayoutInflater inflater;
    private OnNotificationDeleteListener deleteListener;

    public NotificationAdapter(Context context,
                               List<AppNotification> list,
                               OnNotificationDeleteListener listener) {
        super(context, 0, list);
        inflater = LayoutInflater.from(context);
        this.deleteListener = listener;
    }

    public void setOnNotificationDeleteListener(OnNotificationDeleteListener listener) {
        this.deleteListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppNotification noti = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_notification, parent, false);
        }

        TextView tvMsg = convertView.findViewById(R.id.tvNotifMessage);
        TextView tvTime = convertView.findViewById(R.id.tvNotifTime);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteNotif);

        btnDelete.setFocusable(false);
        btnDelete.setFocusableInTouchMode(false);

        if (noti != null) {
            String message;

            if ("comment_reply".equals(noti.getType())) {
                String fromName = noti.getFromUserName();
                message = fromName + " đã trả lời bình luận của bạn";
            } else if ("new_post".equals(noti.getType())) {
                String title = noti.getTitle();
                if (title == null) title = "";
                message = "Bài viết mới: " + title;
            } else {
                message = "Thông báo";
            }

            tvMsg.setText(message);

            long ts = noti.getCreatedAt();
            if (ts > 0) {
                SimpleDateFormat sdf =
                        new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvTime.setText(sdf.format(new Date(ts)));
            } else {
                tvTime.setText("");
            }

            // Xử lý nút xoá
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(noti);
                }
            });
        }

        return convertView;
    }
}
