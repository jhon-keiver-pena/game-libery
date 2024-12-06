package com.example.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gamelibery.R;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<CustomSpinnerAdapter.SpinnerItem> {

    public CustomSpinnerAdapter(Context context, List<SpinnerItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);
        }

        SpinnerItem item = getItem(position);
        ImageView icon = convertView.findViewById(R.id.spinner_icon);
        TextView text = convertView.findViewById(R.id.spinner_text);

        if (item != null) {
            icon.setImageResource(item.getIcon());
            text.setText(item.getText());
        }

        return convertView;
    }

    // SpinnerItem clase interna
    public static class SpinnerItem {
        private String text;
        private int icon;

        public SpinnerItem(String text, int icon) {
            this.text = text;
            this.icon = icon;
        }

        public String getText() {
            return text;
        }

        public int getIcon() {
            return icon;
        }
    }
}

