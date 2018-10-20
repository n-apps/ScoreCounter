package ua.napps.scorekeeper.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import ua.com.napps.scorekeeper.R;

public class RoundedColorView extends View {
    private final int DEFAULT_BACKGROUND_COLOR = Color.CYAN;
    private final int DEFAULT_VIEW_SIZE = 48;

    private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;

    private Paint mBackgroundPaint;
    private RectF mInnerRectF;
    private float mViewSize;

    @Override
    public void setBackgroundColor(final int backgroundColor) {
        mBackgroundColor = backgroundColor;
        invalidatePaints();
    }

    public RoundedColorView(final Context context) {
        this(context, null);
    }

    public RoundedColorView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedColorView(final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(final AttributeSet attrs, final int defStyle) {
        final TypedArray array = getContext()
                .obtainStyledAttributes(attrs, R.styleable.RoundedColorView, defStyle, 0);

        mBackgroundColor = array.getColor(R.styleable.RoundedColorView_backgroundColor, DEFAULT_BACKGROUND_COLOR);

        array.recycle();

        //Background Paint
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mBackgroundColor);

        mInnerRectF = new RectF();
    }

    private void invalidatePaints() {
        mBackgroundPaint.setColor(mBackgroundColor);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = resolveSize(DEFAULT_VIEW_SIZE, widthMeasureSpec);
        final int height = resolveSize(DEFAULT_VIEW_SIZE, heightMeasureSpec);
        mViewSize = Math.min(width, height);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        mInnerRectF.set(0,0,mViewSize, mViewSize);
        mInnerRectF.offset((getWidth() - mViewSize) / 2, (getHeight() - mViewSize) / 2);
        canvas.drawOval(mInnerRectF, mBackgroundPaint);
    }
}