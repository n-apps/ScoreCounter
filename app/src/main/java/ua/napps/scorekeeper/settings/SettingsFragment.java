package ua.napps.scorekeeper.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.marcoscg.xmassnow.XmasSnow;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.DonateDialog;
import ua.napps.scorekeeper.utils.RateMyAppDialog;
import ua.napps.scorekeeper.utils.Utilities;


public class SettingsFragment extends Fragment implements View.OnClickListener {

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


        contentView.findViewById(R.id.iv_donate).setOnClickListener(this);
        contentView.findViewById(R.id.tv_open_settings).setOnClickListener(this);

        contentView.findViewById(R.id.tv_request_feature).setOnClickListener(this);
        contentView.findViewById(R.id.tv_help_translate).setOnClickListener(this);
        contentView.findViewById(R.id.tv_rate_app).setOnClickListener(this);
        contentView.findViewById(R.id.tv_privacy_policy).setOnClickListener(this);
        contentView.findViewById(R.id.tv_about).setOnClickListener(this);
        contentView.findViewById(R.id.tv_share).setOnClickListener(this);
        contentView.findViewById(R.id.tv_easter).setOnClickListener(this);


        XmasSnow.on(requireActivity())
                .belowActionBar(false)
                .belowStatusBar(false) // Always true if belowActionBar() is set to true
                .onlyOnChristmas(false) // Only the 25th of december
                .setInterval("12/20/2022", "1/15/2023") // 25th of december to 7th of january (not included). Date format: MM/dd/yyyy
                .start();

        return contentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_open_settings:
                showBottomSheet();
                break;

            case R.id.tv_request_feature:
            case R.id.tv_help_translate:
                Utilities.startEmail(requireContext());
                break;
            case R.id.tv_rate_app:
                Utilities.rateApp(requireActivity());
                break;
            case R.id.iv_donate:
                DonateDialog dialog = new DonateDialog();
                dialog.show(getParentFragmentManager(), "donate");
                break;
            case R.id.tv_privacy_policy:
                Intent viewIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/score-counter-privacy-policy/home"));
                startActivity(viewIntent);
                break;
            case R.id.tv_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_snippet) + requireActivity().getPackageName());
                Intent chooserIntent = Intent.createChooser(shareIntent, getString(R.string.setting_share));
                chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                requireActivity().startActivity(chooserIntent);
                break;
            case R.id.tv_about:
                AboutActivity.start(requireActivity());
                break;
            case R.id.tv_easter:
                new RateMyAppDialog(requireActivity()).showAnyway();
                break;
        }
    }


    private void showBottomSheet() {
        SettingsBottomSheetFragment bottomSheet = new SettingsBottomSheetFragment();
        bottomSheet.show(getParentFragmentManager(), "SettingsBottomSheetFragment");
    }
}
