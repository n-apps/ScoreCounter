package ua.napps.scorekeeper.View;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Utils.PrefUtil;

import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_ALL_COUNTERS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_DICES;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_STAY_AWAKE;

/**
 * Created by novo on 2016-01-02.
 */
public class SettingFragment extends Fragment {

    @Bind(R.id.showDicesBar)
    SwitchCompat showDicesBar;
    @Bind(R.id.stayAwake)
    SwitchCompat stayAwake;
    @Bind(R.id.showAllCounters)
    SwitchCompat showAllCounetrs;

    SettingsUpdatedListener mCallback;

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_title);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
                getActivity().onBackPressed();
            }
        });
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (SettingsUpdatedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SettingsUpdatedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback.onSettingsUpdated();
    }

    @Override
    public void onResume() {
        super.onResume();
        showDicesBar.setChecked(PrefUtil.getBoolean(getContext(), PREFS_SHOW_DICES, false));
        stayAwake.setChecked(PrefUtil.getBoolean(getContext(), PREFS_STAY_AWAKE, true));
        showAllCounetrs.setChecked(PrefUtil.getBoolean(getContext(), PREFS_SHOW_ALL_COUNTERS, true));
    }

    @Override
    public void onPause() {
        super.onPause();
        PrefUtil.putBoolean(getContext(), PREFS_SHOW_DICES, showDicesBar.isChecked());
        PrefUtil.putBoolean(getContext(), PREFS_STAY_AWAKE, stayAwake.isChecked());
        PrefUtil.putBoolean(getContext(), PREFS_SHOW_ALL_COUNTERS, showAllCounetrs.isChecked());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public interface SettingsUpdatedListener {
        void onSettingsUpdated();
    }
}
