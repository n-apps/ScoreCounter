package ua.napps.scorekeeper.utils;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

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
        final View titleView = getLayoutInflater().inflate(R.layout.item_donation_title, null);

        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.action_donate)
                .setCustomTitle(titleView)
                .setAdapter(adapter, null);

        // Set a custom ShapeAppearanceModel
        MaterialShapeDrawable alertBackground = (MaterialShapeDrawable) materialAlertDialogBuilder.getBackground();
        if (alertBackground != null) {
            alertBackground.setShapeAppearanceModel(
                    alertBackground.getShapeAppearanceModel()
                            .toBuilder()
                            .setAllCorners(CornerFamily.ROUNDED, 32.0f)
                            .build());
        }

        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//            window.setGravity(Gravity.BOTTOM);
        }
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
