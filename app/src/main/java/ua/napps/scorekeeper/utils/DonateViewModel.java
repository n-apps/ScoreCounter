package ua.napps.scorekeeper.utils;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.android.billingclient.api.BillingClient.SkuType;
import static com.android.billingclient.api.BillingClient.newBuilder;

import android.app.Activity;
import android.app.Application;

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
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;
import ua.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.livedata.CloseScreenIntent;
import ua.napps.scorekeeper.utils.livedata.MessageIntent;
import ua.napps.scorekeeper.utils.livedata.SingleShotEvent;

public class DonateViewModel extends AndroidViewModel implements PurchasesUpdatedListener {

    public final MutableLiveData<SingleShotEvent> eventBus = new MutableLiveData<>();

    private final BillingClient billingClient;

    private final List<SkuDetails> skuDetailsList = new ArrayList<>();
    private final List<String> skuList = BuildConfig.DEBUG
            ? Arrays.asList("android.test.purchased", "android.test.purchased", "android.test.canceled")
            : Arrays.asList("buy_me_a_coffee", "buy_me_a_pizza", "buy_me_a_xwing");

    public DonateViewModel(@NonNull Application application) {
        super(application);
        billingClient = newBuilder(application).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
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
                    eventBus.postValue(new SingleShotEvent<>(new MessageIntent(R.string.message_error_generic)));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingClient.startConnection(this);
            }
        });
    }

    private void handleUnconsumedPurchases() {
        billingClient.queryPurchasesAsync(SkuType.INAPP, (billingResult, list) -> consumePurchases(list, null));
    }

    void purchase(Activity activity, int donateOption) {
        String sku = skuList.get(donateOption);
        SkuDetails skuDetails = findSkuDetails(sku);
        if (skuDetails == null) {
            Timber.e(new BillingStateException("skuDetails not found :("));
            eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_error_generic)));
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
                    () -> eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_thank_you)))
            );
        } else {
            if (billingResult.getResponseCode() != BillingResponseCode.USER_CANCELED) {
                eventBus.postValue(new SingleShotEvent<>(new MessageIntent(R.string.message_error_generic)));
            }
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
