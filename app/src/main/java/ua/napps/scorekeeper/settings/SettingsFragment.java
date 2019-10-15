package ua.napps.scorekeeper.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.DonateDialog;
import ua.napps.scorekeeper.utils.Utilities;


public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private Button btn_c_1, btn_c_2, btn_c_3, btn_c_4;

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
        SwitchCompat keepScreenOn = contentView.findViewById(R.id.sw_keep_screen_on);
        SwitchCompat darkTheme = contentView.findViewById(R.id.sw_dark_theme);

        btn_c_1 = contentView.findViewById(R.id.btn_settings_counter_1);
        btn_c_2 = contentView.findViewById(R.id.btn_settings_counter_2);
        btn_c_3 = contentView.findViewById(R.id.btn_settings_counter_3);
        btn_c_4 = contentView.findViewById(R.id.btn_settings_counter_4);

        keepScreenOn.setChecked(LocalSettings.isKeepScreenOnEnabled());
        darkTheme.setChecked(!LocalSettings.isLightTheme());

        keepScreenOn.setOnCheckedChangeListener(this);
        darkTheme.setOnCheckedChangeListener(this);
        contentView.findViewById(R.id.tv_request_feature).setOnClickListener(this);
        contentView.findViewById(R.id.tv_rate_app).setOnClickListener(this);
        contentView.findViewById(R.id.tv_privacy_policy).setOnClickListener(this);
        contentView.findViewById(R.id.tv_github).setOnClickListener(this);
        contentView.findViewById(R.id.tv_donation).setOnClickListener(this);

        btn_c_1.setOnClickListener(this);
        btn_c_2.setOnClickListener(this);
        btn_c_3.setOnClickListener(this);
        btn_c_4.setOnClickListener(this);

        btn_c_1.setText(String.valueOf(LocalSettings.getCustomCounter(1)));
        btn_c_2.setText(String.valueOf(LocalSettings.getCustomCounter(2)));
        btn_c_3.setText(String.valueOf(LocalSettings.getCustomCounter(3)));
        btn_c_4.setText(String.valueOf(LocalSettings.getCustomCounter(4)));

        if(Utilities.hasQ()) {
            darkTheme.setVisibility(View.GONE);
            contentView.findViewById(R.id.iv_dark_theme).setVisibility(View.GONE);
        }

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidFirebaseAnalytics.trackScreen(requireActivity(), "Settings", getClass().getSimpleName());
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        switch (v.getId()) {
            case R.id.sw_keep_screen_on:
                LocalSettings.saveKeepScreenOn(isChecked);
                break;
            case R.id.sw_dark_theme:
                LocalSettings.saveDarkTheme(isChecked);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_request_feature:
                AndroidFirebaseAnalytics.logEvent("SettingsScreenFeedbackClick");
                startEmailClient();
                break;
            case R.id.tv_rate_app:
                AndroidFirebaseAnalytics.logEvent("SettingsScreenRateAppClick");
                Utilities.rateApp(requireActivity());
                break;
            case R.id.tv_donation:
                AndroidFirebaseAnalytics.logEvent("SettingsScreenDonateClick");
                DonateDialog dialog = new DonateDialog();
                dialog.show(requireFragmentManager(), "donate");
                break;
            case R.id.tv_github:
                AndroidFirebaseAnalytics.logEvent("SettingsScreenContributeClick");
                Intent githubIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/n-apps/ScoreCounter/issues"));
                startActivity(githubIntent);
                break;
            case R.id.tv_privacy_policy:
                AndroidFirebaseAnalytics.logEvent("SettingsScreenPrivacyClick");
                Intent viewIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/score-counter-privacy-policy/home"));
                startActivity(viewIntent);
                break;
            case R.id.btn_settings_counter_1:
                openCustomCounterDialog(1);
                break;
            case R.id.btn_settings_counter_2:
                openCustomCounterDialog(2);
                break;
            case R.id.btn_settings_counter_3:
                openCustomCounterDialog(3);
                break;
            case R.id.btn_settings_counter_4:
                openCustomCounterDialog(4);
                break;
        }
    }

    private void openCustomCounterDialog(final int id) {
        final MaterialDialog customCounterDialog = new MaterialDialog.Builder(requireActivity())
                .content(R.string.dialog_custom_counter_title)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                .positiveText(R.string.common_set)
                .negativeColorRes(R.color.primaryColor)
                .negativeText(R.string.common_cancel)
                .alwaysCallInputCallback()
                .input(getString(R.string.dialog_custom_counter_hint), null, false,
                        (dialog, input) -> {
                            int parseInt = Utilities.parseInt(input.toString());
                            if (parseInt <= 999 && parseInt > 1) {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                            } else {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            }
                        })
                .onPositive((dialog, which) -> {
                    EditText editText = dialog.getInputEditText();
                    if (editText != null) {
                        String value = editText.getText().toString();
                        Integer parseInt = Utilities.parseInt(value);
                        if (parseInt <= 999 && parseInt > 1) {
                            setCustomCounter(id, parseInt);
                        }
                        dialog.dismiss();
                        Bundle params = new Bundle();
                        params.putString(FirebaseAnalytics.Param.CHARACTER, value);
                        AndroidFirebaseAnalytics.logEvent("SettingsScreenCounterDialogValueSubmit", params);
                    }
                })
                .build();

        EditText editText = customCounterDialog.getInputEditText();
        if (editText != null) {
            editText.setOnEditorActionListener((textView, actionId, event) -> {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    View positiveButton = customCounterDialog.getActionButton(DialogAction.POSITIVE);
                    positiveButton.callOnClick();
                }
                return false;
            });
        }
        customCounterDialog.show();
    }

    private void setCustomCounter(int id, int value) {
        LocalSettings.saveCustomCounter(id, value);
        switch (id) {
            case 1:
                btn_c_1.setText(String.valueOf(value));
                break;
            case 2:
                btn_c_2.setText(String.valueOf(value));
                break;
            case 3:
                btn_c_3.setText(String.valueOf(value));
                break;
            case 4:
                btn_c_4.setText(String.valueOf(value));
                break;
        }
    }

    private void startEmailClient() {
        final String title = getString(R.string.app_name);

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "scorekeeper.feedback@gmail.com", null));
        intent.putExtra(Intent.EXTRA_EMAIL, "scorekeeper.feedback@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        if (intent.resolveActivity(App.getInstance().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), R.string.error_no_email_client, Toast.LENGTH_SHORT).show();
        }
    }
}
