package ua.napps.scorekeeper.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SpanningLinearLayoutManager extends LinearLayoutManager {

    public SpanningLinearLayoutManager(Context context) {
        super(context);
    }

    public SpanningLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SpanningLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private final Rect insets = new Rect();

    @Override
    public void measureChild(View child, int widthUsed, int heightUsed) {
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();

        calculateItemDecorationsForChild(child, insets);
        widthUsed += insets.left + insets.right;
        heightUsed += insets.top + insets.bottom;

//        if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
        child.measure(
                createWidthSpec(widthUsed, lp, false),
                createHeightSpec(heightUsed, lp, false)
        );
//        }
    }

    @Override
    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();

        calculateItemDecorationsForChild(child, insets);
        widthUsed += insets.left + insets.right;
        heightUsed += insets.top + insets.bottom;

//        if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
        child.measure(
                createWidthSpec(widthUsed, lp, true),
                createHeightSpec(heightUsed, lp, true)
        );
//        }
    }

    private int createWidthSpec(int widthUsed, RecyclerView.LayoutParams lp, boolean withMargins) {
        int padding = getPaddingLeft() + getPaddingRight() + widthUsed;
        if (withMargins) padding += lp.leftMargin + lp.rightMargin;
        return getOrientation() == HORIZONTAL
                ? View.MeasureSpec.makeMeasureSpec((int) Math.round(getHorizontalSpace() / (double) getItemCount()), View.MeasureSpec.EXACTLY)
                : getChildMeasureSpec(getWidth(), getWidthMode(), padding, lp.width, /*canScrollHorizontally*/ false);
    }

    private int createHeightSpec(int heightUsed, RecyclerView.LayoutParams lp, boolean withMargins) {
        int padding = getPaddingTop() + getPaddingBottom() + heightUsed;
        if (withMargins) padding += lp.topMargin + lp.bottomMargin;
        return getOrientation() == VERTICAL
                ? View.MeasureSpec.makeMeasureSpec((int) Math.round(getVerticalSpace() / (double) getItemCount()), View.MeasureSpec.EXACTLY)
                : getChildMeasureSpec(getHeight(), getHeightMode(), padding, lp.height, /*canScrollVertically*/ false);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }
}
