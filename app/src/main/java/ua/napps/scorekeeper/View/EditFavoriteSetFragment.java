package ua.napps.scorekeeper.View;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Interactors.CurrentSet;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.Utils.ColorUtil;
import ua.napps.scorekeeper.Utils.KeyboardUtil;

import static android.content.DialogInterface.BUTTON_NEUTRAL;

/**
 * Created by novo on 2015-12-26.
 */
public class EditFavoriteSetFragment extends DialogFragment {

    private boolean mIsNewSet;
    private FavoriteSet mFavoriteSet;
    @Bind(R.id.color_header)
    LinearLayout mColorHeader;
    @Bind(R.id.redSeekBar)
    SeekBar mRedBar;
    @Bind(R.id.greenSeekBar)
    SeekBar mGreenBar;
    @Bind(R.id.blueSeekBar)
    SeekBar mBlueBar;
    @Bind(R.id.setName)
    EditText mSetName;

    private EditFavSetDialogListener mCallback;

    public EditFavoriteSetFragment() {
    }

    public static EditFavoriteSetFragment newInstance(FavoriteSet set, boolean isNewSet) {
        EditFavoriteSetFragment frag = new EditFavoriteSetFragment();
        Bundle args = new Bundle();
        args.putSerializable("set", set);
        args.putBoolean("isNewSet", isNewSet);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.favorite_set_dialog, null);
        ButterKnife.bind(this, view);

        mIsNewSet = getArguments().getBoolean("isNewSet");
        mFavoriteSet = (FavoriteSet) getArguments().getSerializable("set");

        mRedBar.setOnSeekBarChangeListener(seekListener);
        mGreenBar.setOnSeekBarChangeListener(seekListener);
        mBlueBar.setOnSeekBarChangeListener(seekListener);

        if (!mIsNewSet) {
            setSeekBarProgress(mFavoriteSet.getIconColor());
            mSetName.append(mFavoriteSet.getName());
        } else {
            setSeekBarProgress(ColorUtil.getRandomColor());
            KeyboardUtil.showKeyboard(getActivity(), mSetName);
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(getContext().getString(R.string.button_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                applyChanges();
            }
        });
        alertDialogBuilder.setNegativeButton(getContext().getString(R.string.button_negative), null);
        if (!mIsNewSet) {
            alertDialogBuilder.setNeutralButton(getContext().getString(R.string.button_neutral), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCallback.onFavSetDeleted(mFavoriteSet);
                }
            });
        }
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
        dialog.getButton(BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(getContext(), R.color.accentColor));
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (EditFavSetDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement EditFavSetDialogListener");
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        KeyboardUtil.hideKeyboard(getActivity());
    }

    private void setSeekBarProgress(int color) {
        mRedBar.setProgress(Color.red(color));
        mGreenBar.setProgress(Color.green(color));
        mBlueBar.setProgress(Color.blue(color));
        mColorHeader.setBackgroundColor(color);
        mSetName.setTextColor(ColorUtil.getContrastColor(getProgressRGBColor()));
    }

    private void applyChanges() {
        if (mIsNewSet) {
            FavoriteSet set = new FavoriteSet(mSetName.getText().toString().trim());
            set.setCounters(CurrentSet.getCurrentSet().getCounters());
            set.setIconColor(getProgressRGBColor());
            mCallback.onFavSetAdded(set);

        } else {
            mFavoriteSet.setName(mSetName.getText().toString().trim());
            mFavoriteSet.setIconColor(getProgressRGBColor());
            mCallback.onFavSetUpdated(mFavoriteSet);
        }
    }

    private final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mColorHeader.setBackgroundColor(getProgressRGBColor());
            mSetName.setTextColor(ColorUtil.getContrastColor(getProgressRGBColor()));

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

    public interface EditFavSetDialogListener {
        void onFavSetUpdated(FavoriteSet set);

        void onFavSetDeleted(FavoriteSet set);

        void onFavSetAdded(FavoriteSet set);
    }
}
