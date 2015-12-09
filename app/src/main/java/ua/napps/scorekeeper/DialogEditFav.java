package ua.napps.scorekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Adapters.FavoriteSetsAdapter;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Models.FavoriteSet;

import static android.content.DialogInterface.BUTTON_NEUTRAL;

public class DialogEditFav extends AlertDialog.Builder {
    private final FavoriteSetsAdapter adapter;
    private final FavoriteSet favItem;
    private final boolean isCurrentSet;
    @Bind(R.id.paintedView) View paintedView;
    @Bind(R.id.redSeekBar) SeekBar redBar;
    @Bind(R.id.greenSeekBar) SeekBar greenBar;
    @Bind(R.id.blueSeekBar) SeekBar blueBar;
    @Bind(R.id.setName) EditText favName;

    public DialogEditFav(final Context context, FavoriteSetsAdapter adapter, FavoriteSet favItem, boolean isCurrentSet) {
        super(context);
        this.adapter = adapter;
        this.favItem = favItem;
        this.isCurrentSet = isCurrentSet;
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.fav_dialog, null);
        setView(view);
        ButterKnife.bind(this,view);
        initDialogButtons(context.getString(R.string.button_negative), context.getString(R.string.button_positive), context.getString((R.string.button_neutral)));
        redBar.setOnSeekBarChangeListener(seekListener);
        greenBar.setOnSeekBarChangeListener(seekListener);
        blueBar.setOnSeekBarChangeListener(seekListener);
        setSeekBarProgress(favItem.getIconColor());
        favName.append(favItem.getName());
        //
        AlertDialog dialog = create();
        dialog.show();
        dialog.getButton(BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.accentColor));
    }



    private void setSeekBarProgress(int color) {
        redBar.setProgress(Color.red(color));
        greenBar.setProgress(Color.green(color));
        blueBar.setProgress(Color.blue(color));
        paintedView.setBackgroundColor(color);
    }

    private void initDialogButtons(String negative, String positive, String neutral) {
        setNegativeButton(negative, null);
        setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                applyChanges();
            }
        });
        if (!isCurrentSet) {
            setNeutralButton(neutral, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.delFavItem(favItem);
                }
            });
        }
    }

    private void applyChanges() {
        if (isCurrentSet) {
            FavoriteSet item = new FavoriteSet(favName.getText().toString());
            item.setIconColor(getProgressRGBColor());
            for (Counter c : favItem.getCounters()) item.getCounters().add(Counter.getClone(c));
            adapter.saveCurrentSet(item);
        } else {
            favItem.setName(favName.getText().toString());
            favItem.setIconColor(getProgressRGBColor());
            adapter.notifyDataSetChanged();
        }
    }

    private final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            paintedView.setBackgroundColor(getProgressRGBColor());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private int getProgressRGBColor() {
        return Color.rgb(redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
    }
}
