package ua.napps.scorekeeper.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
        ImageView image = v.findViewById(R.id.image);
        switch (position) {
            default:
            case 0:  // coffee
                title.setText(R.string.donation_coffee_title);
                description.setText(R.string.donation_coffee_description);
                image.setImageResource(R.drawable.inapp_coffee);
                break;
            case 1: // pizza
                title.setText(R.string.donation_pizza_title);
                description.setText(R.string.donation_pizza_description);
                image.setImageResource(R.drawable.inapp_food);
                break;
            case 2:
                title.setText(R.string.donation_xwing_title);
                description.setText(R.string.donation_xwing_description);
                image.setImageResource(R.drawable.inapp_miniature);
                break;
        }

        return (v);
    }
}
