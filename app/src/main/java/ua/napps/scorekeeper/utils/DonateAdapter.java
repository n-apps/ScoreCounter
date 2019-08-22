package ua.napps.scorekeeper.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ua.napps.scorekeeper.R;

public class DonateAdapter extends BaseAdapter {

    private final Context context;

    DonateAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        @SuppressLint("ViewHolder")
        View v = LayoutInflater.from(context).inflate(R.layout.item_donation, null);

        TextView title = v.findViewById(R.id.title);
        TextView description = v.findViewById(R.id.description);
        TextView emoji = v.findViewById(R.id.emoji);
        if (position == 0) { // coffee
            title.setText(context.getString(R.string.donation_coffee_title));
            description.setText(context.getString(R.string.donation_coffee_description));
            emoji.setText("\u2615\ufe0f");
        } else { // pizza
            title.setText(context.getString(R.string.donation_pizza_title));
            description.setText(context.getString(R.string.donation_pizza_description));
            emoji.setText("\uD83C\uDF55");
        }

        return (v);
    }
}
