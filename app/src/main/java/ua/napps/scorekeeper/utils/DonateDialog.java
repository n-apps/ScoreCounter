package ua.napps.scorekeeper.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.livedata.CloseScreenIntent;
import ua.napps.scorekeeper.utils.livedata.MessageIntent;
import ua.napps.scorekeeper.utils.livedata.SingleShotEvent;

public class DonateDialog extends DialogFragment {

    private final Observer<SingleShotEvent> eventBusObserver = event -> {
        Object intent = event.getValueAndConsume();
        if (intent instanceof MessageIntent) {
            int messageResId = ((MessageIntent) intent).messageResId;
            Toast.makeText(requireContext().getApplicationContext(), messageResId, Toast.LENGTH_SHORT).show();
        } else if (intent instanceof CloseScreenIntent) {
            int messageResId = ((CloseScreenIntent) intent).resultMessageResId;
            boolean dueToError = ((CloseScreenIntent) intent).dueToError;
            if (dueToError) {
                Toast.makeText(requireContext().getApplicationContext(), messageResId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext().getApplicationContext(), messageResId, Toast.LENGTH_LONG).show();
                LocalSettings.markDonated();
            }
            dismiss();
        }
    };
    private DonateViewModel viewModel;
    private DonateAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DonateViewModel.class);
        adapter = new DonateAdapter(requireContext(), new ArrayList<>());

        viewModel.productDetailsList.observe(requireActivity(), productDetails -> {
            adapter.updateData(productDetails);
            adapter.notifyDataSetChanged();
        });

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.action_donate)
                .setAdapter(adapter, null)
                .create();
        alertDialog.getListView().setOnItemClickListener((p, v, donateOption, id) -> viewModel.launchPurchaseFlow(requireActivity(), donateOption));

        return alertDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.eventBus.observeForever(eventBusObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.eventBus.removeObserver(eventBusObserver);
    }
}
