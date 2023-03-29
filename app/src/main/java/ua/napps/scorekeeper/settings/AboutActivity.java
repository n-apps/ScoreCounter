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
        getSupportActionBar().setTitle("\uD83D\uDC4B");

        ((TextView) findViewById(R.id.content)).setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.image_hero).setOnClickListener(v -> Toast.makeText(this, "Привіт з України \uD83C\uDDFA\uD83C\uDDE6!", Toast.LENGTH_SHORT).show());

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
        findViewById(R.id.translators).setOnClickListener(v -> {
            Intent s = new Intent(Intent.ACTION_SENDTO);
            s.setData(Uri.parse("mailto:scorekeeper.feedback@gmail.com"));
            s.putExtra(Intent.EXTRA_EMAIL, "scorekeeper.feedback@gmail.com");
            s.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            s.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_help_translate));

            try {
                startActivity(s);
            } catch (Exception e) {
                Toast.makeText(this, R.string.error_no_email_client, Toast.LENGTH_SHORT).show();
                Timber.e(e, "Launch email intent");
            }
        });

        boolean isLightTheme = LocalSettings.isLightTheme();
        if (isLightTheme) {
            ViewUtil.setLightStatusBar(this);
        } else {
            ViewUtil.clearLightStatusBar(this);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
