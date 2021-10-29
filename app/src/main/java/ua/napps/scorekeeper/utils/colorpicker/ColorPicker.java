package ua.napps.scorekeeper.utils.colorpicker;


import static ua.napps.scorekeeper.utils.Utilities.dip2px;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

import ua.napps.scorekeeper.R;

public class ColorPicker {

    private ColorPicker.OnChooseColorListener onChooseColorListener;
    private ColorPicker.OnFastChooseColorListener onFastChooseColorListener;

    public interface OnChooseColorListener {
        void onChooseColor(int position, int color);

        void onCancel();
    }

    public interface OnFastChooseColorListener {
        void setOnFastChooseColorListener(int position, int color);

        void onCancel();
    }

    public interface OnButtonListener {
        void onClick(View v, int position, int color);
    }

    private int[] colors = new int[]{};
    private boolean fastChooser;
    private String[] hex;
    private final WeakReference<Activity> mContext;
    private int columns;
    private int tickColor;
    private int marginColorButtonLeft, marginColorButtonRight, marginColorButtonTop, marginColorButtonBottom;
    private int colorButtonWidth, colorButtonHeight;
    private int colorButtonDrawable;
    private boolean fullHeight;
    private WeakReference<CustomDialog> mDialog;
    private final RecyclerView recyclerView;
    private int default_color;
    private final View dialogViewLayout;

    /**
     * Constructor
     */
    public ColorPicker(Activity context) {
        dialogViewLayout = LayoutInflater.from(context).inflate(R.layout.color_palette_layout, null, false);
        recyclerView = dialogViewLayout.findViewById(R.id.color_palette);

        this.mContext = new WeakReference<>(context);
        this.marginColorButtonLeft = this.marginColorButtonTop = this.marginColorButtonRight = this.marginColorButtonBottom = 5;
        this.default_color = 0;
        this.columns = 5;
    }

    /**
     * Set buttons color using a resource array of colors example : check in library  res/values/colorpicker-array.xml
     *
     * @param resId Array resource
     * @return this
     */
    public ColorPicker setColors(int resId) {
        if (mContext == null)
            return this;

        Context context = mContext.get();
        if (context == null)
            return this;

        hex = context.getResources().getStringArray(resId);
        colors = new int[hex.length];
        for (int i = 0; i < hex.length; i++) {
            colors[i] = Color.parseColor(hex[i]);
        }

        return this;
    }

    /**
     * Choose the color to be selected by default
     *
     * @param color int
     * @return this
     */
    public ColorPicker setDefaultColorButton(int color) {
        this.default_color = color;
        return this;
    }

    /**
     * Show the Material Dialog
     */
    public void show() {
        if (mContext == null)
            return;

        Activity context = mContext.get();
        if (context == null)
            return;

        if (colors == null || colors.length == 0)
            setColors();

        mDialog = new WeakReference<>(new CustomDialog(context, dialogViewLayout));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, columns);
        recyclerView.setLayoutManager(gridLayoutManager);
        ColorViewAdapter colorViewAdapter;
        if (fastChooser)
            colorViewAdapter = new ColorViewAdapter(colors, onFastChooseColorListener, mDialog);
        else
            colorViewAdapter = new ColorViewAdapter(colors);

        if (fullHeight) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            recyclerView.setLayoutParams(lp);
        }

        recyclerView.setAdapter(colorViewAdapter);


        if (tickColor != 0) {
            colorViewAdapter.setTickColor(tickColor);
        }
        if (marginColorButtonBottom != 0 || marginColorButtonLeft != 0 || marginColorButtonRight != 0 || marginColorButtonTop != 0) {
            colorViewAdapter.setColorButtonMargin(
                    dip2px(marginColorButtonLeft, context), dip2px(marginColorButtonTop, context),
                    dip2px(marginColorButtonRight, context), dip2px(marginColorButtonBottom, context));
        }
        if (colorButtonHeight != 0 || colorButtonWidth != 0) {
            colorViewAdapter.setColorButtonSize(dip2px(colorButtonWidth, context), dip2px(colorButtonHeight, context));
        }
        if (colorButtonDrawable != 0) {
            colorViewAdapter.setColorButtonDrawable(colorButtonDrawable);
        }

        if (default_color != 0) {
            colorViewAdapter.setDefaultColor(default_color);
        }

        if (mDialog == null) {
            return;
        }

        Dialog dialog = mDialog.get();

        if (dialog != null && !context.isFinishing()) {
            dialog.show();
            //Keep mDialog open when rotate
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

    }

    /**
     * Define the number of columns by default value= 3
     *
     * @param c Columns number
     * @return this
     */
    public ColorPicker setColumns(int c) {
        columns = c;
        return this;
    }

    /**
     * Set tick color
     *
     * @param color Color
     * @return this
     */
    public ColorPicker setColorButtonTickColor(int color) {
        this.tickColor = color;
        return this;
    }

    /**
     * Set a single drawable for all buttons example : you can define a different shape ( then round or square )
     *
     * @param drawable Resource
     * @return this
     */
    public ColorPicker setColorButtonDrawable(int drawable) {
        this.colorButtonDrawable = drawable;
        return this;
    }

    /**
     * Set the buttons size in DP
     *
     * @param width  width
     * @param height height
     * @return this
     */
    public ColorPicker setColorButtonSize(int width, int height) {
        this.colorButtonWidth = width;
        this.colorButtonHeight = height;
        return this;
    }

    /**
     * Set the Margin between the buttons in DP is 10
     *
     * @param left   left
     * @param top    top
     * @param right  right
     * @param bottom bottom
     * @return this
     */
    public ColorPicker setColorButtonMargin(int left, int top, int right, int bottom) {
        this.marginColorButtonLeft = left;
        this.marginColorButtonTop = top;
        this.marginColorButtonRight = right;
        this.marginColorButtonBottom = bottom;
        return this;
    }

    /**
     * set a fast listener ( it shows a mDialog without buttons and the event fires as soon you select a color )
     *
     * @param listener OnFastChooseColorListener
     */
    public void setOnFastChooseColorListener(OnFastChooseColorListener listener) {
        this.fastChooser = true;
        this.onFastChooseColorListener = listener;
        dismissDialog();
    }

    /**
     * set a listener for the color picked
     *
     * @param listener OnChooseColorListener
     */
    public ColorPicker setOnChooseColorListener(ColorPicker.OnChooseColorListener listener) {
        onChooseColorListener = listener;
        return this;
    }


    /**
     * set Match_parent to RecyclerView
     *
     * @return this
     */
    public ColorPicker setDialogFullHeight() {
        this.fullHeight = true;
        return this;
    }

    /**
     * getmDialog if you need more options
     *
     * @return CustomDialog
     */
    public
    @Nullable
    CustomDialog getmDialog() {
        if (mDialog == null)
            return null;
        return mDialog.get();
    }

    /**
     * getDialogViewLayout is the view inflated into the mDialog
     *
     * @return View
     */
    public View getDialogViewLayout() {
        return dialogViewLayout;
    }

    /**
     * dismiss the mDialog
     */
    public void dismissDialog() {
        if (mDialog == null)
            return;

        Dialog dialog = mDialog.get();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    private void setColors() {
        if (mContext == null)
            return;

        Context context = mContext.get();
        if (context == null)
            return;

        hex = context.getResources().getStringArray(R.array.bright_palette);
        colors = new int[hex.length];
        for (int i = 0; i < hex.length; i++) {
            colors[i] = Color.parseColor(hex[i]);
        }
    }

}