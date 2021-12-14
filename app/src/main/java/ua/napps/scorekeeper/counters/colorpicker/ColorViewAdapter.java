package ua.napps.scorekeeper.counters.colorpicker;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

import ua.napps.scorekeeper.R;


public class ColorViewAdapter extends RecyclerView.Adapter<ColorViewAdapter.ViewHolder> {

    private ColorPicker.OnFastChooseColorListener onFastChooseColorListener;
    private final int[] mColors;
    private int colorPosition = -1;
    private int marginButtonLeft = 0, marginButtonRight = 0, marginButtonTop = 3, marginButtonBottom = 3;
    private int buttonWidth = -1, buttonHeight = -1;
    private WeakReference<CustomDialog> mDialog;


    public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final AppCompatButton colorItem;

        public ViewHolder(View v) {
            super(v);
            //buttons settings
            colorItem = v.findViewById(R.id.color);
            colorItem.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) colorItem.getLayoutParams();
            layoutParams.setMargins(marginButtonLeft, marginButtonTop, marginButtonRight, marginButtonBottom);
            if (buttonWidth != -1)
                layoutParams.width = buttonWidth;
            if (buttonHeight != -1)
                layoutParams.height = buttonHeight;

        }

        @Override
        public void onClick(View v) {
            if (colorPosition != -1 && colorPosition != getLayoutPosition()) {
                notifyItemChanged(colorPosition);
            }
            colorPosition = getLayoutPosition();
            int colorSelected = (int) v.getTag();
            notifyItemChanged(colorPosition);

            if (onFastChooseColorListener != null && mDialog != null) {
                onFastChooseColorListener.setOnFastChooseColorListener(colorPosition, colorSelected);
                dismissDialog();
            }
        }
    }

    private void dismissDialog() {
        if (mDialog == null)
            return;
        Dialog dialog = mDialog.get();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public ColorViewAdapter(int[] colors, ColorPicker.OnFastChooseColorListener onFastChooseColorListener, WeakReference<CustomDialog> dialog) {
        mColors = colors;
        mDialog = dialog;
        this.onFastChooseColorListener = onFastChooseColorListener;
    }

    public ColorViewAdapter(int[] myDataset) {
        mColors = myDataset;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.palette_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color = mColors[position];
        holder.colorItem.setBackgroundColor(color);
        holder.colorItem.setTag(color);
    }

    @Override
    public int getItemCount() {
        return mColors.length;
    }

    public void setDefaultColor(int color) {
        for (int i = 0; i < mColors.length; i++) {
            if (mColors[i] == color) {
                colorPosition = i;
                notifyItemChanged(i);
            }
        }
    }

    public void setColorButtonMargin(int left, int top, int right, int bottom) {
        this.marginButtonLeft = left;
        this.marginButtonRight = right;
        this.marginButtonTop = top;
        this.marginButtonBottom = bottom;
    }

    public void setColorButtonSize(int width, int height) {
        this.buttonWidth = width;
        this.buttonHeight = height;
    }

}