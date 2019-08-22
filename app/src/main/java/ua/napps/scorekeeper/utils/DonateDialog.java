package ua.napps.scorekeeper.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import ua.com.napps.scorekeeper.R;

public class DonateDialog extends DialogFragment {

    private DonateViewModel viewModel;
    private DonateAdapter adapter;
    private final Observer<ConsumableEvent> eventObserver = consumableEvent -> {
        Object event = consumableEvent.getPayloadIfNotConsumed();
        if (DonateViewModel.EVENT_DISMISS == event) {
            DonateDialog.this.dismiss();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(DonateViewModel.class);
        adapter = new DonateAdapter(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.events.observeForever(eventObserver);
    }

    @Override
    public void onPause() {
        viewModel.events.removeObserver(eventObserver);
        super.onPause();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.action_donate)
                .setAdapter(adapter, null)
                .create();
        alertDialog.getListView().setOnItemClickListener((p, v, donateOption, id) -> viewModel.purchase(requireActivity(), donateOption));
        return alertDialog;
    }
}
