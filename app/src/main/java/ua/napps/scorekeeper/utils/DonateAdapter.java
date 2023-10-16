package ua.napps.scorekeeper.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.android.billingclient.api.ProductDetails;

import java.util.List;

import ua.napps.scorekeeper.R;

public class DonateAdapter extends BaseAdapter {

    private final Context context;
    private List<ProductDetails> productDetails;

    DonateAdapter(Context context, List<ProductDetails> data) {
        this.context = context;
        productDetails = data;
    }

    @Override
    public int getCount() {
        return productDetails.size();
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
        final View v = LayoutInflater.from(context).inflate(R.layout.item_donation, null);

        if (!productDetails.isEmpty()) {
            ProductDetails productDetail = productDetails.get(position);
            int imageResource = getIconForInApp(productDetail);
            String name = productDetail.getName();
            String price = productDetail.getOneTimePurchaseOfferDetails().getFormattedPrice();

            ((ImageView) v.findViewById(R.id.image)).setImageResource(imageResource);
            ((TextView) v.findViewById(R.id.title)).setText(name);
            ((TextView) v.findViewById(R.id.price)).setText(price);
        }

        return (v);
    }

    @DrawableRes
    private int getIconForInApp(ProductDetails productDetail) {
        switch (productDetail.getProductId()) {
            default:
            case "buy_me_a_coffee":
                return R.drawable.in_app_coffee;
            case "buy_me_a_pizza":
                return R.drawable.in_app_food;
            case "buy_me_a_xwing":
                return R.drawable.in_app_xwing;
        }
    }

    public void updateData(List<ProductDetails> data) {
        productDetails = data;
        notifyDataSetChanged();
    }
}
