package ua.napps.scorekeeper.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
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

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.android.billingclient.api.BillingClient.SkuType;
import static com.android.billingclient.api.BillingClient.newBuilder;

public class DonateDialog extends DialogFragment implements PurchasesUpdatedListener {

    private BillingClient billingClient;
    private List<SkuDetails> skuDetailsList;
    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBillingClient();
    }

    private void setupBillingClient() {
        billingClient = newBuilder(requireActivity()).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Timber.d("// The BillingClient is ready. You can query purchases here.");
                    List<String> skuList = new ArrayList<>();
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
                                        skuDetailsList = skuDetailsListResponse;
                                    }
                                }
                            });


                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                billingClient.startConnection(this);
            }
        });
        acknowledgePurchaseResponseListener = billingResult -> {
        };
    }


    private void purchase(int which) {
        if (skuDetailsList == null) {
            Toast.makeText(requireContext(), "skuDetailsList is null :(", Toast.LENGTH_SHORT).show();
            return;
        }
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetailsList.get(which))
                .build();
        billingClient.launchBillingFlow(requireActivity(), flowParams);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DonateAdapter adapter = new DonateAdapter(new CharSequence[]{"Title 1", "Title 2"}, new CharSequence[]{"☕", "\uD83C\uDF55"});

        return new AlertDialog.Builder(requireContext())
                .setAdapter(adapter, (dialog, which) -> purchase(which))
                .setTitle("Donate").create();
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        Timber.d("Got a verified purchase: %s", purchase.toString());

        acknowledgePurchase(purchase);

        // FOR DEV PURPOSE ONLY
        billingClient.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(),(billingResult, purchaseToken) -> {
            Timber.d("purchase consumed");
        } );
    }

    private void acknowledgePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
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
    public void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
    }
}
