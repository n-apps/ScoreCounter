package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityEditCounterBinding;
import ua.napps.scorekeeper.utils.ColorUtil;

public class EditCounterActivity extends AppCompatActivity
    implements ColorChooserDialog.ColorCallback {

  public static final int REQUEST_CODE = 1;
  public static final int RESULT_DELETE = 1003;
  public static final int RESULT_EDITED = 1004;

  protected static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";

  private ActivityEditCounterBinding binding;
  private EditCounterViewModel viewModel;

  public static Intent getIntent(Context context, final int id) {
    Intent intent = new Intent(context, EditCounterActivity.class);
    intent.putExtra(ARGUMENT_COUNTER_ID, id);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_counter);

    setSupportActionBar(binding.toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle("");

    final int id = getIntent().getIntExtra(ARGUMENT_COUNTER_ID, 0);

    EditCounterViewModel.Factory factory = new EditCounterViewModel.Factory(getApplication(), id);

    viewModel = ViewModelProviders.of(this, factory).get(EditCounterViewModel.class);

    subscribeToModel(viewModel);
    binding.setViewModel(viewModel);
  }

  private void subscribeToModel(EditCounterViewModel model) {
    // Observe product data
    model.getCounterLiveData().observe(this, c -> {
      if (c != null) {
        viewModel.setCounter(c);
        setResult(RESULT_EDITED);
      } else {
        setResult(RESULT_DELETE);
        finish();
      }
    });
  }

  public void onColorPickerClick(View v) {
    new ColorChooserDialog.Builder(this, R.string.dialog_select_color_title).doneButton(
        R.string.action_select)
        .cancelButton(R.string.md_cancel_label)
        .dynamicButtonColor(false)
        .allowUserColorInputAlpha(false)
        .show(this);
  }

  @Override public void onColorSelection(@NonNull ColorChooserDialog dialog, int color) {
    binding.colorPreview.setBackgroundColor(color);
    final String hex = ColorUtil.intColorToString(color);
    viewModel.updateColor(hex);
  }

  @Override public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
  }
}
