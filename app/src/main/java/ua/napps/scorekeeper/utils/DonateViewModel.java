package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

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
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.com.napps.scorekeeper.R;

import static com.android.billingclient.api.BillingClient.newBuilder;

public class DonateViewModel extends AndroidViewModel implements PurchasesUpdatedListener {

    public static final Integer EVENT_DISMISS = 101;

    private BillingClient billingClient;
    private List<SkuDetails> skuDetailsList = new ArrayList<>();
    public final MutableLiveData<ConsumableEvent> events = new MutableLiveData<>();

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
                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                    List<Purchase> purchasesList = purchasesResult.getPurchasesList();
                    if (purchasesList != null) {
                        for (Purchase purchase : purchasesList) {
                            consumePurchase(purchase);
                        }
                    }
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
                consumePurchase(purchase);
            }
            Toast.makeText(getApplication(), R.string.donation_thank_you, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplication(), R.string.error_message, Toast.LENGTH_SHORT).show();
        }
        postDismissEvent();
    }

    private void postDismissEvent() {
        events.postValue(new ConsumableEvent<>(EVENT_DISMISS));
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

    private void consumePurchase(Purchase purchase) {
        billingClient.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), (billingResult, purchaseToken) -> {
            AndroidFirebaseAnalytics.logEvent("DonationScreenDonateOptionPurchased");
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseToken != null) {
                Timber.d("AllowMultiplePurchases success, responseCode: %s", billingResult.getResponseCode());
            } else {
                Timber.e("Can't allow multiple purchases, responseCode: %s", billingResult.getResponseCode());
            }
        });
    }

    @Override
    protected void onCleared() {
        billingClient.endConnection();
    }
}
