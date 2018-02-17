package ua.napps.scorekeeper.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ua.com.napps.scorekeeper.BuildConfig;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;


public class SettingsFragment extends Fragment {

    private static final String FEEDBACK_EMAIL_ADDRESS = "scorekeeper.feedback@gmail.com";

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_settings, null);
        SwitchCompat stayAwake = contentView.findViewById(R.id.sw_stay_awake);
        contentView.findViewById(R.id.send_feedback).setOnClickListener(v -> {
            startEmailClient();
        });
        final TinyDB settingsDB = new TinyDB(getContext());
        boolean isStayAwake = settingsDB.getBoolean(Constants.SETTINGS_KEEP_SCREEN_ON, true);
        stayAwake.setChecked(isStayAwake);
        stayAwake.setOnCheckedChangeListener(
                (buttonView, isChecked) -> settingsDB.putBoolean(Constants.SETTINGS_KEEP_SCREEN_ON, isChecked));
        return contentView;
    }

    private void startEmailClient() {
        final String title = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;

        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", FEEDBACK_EMAIL_ADDRESS, null));
        intent.putExtra(Intent.EXTRA_EMAIL, FEEDBACK_EMAIL_ADDRESS);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        startActivity(Intent.createChooser(intent, getString(R.string.dialog_feedback_title)));
        AndroidFirebaseAnalytics.logEvent(getContext(), "settings_send_feedback");
    }
}
