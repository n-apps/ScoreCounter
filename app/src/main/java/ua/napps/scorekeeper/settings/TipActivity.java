package ua.napps.scorekeeper.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import com.android.billingclient.api.ProductDetails;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;
import nl.dionsegijn.konfetti.xml.listeners.OnParticleSystemUpdateListener;
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
    private View container;
    private TextView tv_thanks;
    private CoordinatorLayout root;
    private KonfettiView konfettiView;


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

        ViewUtil.setLightMode(this, true);
        ViewUtil.setNavBarColor(this, true);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.parseColor("#86CEBE"));

        observeData();

        coffee_cardview = findViewById(R.id.in_app_coffee_card);
        food_cardview = findViewById(R.id.in_app_food_card);
        xwing_cardview = findViewById(R.id.in_app_xwing_card);
        konfettiView = findViewById(R.id.konfetti_view);
        container = findViewById(R.id.container);
        root = findViewById(R.id.root);
        tv_thanks = findViewById(R.id.tv_thanks);

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
                if (!dueToError) {
                    // in-app success
                    LocalSettings.markDonated();

                    //confetti
                    showParty();
                } else {
                    Toast.makeText(getApplicationContext(), messageResId, Toast.LENGTH_SHORT).show();
                    TipActivity.this.finish();
                }

            }
        });
    }

    private void showParty() {
        EmitterConfig emitterConfig = new Emitter(3, TimeUnit.SECONDS).perSecond(30);

        konfettiView.setOnParticleSystemUpdateListener(new OnParticleSystemUpdateListener() {
            @Override
            public void onParticleSystemStarted(@NonNull KonfettiView konfettiView, @NonNull Party party, int i) {
                TransitionManager.beginDelayedTransition(root, new Fade());
                container.setVisibility(View.INVISIBLE);
                tv_thanks.setVisibility(View.VISIBLE);
            }

            @Override
            public void onParticleSystemEnded(@NonNull KonfettiView konfettiView, @NonNull Party party, int i) {
                TipActivity.this.finish();
            }
        });
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.RIGHT - 45)
                        .spread(Spread.WIDE)
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Position.Relative(0.0, 0.3))
                        .build(),
                new PartyFactory(emitterConfig)
                        .angle(Angle.LEFT + 45)
                        .spread(Spread.WIDE)
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Position.Relative(1.0, 0.4))
                        .build());
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
