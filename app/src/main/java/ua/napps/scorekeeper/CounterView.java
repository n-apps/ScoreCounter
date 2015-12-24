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
import android.widget.FrameLayout;
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
import static ua.napps.scorekeeper.Helpers.Constants.PREV_VALUE_SHOW_DURATION;
import static ua.napps.scorekeeper.Helpers.Constants.RIGHT;
import static ua.napps.scorekeeper.Helpers.Constants.SWIPE_THRESHOLD;
import static ua.napps.scorekeeper.Helpers.Constants.VALUE_TEXT_RATIO;
import static ua.napps.scorekeeper.Models.Counter.OnChangeListener;

public class CounterView extends FrameLayout implements View.OnClickListener, View.OnTouchListener, OnChangeListener {
    private final float density = getResources().getDisplayMetrics().density;
    private MainActivity mActivity;
    Counter mCounter;
    private long startShowingPrevValue;
    @Bind(R.id.rootCounterView) View root;
    @Bind(R.id.caption) TextView caption;
    @Bind(R.id.prevValue) TextView prevValue;
    @Bind(R.id.value) TextView tvValue;
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
        this.mCounter = counter;
        this.mActivity = callback;
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
        }
    }

    @Override
        public void onClick(View v) {
            EventBus.getDefault().post(new CounterCaptionClick(mCounter));
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
                        mActivity.onCounterSwipe(mCounter, delta > 0 ? RIGHT : LEFT, moved);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                boolean longClick = event.getEventTime() - event.getDownTime() > LONG_PRESS_TIMEOUT;
                if (longClick || moved) return true;
                showPrevValue();
                mActivity.onCounterSwipe(mCounter, event.getX() > v.getWidth() / 2 ? RIGHT : LEFT, moved);
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


    @Override
    public void onChangeColor() {
        root.setBackgroundColor(mCounter.getColor());
        defineColorsByBackground();
    }

    private void defineColorsByBackground() {
        int tint = getTintColor(mCounter.getColor());
        caption.setTextColor(tint);
        tvValue.setTextColor(tint);
        prevValue.setTextColor(tint);

    }

    @Override
    public void onChangeValues() {
        caption.setText(mCounter.getCaption());
        int v = mCounter.getValue();
        if (v < 0) {
            SpannableStringBuilder sb = new SpannableStringBuilder("" + mCounter.getValue());
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
