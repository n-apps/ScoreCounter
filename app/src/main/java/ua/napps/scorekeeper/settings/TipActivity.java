package ua.napps.scorekeeper.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.android.billingclient.api.ProductDetails;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.DonateViewModel;
import ua.napps.scorekeeper.utils.ViewUtil;
import ua.napps.scorekeeper.utils.livedata.CloseScreenIntent;
import ua.napps.scorekeeper.utils.livedata.MessageIntent;

public class TipActivity extends AppCompatActivity {

    private static final int COFFEE_IN_APP = 0;
    private static final int FOOD_IN_APP = 1;
    private static final int XWING_IN_APP = 2;

    private DonateViewModel viewModel;
    private MaterialCardView coffee_cardview, food_cardview, xwing_cardview;
    private int selected_in_app;


    public static void start(Activity activity) {
        Intent intent = new Intent(activity, TipActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        ViewUtil.setLightMode(this, false);
        ViewUtil.setNavBarColor(this, false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        int color = ContextCompat.getColor(this, R.color.colorPrimaryVariant);
        window.setStatusBarColor(color);

        observeData();

        coffee_cardview = findViewById(R.id.in_app_coffee_card);
        food_cardview = findViewById(R.id.in_app_food_card);
        xwing_cardview = findViewById(R.id.in_app_xwing_card);

        food_cardview.setChecked(true);
        selected_in_app = FOOD_IN_APP;

        coffee_cardview.setOnClickListener(view -> {
            food_cardview.setChecked(false);
            xwing_cardview.setChecked(false);
            coffee_cardview.setChecked(true);
            selected_in_app = COFFEE_IN_APP;
        });

        food_cardview.setOnClickListener(view -> {
            coffee_cardview.setChecked(false);
            xwing_cardview.setChecked(false);
            food_cardview.setChecked(true);
            selected_in_app = FOOD_IN_APP;
        });

        xwing_cardview.setOnClickListener(view -> {
            coffee_cardview.setChecked(false);
            food_cardview.setChecked(false);
            xwing_cardview.setChecked(true);
            selected_in_app = XWING_IN_APP;
        });

        findViewById(R.id.btn_one_time).setOnClickListener(v -> viewModel.launchPurchaseFlow(TipActivity.this, selected_in_app, true));
        findViewById(R.id.btn_monthly).setOnClickListener(v -> viewModel.launchPurchaseFlow(TipActivity.this, selected_in_app, false));
        findViewById(R.id.btn_remind_later).setOnClickListener(v -> finish());
    }

    private void observeData() {
        viewModel = new ViewModelProvider(this).get(DonateViewModel.class);
        viewModel.oneTimeDetailsList.observe(this, this::updateUI);

        viewModel.eventBus.observe(this, event -> {
            Object intent = event.getValueAndConsume();
            if (intent instanceof MessageIntent) {
                int messageResId = ((MessageIntent) intent).messageResId;
                Toast.makeText(TipActivity.this, messageResId, Toast.LENGTH_SHORT).show();
            } else if (intent instanceof CloseScreenIntent) {
                int messageResId = ((CloseScreenIntent) intent).resultMessageResId;
                boolean dueToError = ((CloseScreenIntent) intent).dueToError;
                if (dueToError) {
                    Toast.makeText(getApplicationContext(), messageResId, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), messageResId, Toast.LENGTH_LONG).show();
                    LocalSettings.markDonated();
                }
                TipActivity.this.finish();
            }
        });
    }

    private void updateUI(List<ProductDetails> productDetails) {
        if (productDetails.isEmpty()) return;

        for (ProductDetails p : productDetails) {
            String name = p.getName();
            String price = p.getOneTimePurchaseOfferDetails().getFormattedPrice();

            switch (p.getProductId()) {
                default:
                case "buy_me_a_coffee":
                    ((TextView) findViewById(R.id.in_app_coffee_title)).setText(name);
                    ((TextView) findViewById(R.id.in_app_coffee_price)).setText(price);
                    break;
                case "buy_me_a_pizza":
                    ((TextView) findViewById(R.id.in_app_food_title)).setText(name);
                    ((TextView) findViewById(R.id.in_app_food_price)).setText(price);
                    break;
                case "buy_me_a_xwing":
                    ((TextView) findViewById(R.id.in_app_xwing_title)).setText(name);
                    ((TextView) findViewById(R.id.in_app_xwing_price)).setText(price);
                    break;
            }
        }
    }

}
