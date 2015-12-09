package ua.napps.scorekeeper.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.apkfuns.logutils.LogUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_NAME;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_DICES;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_STAY_AWAKE;
import static ua.napps.scorekeeper.Helpers.Constants.SEND_REPORT_EMAIL;

public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.showDicesBar)
    SwitchCompat showDicesBar;
    @Bind(R.id.stayAwake)
    SwitchCompat stayAwake;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 18) getWindow().addFlags(FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.settings_title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.i("onResume");
        LogUtils.i("access to SharedPreferences");
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        showDicesBar.setChecked(sp.getBoolean(PREFS_SHOW_DICES, false));
        stayAwake.setChecked(sp.getBoolean(PREFS_STAY_AWAKE, true));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.i("onPause");
        LogUtils.i("access to SharedPreferences");
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREFS_SHOW_DICES, showDicesBar.isChecked());
        editor.putBoolean(PREFS_STAY_AWAKE, stayAwake.isChecked());
        editor.apply();
    }

    public void onClickSendReport(View v) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + SEND_REPORT_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        startActivity(intent);
    }
}
