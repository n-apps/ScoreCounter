package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.app.App;

public class AndroidFirebaseAnalytics {

    private static com.google.firebase.analytics.FirebaseAnalytics firebaseAnalytics;

    private static com.google.firebase.analytics.FirebaseAnalytics getInstance() {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(App.getInstance());
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
