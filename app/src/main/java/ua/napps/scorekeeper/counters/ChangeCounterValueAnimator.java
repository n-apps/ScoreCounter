package ua.napps.scorekeeper.counters;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

class ChangeCounterValueAnimator extends DefaultItemAnimator {

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
        return true;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(@NonNull RecyclerView.State state,
                                                     @NonNull RecyclerView.ViewHolder viewHolder,
                                                     int changeFlags,
                                                     @NonNull List<Object> payloads) {
        if (changeFlags == FLAG_CHANGED) {
            for (Object payload : payloads) {
                if (payload instanceof String) {
                    return new CountryItemHolderInfo((String) payload);
                }
            }
        }
        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
                                 @NonNull RecyclerView.ViewHolder newHolder,
                                 @NonNull ItemHolderInfo preInfo,
                                 @NonNull ItemHolderInfo postInfo) {

        if (preInfo instanceof CountryItemHolderInfo) {

            CountryItemHolderInfo itemHolderInfo = (CountryItemHolderInfo) preInfo;
            CountersAdapter.CountersViewHolder holder = (CountersAdapter.CountersViewHolder) newHolder;

            if (itemHolderInfo.clickAction.equals(CountersAdapter.INCREASE_VALUE_CLICK)) {
                animateHolder(holder, true);
            } else if (itemHolderInfo.clickAction.equals(CountersAdapter.DECREASE_VALUE_CLICK)) {
                animateHolder(holder, false);
            }
        }
        return false;
    }

    private void animateHolder(CountersAdapter.CountersViewHolder holder, boolean isIncrease) {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> items = new ArrayList<>();

        ObjectAnimator scaleAnimator =
                ObjectAnimator.ofPropertyValuesHolder(holder.counterValue,
                        PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.87f, 1.0f),
                        PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.87f, 1.0f));
        scaleAnimator.setInterpolator(DECELERATE_INTERPOLATOR);
        scaleAnimator.setDuration(500);
        items.add(scaleAnimator);
        if (isIncrease) {
            ObjectAnimator scaleArrowAnimator =
                    ObjectAnimator.ofPropertyValuesHolder(holder.increaseImageView,
                            PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.54f, 1.0f),
                            PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.54f, 1.0f));
            items.add(scaleArrowAnimator);
        } else {
            ObjectAnimator scaleArrowAnimator =
                    ObjectAnimator.ofPropertyValuesHolder(holder.decreaseImageView,
                            PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.54f, 1.0f),
                            PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.54f, 1.0f));
            items.add(scaleArrowAnimator);
        }
        animatorSet.playTogether(items);
        animatorSet.start();
    }

    private static class CountryItemHolderInfo extends ItemHolderInfo {
        final String clickAction;

        CountryItemHolderInfo(String clickAction) {
            this.clickAction = clickAction;
        }
    }
}
