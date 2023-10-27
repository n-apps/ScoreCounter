package ua.napps.scorekeeper.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import timber.log.Timber;
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
        contentView.findViewById(R.id.tv_about).setOnClickListener(this);
        contentView.findViewById(R.id.tv_share).setOnClickListener(this);
        contentView.findViewById(R.id.tv_support).setOnClickListener(this);

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
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:scorekeeper.feedback@gmail.com"));
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"scorekeeper.feedback@gmail.com"});
                if (v.getId() == R.id.tv_help_translate) {
                    String s = getString(R.string.app_name) + " – " + getString(R.string.setting_help_translate);
                    i.putExtra(Intent.EXTRA_SUBJECT, s);
                } else {
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                }

                try {
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
                    Timber.e(e, "Launch email intent");
                }
                break;
            case R.id.tv_rate_app:
                Utilities.rateApp(requireActivity());
                break;
            case R.id.iv_donate:
                DonateDialog dialog = new DonateDialog();
                dialog.show(getParentFragmentManager(), "donate");
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
            case R.id.tv_support:
                new RateMyAppDialog(requireActivity()).showAnyway();
                break;
        }
    }


    private void showBottomSheet() {
        SettingsBottomSheetFragment bottomSheet = new SettingsBottomSheetFragment();
        bottomSheet.show(getParentFragmentManager(), "SettingsBottomSheetFragment");
    }
}
