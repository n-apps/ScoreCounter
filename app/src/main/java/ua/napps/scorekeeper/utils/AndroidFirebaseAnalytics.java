package ua.napps.scorekeeper.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.BuildConfig;

public class AndroidFirebaseAnalytics {

    private static FirebaseAnalytics firebaseAnalytics;

    private static FirebaseAnalytics getInstance(Context context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            firebaseAnalytics.setMinimumSessionDuration(3000); //minimum session time is 1 minute
            firebaseAnalytics.setUserProperty("app_version", "beta");
            firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        }
        return firebaseAnalytics;
    }

    public static void logEvent(Context context, @NonNull String event, @NonNull Bundle params) {
        getInstance(context).logEvent(event, params);
    }

    public static void logEvent(Context context, @NonNull String event) {
        getInstance(context).logEvent(event, null);
    }

}
