package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.google.firebase.analytics.FirebaseAnalytics;

import timber.log.Timber;
import ua.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.app.App;

public class AndroidFirebaseAnalytics {

    private static FirebaseAnalytics firebaseAnalytics;

    private static FirebaseAnalytics getInstance() {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(App.getInstance());
            firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        }
        return firebaseAnalytics;
    }

    public static void logEvent(@NonNull @Size(min = 1L, max = 40L) String event) {
        getInstance().logEvent(event, null);
        Timber.d("%s, %s", event, null);
    }

    public static void logEvent(@NonNull @Size(min = 1L, max = 40L) String event, @NonNull Bundle params) {
        getInstance().logEvent(event, params);
        Timber.d("%s, %s", event, params);
    }

    public static void setUserProperty(@NonNull @Size(min = 1L, max = 24L) String property, @Nullable @Size(max = 36L) String value) {
        getInstance().setUserProperty(property, value);
        Timber.d("%s, %s", property, value);
    }

    public static void trackScreen(@NonNull Activity activity, @NonNull String screenName, String classOverride) {
        getInstance().setCurrentScreen(activity, screenName, classOverride);
        Timber.d("trackScreen, %s", screenName);
    }
}
