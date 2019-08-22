package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

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

import static com.android.billingclient.api.BillingClient.newBuilder;

public class DonateViewModel extends AndroidViewModel implements PurchasesUpdatedListener {

    private BillingClient billingClient;
    private List<SkuDetails> skuDetailsList = new ArrayList<>();

    public DonateViewModel(@NonNull Application application) {
        super(application);
        billingClient = newBuilder(application).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    List<String> skuList = new ArrayList<>(2);
                    skuList.add("buy_me_a_coffee");
                    skuList.add("buy_me_a_pizza");

                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(), (result, response) -> {
                        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (response != null) {
                                skuDetailsList.addAll(response);
                            }
                        } else {
                            Timber.e("Problem retrieve a value sku details list : %s", billingResult.getResponseCode());
                        }
                    });
                } else {
                    Timber.e("Problem setting up in-app billing: %s", billingResult.getResponseCode());
                    Toast.makeText(getApplication(), R.string.error_message, Toast.LENGTH_SHORT).show();
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
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else {
            Timber.e("Purchase error, responseCode: %s", billingResult.getResponseCode());
            Toast.makeText(getApplication(), R.string.error_message, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplication(), R.string.donation_thank_you, Toast.LENGTH_SHORT).show();
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
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
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
}
