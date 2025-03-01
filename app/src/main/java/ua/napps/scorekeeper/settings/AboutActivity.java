package ua.napps.scorekeeper.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ua.napps.scorekeeper.R;
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
        getSupportActionBar().setTitle("");

        ((TextView) findViewById(R.id.tv_about_text)).setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.image_hero).setOnClickListener(v -> Toast.makeText(this, "Привіт з України \uD83C\uDDFA\uD83C\uDDE6", Toast.LENGTH_SHORT).show());

        findViewById(R.id.hero_image).setOnClickListener(v -> {
            showTipScreen();
        });
        findViewById(R.id.btn_donate_it).setOnClickListener(v -> {
            showTipScreen();
        });
        findViewById(R.id.btn_help_translate).setOnClickListener(v -> launchEmailClient());
        findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> {
                    Intent viewIntent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/score-counter-privacy-policy/home"));
                    try {
                        startActivity(viewIntent);
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        ViewUtil.setLightMode(this, true);
        ViewUtil.setNavBarColor(this, true, Color.parseColor("#86CEBE"));
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.parseColor("#86CEBE"));
    }

    private void showTipScreen() {
        TipActivity.start(this);
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
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
