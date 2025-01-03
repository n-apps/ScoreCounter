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

import ua.napps.scorekeeper.R;
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

        contentView.findViewById(R.id.tv_donate).setOnClickListener(this);
        contentView.findViewById(R.id.tv_open_settings).setOnClickListener(this);
        contentView.findViewById(R.id.tv_request_feature).setOnClickListener(this);
        contentView.findViewById(R.id.tv_rate_app).setOnClickListener(this);
        contentView.findViewById(R.id.tv_about).setOnClickListener(this);
        contentView.findViewById(R.id.tv_share).setOnClickListener(this);
        contentView.findViewById(R.id.title_container).setOnClickListener(this);

        return contentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_open_settings:
                showBottomSheet();
                break;
            case R.id.tv_request_feature:
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:scorekeeper.feedback@gmail.com"));
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"scorekeeper.feedback@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));

                try {
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_rate_app:
                Utilities.rateApp(requireActivity());
                break;
            case R.id.tv_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String packageName = requireActivity().getPackageName();
                String extra = getString(R.string.share_snippet) +
                        ": " +
                        "http://play.google.com/store/apps/details?id=" +
                        packageName + "&referrer=" + packageName;
                shareIntent.putExtra(Intent.EXTRA_TEXT, extra);

                Intent chooserIntent = Intent.createChooser(shareIntent, getString(R.string.setting_share));
                chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                requireActivity().startActivity(chooserIntent);
                break;
            case R.id.tv_about:
                AboutActivity.start(requireActivity());
                break;
            case R.id.title_container:
            case R.id.tv_donate:
                showTipScreen();
                break;
        }
    }

    private void showTipScreen() {
        TipActivity.start(requireActivity());
    }


    private void showBottomSheet() {
        SettingsBottomSheetFragment bottomSheet = new SettingsBottomSheetFragment();
        bottomSheet.show(getParentFragmentManager(), "SettingsBottomSheetFragment");
    }
}
