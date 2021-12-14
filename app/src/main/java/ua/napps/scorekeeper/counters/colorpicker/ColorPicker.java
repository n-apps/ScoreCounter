package ua.napps.scorekeeper.counters.colorpicker;


import static ua.napps.scorekeeper.utils.Utilities.dip2px;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

import ua.napps.scorekeeper.R;

public class ColorPicker {

    private ColorPicker.OnFastChooseColorListener onFastChooseColorListener;

        public interface OnFastChooseColorListener {
        void setOnFastChooseColorListener(int position, int color);
    }

    private int[] colors = new int[]{};
    private boolean fastChooser;
    private String[] hex;
    private final WeakReference<Activity> mContext;
    private int columns;
    private final int marginColorButtonLeft;
    private final int marginColorButtonRight;
    private final int marginColorButtonTop;
    private final int marginColorButtonBottom;
    private int colorButtonWidth, colorButtonHeight;
    private WeakReference<CustomDialog> mDialog;
    private final RecyclerView recyclerView;
    private int default_color;
    private final View dialogViewLayout;


    public ColorPicker(Activity context) {
        dialogViewLayout = LayoutInflater.from(context).inflate(R.layout.color_palette_layout, null, false);
        recyclerView = dialogViewLayout.findViewById(R.id.color_palette);

        this.mContext = new WeakReference<>(context);
        this.marginColorButtonLeft = this.marginColorButtonTop = this.marginColorButtonRight = this.marginColorButtonBottom = 5;
        this.default_color = 0;
        this.columns = 5;
    }

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

    public ColorPicker setDefaultColor(int color) {
        this.default_color = color;
        return this;
    }

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

        recyclerView.setAdapter(colorViewAdapter);

        if (marginColorButtonBottom != 0 || marginColorButtonLeft != 0 || marginColorButtonRight != 0 || marginColorButtonTop != 0) {
            colorViewAdapter.setColorButtonMargin(
                    dip2px(marginColorButtonLeft, context), dip2px(marginColorButtonTop, context),
                    dip2px(marginColorButtonRight, context), dip2px(marginColorButtonBottom, context));
        }
        if (colorButtonHeight != 0 || colorButtonWidth != 0) {
            colorViewAdapter.setColorButtonSize(dip2px(colorButtonWidth, context), dip2px(colorButtonHeight, context));
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

    public ColorPicker setColumns(int c) {
        columns = c;
        return this;
    }

    public ColorPicker setColorButtonSize(int width, int height) {
        this.colorButtonWidth = width;
        this.colorButtonHeight = height;
        return this;
    }

    public void setOnFastChooseColorListener(OnFastChooseColorListener listener) {
        this.fastChooser = true;
        this.onFastChooseColorListener = listener;
        dismissDialog();
    }

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