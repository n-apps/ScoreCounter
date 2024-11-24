package ua.napps.scorekeeper.utils;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import ua.napps.scorekeeper.counters.CountersAdapter;

public class BounceItemAnimator extends DefaultItemAnimator {

public BounceItemAnimator() {
    setSupportsChangeAnimations(false);
    setChangeDuration(0);
}

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
                                 @NonNull RecyclerView.ViewHolder newHolder,
                                 int fromLeft, int fromTop, int toLeft, int toTop) {
        if (oldHolder instanceof CountersAdapter.CounterCompactViewHolder
                || oldHolder instanceof CountersAdapter.CounterFullViewHolder) {

            View counterValueView = null;

            if (oldHolder instanceof CountersAdapter.CounterCompactViewHolder) {
                counterValueView = ((CountersAdapter.CounterCompactViewHolder) oldHolder).counterValue;
            } else {
                counterValueView = ((CountersAdapter.CounterFullViewHolder) oldHolder).counterValue;
            }

            if (counterValueView != null) {
                applyBounceAnimation(counterValueView);
            }
        }
        return super.animateChange(oldHolder, newHolder, fromLeft, fromTop, toLeft, toTop);
    }

    private void applyBounceAnimation(View view) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY);
        animator.setDuration(300);
        animator.setInterpolator(new OvershootInterpolator(1.02f));
        animator.start();
    }
}