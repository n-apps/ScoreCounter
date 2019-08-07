package ua.napps.scorekeeper.utils;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ua.com.napps.scorekeeper.R;

public class DonateAdapter extends BaseAdapter {
    private ArrayList<CharSequence> labels;
    private ArrayList<CharSequence> emojis;

    public DonateAdapter() {
        labels = new ArrayList<>();
        emojis = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return labels.size();
    }

    @Override
    public Object getItem(int position) {
        return labels.get(position);
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

        title.setText(labels.get(position));
        emoji.setText(emojis.get(position));
        return (v);
    }

    public void addDonateInfo(@Nullable String emoji, @NonNull String title) {
        labels.add(title);
        emojis.add(emoji != null ? emoji : "☕");
    }

}
