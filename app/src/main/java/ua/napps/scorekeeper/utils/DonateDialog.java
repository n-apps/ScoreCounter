package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import ua.com.napps.scorekeeper.R;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.android.billingclient.api.BillingClient.SkuType;
import static com.android.billingclient.api.BillingClient.newBuilder;

class DonateDialogViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final DonateViewModel.BillingCallback completedCallback;
    private final Application app;

    DonateDialogViewModelFactory(Application application, DonateViewModel.BillingCallback callback) {
        completedCallback = callback;
        app = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DonateViewModel(app, completedCallback);
    }
}

class DonateViewModel extends AndroidViewModel implements PurchasesUpdatedListener {

    private final BillingCallback callback;
    private BillingClient billingClient;
    private List<SkuDetails> skuDetailsList = new ArrayList<>();

    DonateViewModel(@NonNull Application application, BillingCallback completedCallback) {
        super(application);
        callback = completedCallback;
        billingClient = newBuilder(application).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    List<String> skuList = new ArrayList<>(2);
                    skuList.add("buy_me_a_coffee");
                    skuList.add("buy_me_a_pizza");

                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(), (result, response) -> {
                        if (result.getResponseCode() == BillingResponseCode.OK) {
                            if (response != null) {
                                skuDetailsList.addAll(response);
                            }
                        } else {
                            Timber.e("Problem retrieve a value sku details list : %s", billingResult.getResponseCode());
                        }
                    });
                } else {
                    callback.onResult(billingResult.getResponseCode());
                    Timber.e("Problem setting up in-app billing: %s", billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingClient.startConnection(this);
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else {
            callback.onResult(billingResult.getResponseCode());
        }
    }

    void purchase(Activity activity, @IntRange(from = 0, to = 1) int donateOption) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, "" + donateOption);
        AndroidFirebaseAnalytics.logEvent("DonationScreenDonateOptionSubmit", params);

        if (skuDetailsList.isEmpty() || donateOption > 1) {
            Timber.e("skuDetailsList is empty :(");
            return;
        }
        billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetailsList.get(donateOption))
                .build());
    }

    private void handlePurchase(Purchase purchase) {
        acknowledgePurchase(purchase);

        billingClient.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), (billingResult, purchaseToken) -> {
            callback.onResult(billingResult.getResponseCode());
            AndroidFirebaseAnalytics.logEvent("DonationScreenDonateOptionPurchased");
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseToken != null) {
                Timber.d("AllowMultiplePurchases success, responseCode: %s", billingResult.getResponseCode());
            } else {
                Timber.e("Can't allow multiple purchases, responseCode: %s", billingResult.getResponseCode());
            }
        });
    }

    private void acknowledgePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                billingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build(), billingResult -> {
                    if (billingResult.getResponseCode() != BillingResponseCode.OK) {
                        Timber.e("Can't acknowledge purchase, responseCode: %s", billingResult.getResponseCode());
                    }
                });
            }
        }
    }

    @Override
    protected void onCleared() {
        billingClient.endConnection();
    }

    interface BillingCallback {
        void onResult(int responseCode);
    }
}

public class DonateDialog extends DialogFragment implements DonateViewModel.BillingCallback {

    private DonateViewModel viewModel;
    private DonateAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DonateDialogViewModelFactory factory = new DonateDialogViewModelFactory(requireActivity().getApplication(), this);
        viewModel = ViewModelProviders.of(requireActivity(), factory).get(DonateViewModel.class);
        adapter = new DonateAdapter(requireContext());
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

    @Override
    public void onResult(int responseCode) {
        if (responseCode == BillingResponseCode.OK) {
            if (isAdded() && getActivity() != null) {
                Toast.makeText(requireContext(), R.string.donation_thank_you, Toast.LENGTH_SHORT).show();
            }
            AndroidFirebaseAnalytics.setUserProperty("donated", "true");
        } else {
            if (isAdded() && getActivity() != null) {
                Toast.makeText(requireContext(), R.string.error_message, Toast.LENGTH_SHORT).show();
            }
        }
        dismiss();
    }
}
