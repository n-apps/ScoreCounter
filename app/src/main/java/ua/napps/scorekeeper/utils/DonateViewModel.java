package ua.napps.scorekeeper.utils;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.android.billingclient.api.BillingClient.newBuilder;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.livedata.CloseScreenIntent;
import ua.napps.scorekeeper.utils.livedata.MessageIntent;
import ua.napps.scorekeeper.utils.livedata.SingleShotEvent;

public class DonateViewModel extends AndroidViewModel {

    private static final int MAX_CONNECTION_RETRY = 3;

    public final MutableLiveData<SingleShotEvent> eventBus = new MutableLiveData<>();
    public final MutableLiveData<List<ProductDetails>> oneTimeDetailsList = new MutableLiveData<>();
    public final MutableLiveData<List<ProductDetails>> subsDetailsList = new MutableLiveData<>();
    private final AtomicInteger connectionRetryCount = new AtomicInteger(0);
    private final BillingClient billingClient;

    private static final List<String> ONE_TIME_PRODUCT_IDS = List.of(
            "buy_me_a_coffee",
            "buy_me_a_pizza",
            "buy_me_a_xwing"
    );
    private static final List<String> SUBSCRIPTION_IDS = List.of(
            "buy_me_a_coffee_monthly",
            "buy_me_a_pizza_monthly",
            "buy_me_a_xwing_monthly"
    );

    public DonateViewModel(@NonNull Application application) {
        super(application);
        billingClient = newBuilder(application)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .setListener((billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingResponseCode.OK && list != null) {
                        for (Purchase purchase : list) {
                            handleRealTimePurchase(purchase); // Separate method for real-time purchases
                        }
                    } else {
                        if (billingResult.getResponseCode() != BillingResponseCode.USER_CANCELED) {
                            Timber.e(new BillingStateException("Billing initialisation error :("));
                            eventBus.postValue(new SingleShotEvent<>(new MessageIntent(R.string.message_error_generic)));
                        }
                    }
                })
                .build();

        establishConnection();
    }

    private void establishConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connectionRetryCount.set(0);
                    getListsInAppDetail();
                    getListsSubsDetail();
                    refreshPurchasesAsync();
                } else if (billingResult.getResponseCode() == BillingResponseCode.BILLING_UNAVAILABLE) {
                    eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_error_generic, true)));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                if (connectionRetryCount.incrementAndGet() <= MAX_CONNECTION_RETRY) {
                    Timber.w("Billing service disconnected. Retry attempt: %s", connectionRetryCount.get());
                    establishConnection();
                } else {
                    Timber.e("Max connection retries reached. Billing unavailable.");
                    eventBus.postValue(new SingleShotEvent<>(
                            new CloseScreenIntent(R.string.message_error_generic, true)));
                }
            }
        });
    }

    private void getListsSubsDetail() {
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        for (String id : SUBSCRIPTION_IDS) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(id)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> subsDetailsList.postValue(list));
    }

    private void getListsInAppDetail() {
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        for (String id : ONE_TIME_PRODUCT_IDS) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(id)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> oneTimeDetailsList.postValue(list));
    }

    public void launchPurchaseFlow(Activity activity, int item, boolean isOneTime) {

        ProductDetails productDetails = findOneTimeProductsDetails(isOneTime, item);

        if (productDetails == null) {
            eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_error_generic, true)));
            return;
        }

        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();

        if (isOneTime) {
            productList.add(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build());
        } else {
            String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();

            productList.add(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build());
        }

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    @Nullable
    private ProductDetails findOneTimeProductsDetails(boolean isOneTime, int item) {
        if (isOneTime) {
            List<ProductDetails> list = oneTimeDetailsList.getValue();
            if (list != null) {
                for (ProductDetails product : list) {
                    if (ONE_TIME_PRODUCT_IDS.get(item).equals(product.getProductId())) {
                        return product;
                    }
                }
            }
        } else {
            List<ProductDetails> list = subsDetailsList.getValue();
            if (list != null) {
                for (ProductDetails product : list) {
                    if (SUBSCRIPTION_IDS.get(item).equals(product.getProductId())) {
                        return product;
                    }
                }
            }
        }

        return null;
    }

    private void acknowledgePurchase(Purchase purchase) {
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

            billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Purchase acknowledged successfully.");
                } else {
                    Timber.e("Failed to acknowledge purchase: %s", billingResult.getDebugMessage());
                }
            });
        }
    }

    private void consumePurchase(Purchase purchase) {
        ConsumeParams params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.consumeAsync(params, (billingResult, listener) -> {
            if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_thank_you, false)));
            } else {
                eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_error_generic, true)));
            }
        });
    }

    private void handleRealTimePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            List<String> purchasedProducts = purchase.getProducts();

            // Check for subscriptions
            if (purchasedProducts.stream().anyMatch(SUBSCRIPTION_IDS::contains)) {
                // Acknowledge the subscription
                acknowledgePurchase(purchase);

                // Notify the UI with CloseScreenIntent for real-time purchase
                eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_thank_you, false)));
            }

            // Handle one-time purchases if needed
            if (purchasedProducts.stream().anyMatch(ONE_TIME_PRODUCT_IDS::contains)) {
                consumePurchase(purchase);
            }
        }
    }

    public void refreshPurchasesAsync() {
        List<String> productTypes = Arrays.asList(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS);

        for (String productType : productTypes) {
            billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                    .setProductType(productType)
                    .build(), (billingResult, purchases) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (Purchase purchase : purchases) {
                        handleRestoredPurchase(purchase); // Handle restored purchases separately
                    }
                } else {
                    Timber.e("Error querying purchases: %s", billingResult.getDebugMessage());
                }
            });
        }
    }

    private void handleRestoredPurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            List<String> purchasedProducts = purchase.getProducts();

            // Acknowledge subscriptions (if not already acknowledged)
            if (purchasedProducts.stream().anyMatch(SUBSCRIPTION_IDS::contains)) {
                acknowledgePurchase(purchase);
                Timber.d("Restored subscription purchase for %s", purchasedProducts);
            }

            // Consume one-time purchases if needed
            if (purchasedProducts.stream().anyMatch(ONE_TIME_PRODUCT_IDS::contains)) {
                consumePurchase(purchase);
            }
        }
    }
    @Override
    protected void onCleared() {
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }

    private static class BillingStateException extends RuntimeException {
        BillingStateException(String message) {
            super(message);
        }
    }
}
