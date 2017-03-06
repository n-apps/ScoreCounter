package ua.napps.scorekeeper.utils;

import android.support.v7.widget.DefaultItemAnimator;

/**
 * Created by novo on 23-Dec-15.
 */
public class NoChangeAnimator extends DefaultItemAnimator {
    public NoChangeAnimator() {
        setSupportsChangeAnimations(false);
    }
}