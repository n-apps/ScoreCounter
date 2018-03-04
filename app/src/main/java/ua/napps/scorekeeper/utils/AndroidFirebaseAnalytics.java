package ua.napps.scorekeeper.utils;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.app.App;

public class AndroidFirebaseAnalytics {

    private static FirebaseAnalytics firebaseAnalytics;

    private static FirebaseAnalytics getInstance() {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(App.getInstance());
            firebaseAnalytics.setMinimumSessionDuration(3000); //minimum session time is 1 minute
            firebaseAnalytics.setUserProperty("app_version", "beta");
            firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        }
        return firebaseAnalytics;
    }

    public static void logEvent(@NonNull String event, @NonNull Bundle params) {
        getInstance().logEvent(event, params);
    }

    public static void logEvent(@NonNull String event) {
        getInstance().logEvent(event, null);
    }

}
