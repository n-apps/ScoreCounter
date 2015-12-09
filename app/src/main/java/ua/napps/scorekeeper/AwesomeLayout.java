package ua.napps.scorekeeper;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Helpers.Constants;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.MainActivity;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class AwesomeLayout extends FrameLayout {
    private final LayoutTransition transition = new LayoutTransition();
    private Typeface ownFontType;
    private MainActivity context;

    public AwesomeLayout(Context context) {
        super(context);
    }

    public AwesomeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(MainActivity context) {
        this.context = context;
        setLayoutTransition(transition);
        ownFontType = Typeface.createFromAsset(getContext().getAssets(), "Lekton-Regular.ttf");
    }

    public void createCounterView(Counter counter) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        CounterView view = (CounterView) inflater.inflate(R.layout.view_counter, this, false);
        view.init(counter, context, ownFontType);
        addView(view, getChildCount());
        calcViewsBounds();
    }

    public void destroyCounterView(Counter counter) {
        for (int i = 0; i < getChildCount(); i++) {
            CounterView v = (CounterView) getChildAt(i);
            if (v.counter == counter) removeView(v);
        }
        calcViewsBounds();
    }

    private void calcViewsBounds() {
        final int q = getChildCount() - 1;
        if (q < 0) return;
        final int cols = Constants.colsArr[q];
        final int rows = Constants.rowsArr[q];
        final int orientation = getResources().getConfiguration().orientation;
        int rootW = getMeasuredWidth();
        int rootH = getMeasuredHeight();
        if (orientation == ORIENTATION_LANDSCAPE) {
            int t = rootW;
            rootW = rootH;
            rootH = t;
        }
        final int w = (int) Math.ceil((double) rootW / cols); // размеры counter view
        final int h = (int) Math.ceil((double) rootH / rows);
        boolean odd = false;
        if (q % 2 == 0) odd = true;
        int pos = 0;
        for (int y = 0; y < rows; y++) {
            if (pos == q && odd) {
                if (orientation == ORIENTATION_PORTRAIT) applyParams(pos, 0, y * h, rootW, h);
                else applyParams(pos, y * h, 0, h, rootW);
                break;
            }
            for (int x = 0; x < cols; x++) {
                if (orientation == ORIENTATION_PORTRAIT) applyParams(pos, x * w, y * h, w, h);
                else applyParams(pos, y * h, x * w, h, w);
                pos++;
                if (pos > q) break;
            }
        }
    }

    private void applyParams(int pos, int x, int y, int w, int h) {
        CounterView v = (CounterView) getChildAt(pos);
        v.update(x, y, w, h);
    }
}