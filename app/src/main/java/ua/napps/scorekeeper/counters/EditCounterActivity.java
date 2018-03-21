package ua.napps.scorekeeper.counters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.transition.Transition;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.ColorUtil;
import ua.napps.scorekeeper.utils.TransitionListenerAdapter;
import ua.napps.scorekeeper.utils.ViewUtil;

public class EditCounterActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, EditCounterViewModel.EditCounterViewModelCallback {

    public static final int RESULT_DELETE = 1003;

    private static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";
    private static final String ARGUMENT_COUNTER_COLOR = "ARGUMENT_COUNTER_COLOR";
    private static final String STATE_IS_NAME_MODIFIED = "STATE_IS_NAME_MODIFIED";

    private Counter counter;
    private View revealView;
    private View revealBackground;
    private AppBarLayout appBar;
    private TextView counterStep;
    private TextInputLayout counterNameLayout;
    private EditText counterName;
    private TextView counterDefaultValue;
    private TextView labelChangesSaved;
    private TextView counterValue;
    private boolean isNameModified;
    private EditCounterViewModel viewModel;
    private Disposable disposable;

    public static void start(Activity activity, Counter counter, View view) {
        Intent intent = new Intent(activity, EditCounterActivity.class);
        intent.putExtra(ARGUMENT_COUNTER_ID, counter.getId());
        intent.putExtra(ARGUMENT_COUNTER_COLOR, counter.getColor());

        Bundle bundle = null;
        if (view != null) {
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, "backgroundColorImage").toBundle();
        }
        if (bundle == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivity(intent, bundle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.containsKey(ARGUMENT_COUNTER_ID)) {
            throw new UnsupportedOperationException("Activity should be started using the static start method");
        }
        setContentView(R.layout.activity_edit_counter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransitions();
        }
        ViewUtil.setLightStatusBar(this, false, ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.dark_status_bar));

        final int id = getIntent().getIntExtra(ARGUMENT_COUNTER_ID, 0);

        initViews();
        subscribeToModel(id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_NAME_MODIFIED, isNameModified);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            isNameModified = savedInstanceState.getBoolean(STATE_IS_NAME_MODIFIED);
        }
    }

    private void initViews() {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        counterName = findViewById(R.id.et_counter_name);
        counterStep = findViewById(R.id.tv_counter_step);
        counterDefaultValue = findViewById(R.id.tv_counter_default_value);
        counterValue = findViewById(R.id.tv_counter_value);
        labelChangesSaved = findViewById(R.id.tv_label_saved);
        revealBackground = findViewById(R.id.appbar_background);
        appBar = findViewById(R.id.app_bar);
        revealView = findViewById(R.id.reveal_view);
        counterNameLayout = findViewById(R.id.til_counter_name);
        findViewById(R.id.fab).setOnClickListener(v -> {
            new ColorChooserDialog.Builder(EditCounterActivity.this,
                    R.string.counter_details_color_picker_title)
                    .doneButton(R.string.common_select)
                    .cancelButton(R.string.common_cancel)
                    .customButton(R.string.common_custom)
                    .backButton(R.string.common_back)
                    .presetsButton(R.string.dialog_color_picker_presets_button)
                    .dynamicButtonColor(false)
                    .allowUserColorInputAlpha(false)
                    .show(EditCounterActivity.this);
        });
        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            setResult(RESULT_DELETE);
            viewModel.deleteCounter();
        });
        findViewById(R.id.counter_value).setOnClickListener(v -> {
            final MaterialDialog md = new MaterialDialog.Builder(EditCounterActivity.this)
                    .content(R.string.dialog_current_value_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeColorRes(R.color.primaryColor)
                    .negativeText(R.string.common_cancel)
                    .input(String.valueOf(counter.getValue()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newValue = input.toString();
                                    try {
                                        final int value = Integer.parseInt(newValue);
                                        viewModel.updateValue(value);
                                    } catch (NumberFormatException e) {
                                        Timber.e(e, "value: %s", newValue);
                                    }
                                }
                            })
                    .build();
            EditText inputEditText = md.getInputEditText();
            if (inputEditText != null) {
                inputEditText.setOnEditorActionListener((textView, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                            == EditorInfo.IME_ACTION_DONE)) {
                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                        positiveButton.callOnClick();
                    }
                    return false;
                });
            }
            md.show();
        });
        findViewById(R.id.counter_default_value).setOnClickListener(v -> {
            final MaterialDialog md = new MaterialDialog.Builder(EditCounterActivity.this)
                    .content(R.string.dialog_counter_default_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeText(R.string.common_cancel)
                    .negativeColorRes(R.color.primaryColor)
                    .input(String.valueOf(counter.getDefaultValue()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newDefaultValue = input.toString();
                                    try {
                                        final int value = Integer.parseInt(newDefaultValue);
                                        viewModel.updateDefaultValue(value);
                                    } catch (NumberFormatException e) {
                                        Timber.e(e, "value: %s", newDefaultValue);
                                    }
                                    Bundle params = new Bundle();
                                    params.putString(FirebaseAnalytics.Param.CHARACTER, newDefaultValue);
                                    AndroidFirebaseAnalytics.logEvent("counter_default_value_submit", params);

                                }
                            })
                    .build();
            EditText editText = md.getInputEditText();
            if (editText != null) {
                editText.setOnEditorActionListener((textView, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                            == EditorInfo.IME_ACTION_DONE)) {
                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                        positiveButton.callOnClick();
                    }
                    return false;
                });
            }
            md.show();
        });
        findViewById(R.id.counter_step).setOnClickListener(v -> {
            final MaterialDialog md = new MaterialDialog.Builder(EditCounterActivity.this)
                    .content(R.string.dialog_counter_step_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeText(R.string.common_cancel)
                    .negativeColorRes(R.color.primaryColor)
                    .input(String.valueOf(counter.getStep()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newStepValue = input.toString();
                                    try {
                                        final int value = Integer.parseInt(newStepValue);
                                        viewModel.updateStep(value);
                                    } catch (NumberFormatException e) {
                                        Timber.e(e, "value: %s", newStepValue);
                                    }
                                    Bundle params = new Bundle();
                                    params.putString(FirebaseAnalytics.Param.CHARACTER, newStepValue);
                                    AndroidFirebaseAnalytics.logEvent("counter_step_submit", params);

                                }
                            })
                    .build();
            EditText editText = md.getInputEditText();
            if (editText != null) {
                editText.setOnEditorActionListener((textView, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                            == EditorInfo.IME_ACTION_DONE)) {
                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                        positiveButton.callOnClick();
                    }
                    return false;
                });
            }
            md.show();
        });

        findViewById(R.id.iv_step_info).setOnClickListener(v -> {
            AndroidFirebaseAnalytics.logEvent("help_tooltip_click");
            new MaterialDialog.Builder(EditCounterActivity.this)
                    .content(R.string.dialog_step_info_content)
                    .positiveText(R.string.common_got_it)
                    .show();
        });

        findViewById(R.id.iv_default_value_info).setOnClickListener(v -> {
            AndroidFirebaseAnalytics.logEvent("help_tooltip_click");
            new MaterialDialog.Builder(EditCounterActivity.this)
                    .content(R.string.dialog_default_info_content)
                    .positiveText(R.string.common_got_it)
                    .show();
        });

        Observable<String> textChangeStream = createTextChangeObservable();
        disposable = textChangeStream
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    viewModel.updateName(s);
                    isNameModified = true;
                });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            revealView.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTransitions() {
        final String backgroundHex = getIntent().getExtras().getString(ARGUMENT_COUNTER_COLOR);
        final int backgroundColor = Color.parseColor(backgroundHex);
        Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
        sharedElementEnterTransition.addListener(new TransitionListenerAdapter() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onTransitionEnd(Transition transition) {
                reveal(backgroundColor);
            }

            @Override
            public void onTransitionStart(Transition transition) {
                revealView.setBackgroundColor(backgroundColor);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void reveal(int backgroundColor) {
        revealBackground.setBackgroundColor(backgroundColor);
        final Pair<Float, Float> center = ViewUtil.getCenter(revealView);
        Animator anim = ViewAnimationUtils.createCircularReveal(revealBackground, center.first.intValue(),
                center.second.intValue(), 0f, revealBackground.getWidth());
        anim.setDuration(400);
        revealBackground.setVisibility(View.VISIBLE);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                applyTintAccordingToCounterColor(backgroundColor);
            }
        });
        anim.start();
    }

    private void applyTintAccordingToCounterColor(int backgroundColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(backgroundColor);
        } else {
            appBar.setBackgroundColor(Color.parseColor(counter.getColor()));
        }
        boolean useLightTint = ColorUtil.isDarkBackground(backgroundColor);
        int color = ContextCompat.getColor(EditCounterActivity.this, useLightTint ? R.color.white : R.color.black);
        counterName.setTextColor(color);
        labelChangesSaved.setTextColor(color);
        counterNameLayout.setHintTextAppearance(useLightTint ? R.style.HintTextLight : R.style.HintTextDark);
        Drawable wrappedDrawable = DrawableCompat.wrap(counterName.getBackground());
        DrawableCompat.setTint(wrappedDrawable.mutate(), color);
        counterName.setBackground(wrappedDrawable);
        ViewUtil.setCursorTint(counterName, color);
        getSupportActionBar().setHomeAsUpIndicator(useLightTint ? R.drawable.ic_arrow_back_white : R.drawable.ic_arrow_back);
        ViewUtil.setLightStatusBar(EditCounterActivity.this, !useLightTint, backgroundColor, backgroundColor);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reveal(color);
            revealView.setBackgroundColor(color);
        } else {
            applyTintAccordingToCounterColor(color);
        }
        final String hex = ColorUtil.intColorToString(color);
        viewModel.updateColor(hex);
        Bundle params = new Bundle();
        params.putString(Param.CHARACTER, hex);
        AndroidFirebaseAnalytics.logEvent("edit_counter_color_selected", params);
    }

    private void subscribeToModel(int id) {
        EditCounterViewModelFactory factory = new EditCounterViewModelFactory(id, this);
        viewModel = ViewModelProviders.of(this, factory).get(EditCounterViewModel.class);
        viewModel.getCounterLiveData().observe(this, c -> {
            if (c != null) {
                counter = c;
                viewModel.setCounter(c);
                counterValue.setText(String.valueOf(c.getValue()));
                counterStep.setText(String.valueOf(c.getStep()));
                counterDefaultValue.setText(String.valueOf(c.getDefaultValue()));
                if (!c.getName().equals(counterName.getText().toString())) {
                    counterName.setText(c.getName());
                    counterName.setSelection(c.getName().length());
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    applyTintAccordingToCounterColor(Color.parseColor(counter.getColor()));
                }
            } else {
                counter = null;
                setResult(RESULT_DELETE);
                finish();
            }
        });
    }

    @Override
    public void showSavedState() {
        labelChangesSaved.setVisibility(View.VISIBLE);
    }

    private Observable<String> createTextChangeObservable() {
        Observable<String> textChangeObservable =
                Observable.create(e -> {
                    final TextWatcher textWatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start,
                                                      int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {
                            e.onNext(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    };
                    counterName.addTextChangedListener(textWatcher);
                    e.setCancellable(() -> counterName.removeTextChangedListener(textWatcher));
                });
        return textChangeObservable.distinctUntilChanged().debounce(1000, TimeUnit.MILLISECONDS);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void hideView() {
        final Pair<Float, Float> center = ViewUtil.getCenter(revealView);
        final Animator animator;
        animator = ViewAnimationUtils.createCircularReveal(revealBackground,
                center.first.intValue(), center.second.intValue(), revealBackground.getWidth(), 0);
        animator.setDuration(300);
        animator.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            hideView();
        }
    }

    @Override
    protected void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
        if (isNameModified && counter != null) {
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, counter.getName());
            AndroidFirebaseAnalytics.logEvent("counter_name_submit", params);
        }
    }
}
