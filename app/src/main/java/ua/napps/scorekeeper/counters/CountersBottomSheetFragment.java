package ua.napps.scorekeeper.counters;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment;

import org.jetbrains.annotations.NotNull;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;

public class CountersBottomSheetFragment extends SuperBottomSheetFragment implements View.OnClickListener {

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @org.jetbrains.annotations.Nullable ViewGroup container, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_counters_sheet, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int oldFlags = getDialog().getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            if (LocalSettings.isLightTheme()) {
                newFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                getDialog().getWindow().setNavigationBarColor(Color.WHITE);
            } else {
                newFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                getDialog().getWindow().setNavigationBarColor(ContextCompat.getColor(requireActivity(), R.color.primaryBackground));
            }
            if (newFlags != oldFlags) {
                getDialog().getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
        }
//        AndroidFirebaseAnalytics.logEvent("CountersBottomSheetScreenAppear");
    }

    @Override
    public void onClick(View v) {
    }

}
