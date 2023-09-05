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
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.livedata.CloseScreenIntent;
import ua.napps.scorekeeper.utils.livedata.MessageIntent;
import ua.napps.scorekeeper.utils.livedata.SingleShotEvent;

public class DonateViewModel extends AndroidViewModel {

    final MutableLiveData<SingleShotEvent> eventBus = new MutableLiveData<>();
    final MutableLiveData<List<ProductDetails>> productDetailsList = new MutableLiveData<>();
    private final BillingClient billingClient;

    private final ArrayList<String> productIDs = new ArrayList<String>() {{
        add("buy_me_a_coffee");
        add("buy_me_a_pizza");
        add("buy_me_a_xwing");
    }};

    public DonateViewModel(@NonNull Application application) {
        super(application);
        billingClient = newBuilder(application)
                .enablePendingPurchases()
                .setListener((billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingResponseCode.OK && list != null) {
                        for (Purchase purchase : list) {
                            handlePurchase(purchase, () -> eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_thank_you)))
                            );
                        }
                    } else {
                        if (billingResult.getResponseCode() != BillingResponseCode.USER_CANCELED) {
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
                    getListsInAppDetail();
                    refreshPurchasesAsync();
                    Timber.d("Connection Established");
                } else if (billingResult.getResponseCode() == BillingResponseCode.BILLING_UNAVAILABLE) {
                    Timber.e(new BillingStateException("BILLING_UNAVAILABLE :("));
                    eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_error_generic)));
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Timber.d("Connection NOT Established");
                establishConnection();
            }
        });
    }

    private void getListsInAppDetail() {
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        for (String ids : productIDs) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(ids)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> productDetailsList.postValue(list));
    }

    void launchPurchaseFlow(Activity activity, int donateOption) {
        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();

        ProductDetails productDetails = findProductDetails(productIDs.get(donateOption));
        if (productDetails == null) {
            Timber.e(new BillingStateException("productDetails not found :("));
            eventBus.postValue(new SingleShotEvent<>(new CloseScreenIntent(R.string.message_error_generic)));
            return;
        }
        productList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build());

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    @Nullable
    private ProductDetails findProductDetails(String productDetailsString) {
        List<ProductDetails> list = productDetailsList.getValue();
        if (list != null) {
            for (ProductDetails productDetails : list) {
                if (productDetailsString.equals(productDetails.getProductId())) {
                    return productDetails;
                }
            }
        }
        return null;
    }


    private void handlePurchase(Purchase purchase, @Nullable Runnable onCompleted) {
        if (!purchase.isAcknowledged()) {
            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build(), billingResult -> {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    consumePurchase(purchase);
                } else {
                    Timber.e(
                            new BillingStateException("Couldn't handle purchase: " + purchase.getPurchaseToken()));
                }
                if (onCompleted != null) {
                    onCompleted.run();
                }
            });
        }
    }

    private void consumePurchase(Purchase purchase) {
        ConsumeParams params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.consumeAsync(params, (billingResult, s) -> Timber.d("Consuming Successful: %S", s));
    }

    public void refreshPurchasesAsync() {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, list) -> {
                    if (list.isEmpty()) {
                        return;
                    }
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            handlePurchase(purchase, null);
                        }
                    }
                });
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
