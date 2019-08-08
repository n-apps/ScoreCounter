package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
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

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.android.billingclient.api.BillingClient.SkuType;
import static com.android.billingclient.api.BillingClient.newBuilder;


class DonateViewModel extends AndroidViewModel implements PurchasesUpdatedListener {

    MutableLiveData<List<SkuDetails>> skuDetailsList = new MutableLiveData<>();
    private BillingClient billingClient;
    private PurchaseCompletedCallback callback;

    public DonateViewModel(@NonNull Application application) {
        super(application);
        Timber.d("Setup billing client");

        billingClient = newBuilder(application).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Timber.d("// The BillingClient is ready. You can query purchases here.");
                    List<String> skuList = new ArrayList<>();
                    if (BuildConfig.DEBUG) {
                        skuList.add("android.test.purchased");
                        skuList.add("android.test.canceled");
                        skuList.add("android.test.refunded");
                        skuList.add("android.test.item_unavailable");
                    }
                    skuList.add("buy_me_a_coffee");
                    skuList.add("buy_me_a_pizza");

                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(SkuType.INAPP);
                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                    billingClient.querySkuDetailsAsync(params.build(), (billingResult1, skuDetailsListResponse) -> {
                        // Process the result.
                        Timber.d("// Process the result");
                        if (billingResult1.getResponseCode() == BillingResponseCode.OK) {
                            if (skuDetailsListResponse != null) {
                                skuDetailsList.setValue(skuDetailsListResponse);
                            }
                        }
                    });
                } else {
                    Timber.d("Problem setting up in-app billing: %s", billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                billingClient.startConnection(this);
                Timber.d("onBillingServiceDisconnected");
            }
        });


    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        Timber.d("onPurchasesUpdated");
        if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
    }

    void purchase(Activity activity, SkuDetails sku) {
        if (sku == null) {
            Timber.e("sku is null :(");
            return;
        }
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(sku)
                .build();
        billingClient.launchBillingFlow(activity, flowParams);
    }

    private void handlePurchase(Purchase purchase) {
        Timber.d("Got a verified purchase: %s", purchase.toString());

        acknowledgePurchase(purchase);
        // allow multiple purchases
        billingClient.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseToken != null) {
                // TODO: do we need to move it on view side? like PurchaseCompletedCallback
                //  see https://github.com/ianhanniballake/LocalStorage/blob/master/mobile/src/main/java/com/ianhanniballake/localstorage/DonateDialogFragment.kt
                Toast.makeText(getApplication(), "Thank you!", Toast.LENGTH_SHORT).show();
                Timber.d("AllowMultiplePurchases success, responseCode: %s", billingResult.getResponseCode());
            } else {
                Timber.d("Can't allowMultiplePurchases, responseCode: %s", billingResult.getResponseCode());
            }
        });
    }

    private void acknowledgePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams params =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(params, billingResult -> Timber.d("purchase acknowledged. Code: %s", billingResult.getResponseCode()));
            }

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that
            // are given to them. You can also choose to remind the user in the
            // future to complete the purchase if you detect that it is still
            // pending.

            Timber.d("pending");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        billingClient.endConnection();
    }

    interface PurchaseCompletedCallback {
        void onSuccess(int responseCode);
    }
}

public class DonateDialog extends DialogFragment {

    private DonateViewModel viewModel;
    private DonateAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(requireActivity()).get(DonateViewModel.class);
        adapter = new DonateAdapter();
        // replace LiveData with plain ArrayList, if fetching products is not required by Google
        viewModel.skuDetailsList.observe(this, skuDetails -> {
            if (skuDetails != null) {
                for (SkuDetails skuDetail : skuDetails) {
                    adapter.addDonateInfo(null, skuDetail.getTitle());
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setAdapter(adapter, (dialog, which) -> {
                    SkuDetails sku = viewModel.skuDetailsList.getValue().get(which);
                    viewModel.purchase(requireActivity(), sku);
                })
                .create();
    }

}
