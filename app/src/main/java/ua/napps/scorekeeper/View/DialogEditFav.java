package ua.napps.scorekeeper.View;

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
import ua.napps.scorekeeper.Interactors.CurrentSet;
import ua.napps.scorekeeper.Models.FavoriteSet;

import static android.content.DialogInterface.BUTTON_NEUTRAL;
// TODO: wrap into DialogFragment
/*
public class DatePickerFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.date_picker_title)
            .setPositiveButton(android.R.string.ok, null)
            .create();
    }
}
*/
public class DialogEditFav extends AlertDialog.Builder {
    private FragmentFav.FavoriteSetsAdapter mFavoriteSetsAdapter;
    private boolean mIsNewSet;
    private FavoriteSet mFavoriteSet;
    @Bind(R.id.paintedView)
    View mPaintedView;
    @Bind(R.id.redSeekBar)
    SeekBar mRedBar;
    @Bind(R.id.greenSeekBar)
    SeekBar mGreenBar;
    @Bind(R.id.blueSeekBar)
    SeekBar mBlueBar;
    @Bind(R.id.setName)
    EditText mSetName;

    public DialogEditFav(final Context context, FragmentFav.FavoriteSetsAdapter adapter, FavoriteSet favoriteSet, boolean isNewSet) {
        super(context);
        this.mIsNewSet = isNewSet;
        this.mFavoriteSetsAdapter = adapter;
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.fav_dialog, null);
        setView(view);
        ButterKnife.bind(this, view);
        initDialogButtons(context.getString(R.string.button_negative), context.getString(R.string.button_positive), context.getString((R.string.button_neutral)));
        mRedBar.setOnSeekBarChangeListener(seekListener);
        mGreenBar.setOnSeekBarChangeListener(seekListener);
        mBlueBar.setOnSeekBarChangeListener(seekListener);
        if (!isNewSet && favoriteSet != null) {
            mFavoriteSet = favoriteSet;
            setSeekBarProgress(mFavoriteSet.getIconColor());
            mSetName.append(mFavoriteSet.getName());
        }

        AlertDialog dialog = create();
        dialog.show();
        dialog.getButton(BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.accentColor));
    }


    private void setSeekBarProgress(int color) {
        mRedBar.setProgress(Color.red(color));
        mGreenBar.setProgress(Color.green(color));
        mBlueBar.setProgress(Color.blue(color));
        mPaintedView.setBackgroundColor(color);
    }

    private void initDialogButtons(String negative, String positive, String neutral) {
        setNegativeButton(negative, null);
        setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                applyChanges();
            }
        });
        setNeutralButton(neutral, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFavoriteSetsAdapter.remove(mFavoriteSet);
            }
        });

    }

    private void applyChanges() {
        if (mIsNewSet) {
            FavoriteSet set = new FavoriteSet(mSetName.getText().toString());
            set.setCounters(CurrentSet.getCurrentSet().getCounters());
            set.setIconColor(getProgressRGBColor());
            mFavoriteSetsAdapter.add(set);

        } else {
            mFavoriteSet.setName(mSetName.getText().toString());
            mFavoriteSet.setIconColor(getProgressRGBColor());
            mFavoriteSetsAdapter.update(mFavoriteSet);
        }
    }

    private final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mPaintedView.setBackgroundColor(getProgressRGBColor());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private int getProgressRGBColor() {
        return Color.rgb(mRedBar.getProgress(), mGreenBar.getProgress(), mBlueBar.getProgress());
    }
}
