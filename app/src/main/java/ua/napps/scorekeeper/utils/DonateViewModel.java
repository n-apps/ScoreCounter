package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.livedata.MessageIntent;
import ua.napps.scorekeeper.utils.livedata.CloseScreenIntent;
import ua.napps.scorekeeper.utils.livedata.SingleShotEvent;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.android.billingclient.api.BillingClient.SkuType;
import static com.android.billingclient.api.BillingClient.newBuilder;

public class DonateViewModel extends AndroidViewModel implements PurchasesUpdatedListener {

    public final MutableLiveData<SingleShotEvent> eventBus = new MutableLiveData<>();

    private BillingClient billingClient;

    private final List<SkuDetails> skuDetailsList = new ArrayList<>();
    private final List<String> skuList = BuildConfig.DEBUG
            ? Arrays.asList("android.test.purchased", "android.test.canceled")
            : Arrays.asList("buy_me_a_coffee", "buy_me_a_pizza");

    public DonateViewModel(@NonNull Application application) {
        super(application);
        billingClient = newBuilder(application).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(), (result, response) -> {
                        if (result.getResponseCode() == BillingResponseCode.OK) {
                            if (response != null) {
                                skuDetailsList.addAll(response);
                            }
                        } else {
                            Timber.e(
                                    new BillingStateException(result.getDebugMessage()),
                                    "Problem retrieve a value sku details list : %s", billingResult.getResponseCode()
                            );
                        }
                    });
                    handleUnconsumedPurchases();
                } else {
                    Timber.e(
                            new BillingStateException(billingResult.getDebugMessage()),
                            "Problem setting up in-app billing: %s", billingResult.getResponseCode()
                    );
                    eventBus.postValue(new SingleShotEvent<>(new MessageIntent(R.string.error_message)));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingClient.startConnection(this);
            }
        });
    }

    private void handleUnconsumedPurchases() {
        PurchasesResult purchasesResult = billingClient.queryPurchases(SkuType.INAPP);
        List<Purchase> purchases = purchasesResult.getPurchasesList();
        if (purchases != null && !purchases.isEmpty()) {
            Timber.d("Found [%s] unconsumed purchases", purchases.size());
            consumePurchases(purchases, null);
        }
    }

    void purchase(Activity activity, @IntRange(from = 0, to = 1) int donateOption) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, "" + donateOption);
        AndroidFirebaseAnalytics.logEvent("DonationScreenDonateOptionSubmit", params);

        String sku = skuList.get(donateOption);
        SkuDetails skuDetails = findSkuDetails(sku);
        if (skuDetails == null) {
            Timber.e(new BillingStateException("skuDetails not found :("));
            return;
        }
        billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build());
    }

    private SkuDetails findSkuDetails(@NonNull String sku) {
        for (SkuDetails skuDetails : skuDetailsList) {
            if (sku.equals(skuDetails.getSku())) {
                return skuDetails;
            }
        }
        return null;
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null && !purchases.isEmpty()) {
            consumePurchases(
                    purchases,
                    () -> {
                        eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.donation_thank_you)));
                        AndroidFirebaseAnalytics.logEvent("DonationScreenDonateOptionPurchased");
                    }
            );
        } else {
            eventBus.postValue(new SingleShotEvent<>(new MessageIntent(R.string.error_message)));
        }
    }

    private void consumePurchases(@NonNull List<Purchase> purchases, @Nullable Runnable onCompleted) {
        if (purchases.isEmpty()) {
            if (onCompleted != null) onCompleted.run();
            return;
        }

        final AtomicInteger countDown = onCompleted == null ? null : new AtomicInteger(purchases.size());

        for (Purchase purchase : purchases) {
            Timber.d("Start consuming purchase [%s]", purchase.getPurchaseToken());
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.consumeAsync(
                    consumeParams,
                    (result, token) -> {
                        if (result.getResponseCode() == BillingResponseCode.OK) {
                            Timber.d("Purchase [%s] consumed successfully!", purchase.getPurchaseToken());
                        } else {
                            Timber.e(
                                    new BillingStateException(result.getDebugMessage()),
                                    "Fail to consume purchase [%s]",
                                    purchase.getPurchaseToken()
                            );
                        }
                        if (onCompleted != null && countDown.decrementAndGet() <= 0) {
                            onCompleted.run();
                        }
                    }
            );
        }
    }

    @Override
    protected void onCleared() {
        billingClient.endConnection();
    }

    private static class BillingStateException extends RuntimeException {
        BillingStateException(String message) {
            super(message);
        }
    }
}
