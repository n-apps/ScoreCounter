package ua.napps.scorekeeper.utils;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ua.com.napps.scorekeeper.R;

public class DonateAdapter extends BaseAdapter {
    private CharSequence[] labels;
    private CharSequence[] emojis;

    public DonateAdapter(CharSequence[] labels, CharSequence[] emojis) {
        this.labels = labels;
        this.emojis = emojis;
    }

    @Override
    public int getCount() {
        return labels.length;
    }

    @Override
    public Object getItem(int position) {
        return labels[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        @SuppressLint("ViewHolder")
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donation, null);

        TextView title = v.findViewById(R.id.primary);
        TextView emoji = v.findViewById(R.id.emoji);

        title.setText(labels[position]);
        emoji.setText(emojis[position]);
        return (v);
    }
}
