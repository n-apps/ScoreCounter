package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.google.firebase.analytics.FirebaseAnalytics;

import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.app.App;

public class AndroidFirebaseAnalytics {

    private static FirebaseAnalytics firebaseAnalytics;

    private static FirebaseAnalytics getInstance() {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(App.getInstance());
            firebaseAnalytics.setMinimumSessionDuration(3000); //minimum session time is 1 minute
            firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        }
        return firebaseAnalytics;
    }

    public static void logEvent(@NonNull @Size(min = 1L, max = 40L) String event, @NonNull Bundle params) {
        getInstance().logEvent(event, params);
        Timber.d("%s | %s", event, params);
    }

    public static void logEvent(@NonNull @Size(min = 1L, max = 40L) String event) {
        getInstance().logEvent(event, null);
        Timber.d("%s", event);
    }

    public static void trackScreen(@Nullable Activity activity, @NonNull String screenName) {
        if (activity == null) return;
        Timber.d("trackScreen: %s", screenName);
        getInstance().setCurrentScreen(activity, screenName, null);
    }
}
