package ua.napps.scorekeeper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.ColorUtil;
import ua.napps.scorekeeper.utils.DonateDialog;
import ua.napps.scorekeeper.utils.Utilities;

public class AboutActivity extends AppCompatActivity {

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent);
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

        TypedValue primaryColor = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, primaryColor, true);
        int colorPrimary = primaryColor.data;
        if (LocalSettings.isLightTheme()) {
            ((ImageView) findViewById(R.id.cover)).setColorFilter(colorPrimary, PorterDuff.Mode.MULTIPLY);
        } else {
            ((ImageView) findViewById(R.id.cover)).setColorFilter(colorPrimary, PorterDuff.Mode.SCREEN);
        }
        getWindow().setStatusBarColor(colorPrimary);
        if (Utilities.hasOreo()) {
            int oldFlags = getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            if (!ColorUtil.isDarkBackground(colorPrimary)) {
                newFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                newFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            getWindow().setNavigationBarColor(colorPrimary);
            if (newFlags != oldFlags) {
                getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
        }

        findViewById(R.id.tv_rate_app).setOnClickListener(v -> {
            Utilities.rateApp(this);
        });
        findViewById(R.id.tv_donation).setOnClickListener(v -> {
            DonateDialog dialog = new DonateDialog();
            dialog.show(getSupportFragmentManager(), "donate");
        });
        findViewById(R.id.tv_feedback).setOnClickListener(v -> {
            Utilities.startEmail(this);
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
