package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.expenseutility.R;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {

    private Context context;
    private List<String> items,subItems;
    private int[] images;

    public CustomListAdapter(Context context, List<String> items, int[] images, List<String> subItems) {
        this.context = context;
        this.items = items;
        this.images = images;
        this.subItems = subItems;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.item_text);
            holder.subItemTextView = convertView.findViewById(R.id.sub_item_text);
            holder.imageView = convertView.findViewById(R.id.item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(items.get(position));
        holder.subItemTextView.setText(subItems.get(position));
        holder.imageView.setImageResource(images[position]);

        return convertView;
    }

    private static class ViewHolder {
        TextView textView,subItemTextView;
        ImageView imageView;
    }
}

