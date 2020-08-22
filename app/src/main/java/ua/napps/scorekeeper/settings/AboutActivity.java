package ua.napps.scorekeeper.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.DonateDialog;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;

public class AboutActivity extends AppCompatActivity {

    public static void start(Activity activity) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        ((TextView) findViewById(R.id.content)).setMovementMethod(LinkMovementMethod.getInstance());

        ImageView cover = findViewById(R.id.cover);
        cover.setOnClickListener(v -> Toast.makeText(getApplicationContext(), R.string.easter_wave, Toast.LENGTH_SHORT).show());

        findViewById(R.id.tv_rate_app).setOnClickListener(v -> {
            Utilities.rateApp(this);
        });
        findViewById(R.id.tv_donation).setOnClickListener(v -> {
            DonateDialog dialog = new DonateDialog();
            dialog.show(getSupportFragmentManager(), "donate");
        });
        findViewById(R.id.tv_feedback).setOnClickListener(v -> Utilities.startEmail(this));
        findViewById(R.id.tv_mypage).setOnClickListener(v -> {
            Intent viewIntent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse("https://n-apps.github.io/"));
            startActivity(viewIntent);
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
