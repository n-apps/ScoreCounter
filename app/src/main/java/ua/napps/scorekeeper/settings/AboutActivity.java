package ua.napps.scorekeeper.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.DonateDialog;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;

public class AboutActivity extends AppCompatActivity {

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Привіт!");

        ((TextView) findViewById(R.id.content)).setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.image_hero).setOnClickListener(v -> Toast.makeText(this, "Привіт з України \uD83C\uDDFA\uD83C\uDDE6", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btn_rate_it).setOnClickListener(v -> {
            Utilities.rateApp(this);
        });
        findViewById(R.id.hero_image).setOnClickListener(v -> {
            DonateDialog dialog = new DonateDialog();
            dialog.show(getSupportFragmentManager(), "donate");
        });
        findViewById(R.id.btn_donate_it).setOnClickListener(v -> {
            DonateDialog dialog = new DonateDialog();
            dialog.show(getSupportFragmentManager(), "donate");
        });
        findViewById(R.id.btn_buy_me_a_coffee).setOnClickListener(v -> {
            Intent viewIntent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/score_counter_app"));
            try {
                startActivity(viewIntent);
            } catch (Exception e) {
                Toast.makeText(this, R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
                Timber.e(e, "Launch web intent");
            }
        });
        findViewById(R.id.btn_help_translate).setOnClickListener(v -> launchEmailClient());
        findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> {
                    Intent viewIntent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/score-counter-privacy-policy/home"));
                    try {
                        startActivity(viewIntent);
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
                        Timber.e(e, "Launch web intent error");
                    }
                }
        );

        boolean nightModeActive = ViewUtil.isNightModeActive(this);
        ViewUtil.setLightMode(this, !nightModeActive);
        ViewUtil.setNavBarColor(this, !nightModeActive);
    }

    private void launchEmailClient() {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:scorekeeper.feedback@gmail.com"));
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"scorekeeper.feedback@gmail.com"});
        String s = getString(R.string.app_name) + ": " + getString(R.string.setting_help_translate);
        i.putExtra(Intent.EXTRA_SUBJECT, s);

        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
            Timber.e(e, "Launch email intent error");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
