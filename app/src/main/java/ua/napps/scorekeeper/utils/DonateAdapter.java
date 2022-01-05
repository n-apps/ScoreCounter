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
        return 3;
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
        switch (position) {
            default:
            case 0:  // coffee
                title.setText("\u2615 " + context.getString(R.string.donation_coffee_title));
                description.setText(context.getString(R.string.donation_coffee_description));
                break;
            case 1: // pizza
                title.setText("\uD83C\uDF55 " + context.getString(R.string.donation_pizza_title));
                description.setText(context.getString(R.string.donation_pizza_description));
                break;
            case 2:
                    title.setText("\uD83D\uDE80 " +context.getString(R.string.donation_xwing_title));
                description.setText(context.getString(R.string.donation_xwing_description));
                break;
        }

        return (v);
    }
}
