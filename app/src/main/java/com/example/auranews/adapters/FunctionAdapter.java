package com.example.auranews.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.auranews.models.FunctionItem;
import com.example.auranews.R;

import java.util.List;

public class FunctionAdapter extends BaseAdapter {
    Context context;
    List<FunctionItem> list;

    public FunctionAdapter(Context context, List<FunctionItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.lv_adminmain, parent, false);

            ImageView imgIcon = view.findViewById(R.id.imgIcon);
            TextView txtTitle = view.findViewById(R.id.txtTitle);

            FunctionItem item = list.get(i);

            imgIcon.setImageResource(item.getIcon());
            txtTitle.setText(item.getText());
        }
        return view;
    }
}
