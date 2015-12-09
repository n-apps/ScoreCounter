package ua.napps.scorekeeper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Events.CounterCaptionClick;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.MainActivity;

import static ua.napps.scorekeeper.Helpers.Constants.CAPTION_TEXT_RATIO;
import static ua.napps.scorekeeper.Helpers.Constants.LEFT;
import static ua.napps.scorekeeper.Helpers.Constants.LONG_PRESS_TIMEOUT;
import static ua.napps.scorekeeper.Helpers.Constants.MINUS_SYMBOL_SCALE;
import static ua.napps.scorekeeper.Helpers.Constants.PLUS_MINUS_RATIO;
import static ua.napps.scorekeeper.Helpers.Constants.PREV_VALUE_SHOW_DURATION;
import static ua.napps.scorekeeper.Helpers.Constants.RIGHT;
import static ua.napps.scorekeeper.Helpers.Constants.SWIPE_THRESHOLD;
import static ua.napps.scorekeeper.Helpers.Constants.VALUE_TEXT_RATIO;
import static ua.napps.scorekeeper.Models.Counter.OnChangeListener;

public class CounterView extends FrameLayout implements View.OnClickListener, View.OnTouchListener, OnChangeListener {
    private final float density = getResources().getDisplayMetrics().density;
    private MainActivity callback;
    Counter counter;
    private long startShowingPrevValue;
    @Bind(R.id.rootCounterView) View root;
    @Bind(R.id.caption) TextView caption;
    @Bind(R.id.prevValue) TextView prevValue;
    @Bind(R.id.value) TextView tvValue;
    @Bind(R.id.plus) ImageView plus;
    @Bind(R.id.minus) ImageView minus;
    private float origX;
    private boolean moved;
    private int currW, currH, currX, currY;

    public CounterView(Context context) {
        super(context);
    }

    public CounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Counter counter, MainActivity callback, Typeface ownFontType) {
        ButterKnife.bind(this);
        this.counter = counter;
        this.callback = callback;
        root.setOnTouchListener(this);
        caption.setOnClickListener(this);
        tvValue.setTypeface(ownFontType);
        counter.setChangeListener(this);
        defineColorsByBackground();
    }

    public void update(int x, int y, int w, int h) {
        if (w != currW || h != currH || x != currX || y != currY) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
            params.width = w;
            params.height = h;
            params.setMargins(x, y, 0, 0);
            setLayoutParams(params);
            currW = w;
            currH = h;
            currX = x;
            currY = y;
            float dpHeight = (float) (Math.sqrt(w * w + h * h) / density);
            caption.setTextSize(dpHeight * CAPTION_TEXT_RATIO);
            prevValue.setTextSize(dpHeight * CAPTION_TEXT_RATIO);
            tvValue.setTextSize(dpHeight * VALUE_TEXT_RATIO);
            changePlusMinusSize(h);
        }
    }

    @Override
        public void onClick(View v) {
            EventBus.getDefault().post(new CounterCaptionClick(counter));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                origX = event.getX();
                moved = false;
                break;
            case MotionEvent.ACTION_MOVE:

                int delta = (int) (event.getX() - origX);
                if (Math.abs(delta) > SWIPE_THRESHOLD) {
                    moved = true;
                    origX = event.getX();
                    // prevent miss click when try to pull drawer
                    if (origX > 200) {
                        showPrevValue();
                        callback.onCounterSwipe(counter, delta > 0 ? RIGHT : LEFT, moved);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                boolean longClick = event.getEventTime() - event.getDownTime() > LONG_PRESS_TIMEOUT;
                if (longClick || moved) return true;
                showPrevValue();
                callback.onCounterSwipe(counter, event.getX() > v.getWidth() / 2 ? RIGHT : LEFT, moved);
                if (!moved) {
                    YoYo.with(Techniques.ZoomIn)
                            .duration(200)
                            .playOn(tvValue);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    private void showPrevValue() {
        long now = System.currentTimeMillis();
        if (now - PREV_VALUE_SHOW_DURATION > startShowingPrevValue) {

            prevValue.setText(tvValue.getText());
        }
        startShowingPrevValue = now;
    }

    private void changePlusMinusSize(float parentH) {
        ViewGroup.LayoutParams params = plus.getLayoutParams();
        int size = (int) (parentH * PLUS_MINUS_RATIO);
        params.width = size;
        params.height = size;
        plus.setLayoutParams(params);
        params = minus.getLayoutParams();
        params.width = size;
        params.height = size;
        minus.setLayoutParams(params);
    }

    @Override
    public void onChangeColor() {
        root.setBackgroundColor(counter.getColor());
        defineColorsByBackground();
    }

    private void defineColorsByBackground() {
        int tint = getTintColor(counter.getColor());
        caption.setTextColor(tint);
        tvValue.setTextColor(tint);
        prevValue.setTextColor(tint);
        plus.setColorFilter(tint);
        minus.setColorFilter(tint);
    }

    @Override
    public void onChangeValues() {
        caption.setText(counter.getCaption());
        int v = counter.getValue();
        if (v < 0) {
            SpannableStringBuilder sb = new SpannableStringBuilder("" + counter.getValue());
            sb.setSpan(new ScaleXSpan(MINUS_SYMBOL_SCALE), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            tvValue.setText(sb);
        } else tvValue.setText(String.format("%d", v));

    }

    private static int getTintColor(int backgroundColor) {
        int r = Color.red(backgroundColor);
        int g = Color.green(backgroundColor);
        int b = Color.blue(backgroundColor);
        int o = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return o > 125 ? Color.BLACK : Color.WHITE;
    }
}
