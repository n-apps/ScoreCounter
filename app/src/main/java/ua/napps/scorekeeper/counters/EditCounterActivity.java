package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.ColorUtil;

public class EditCounterActivity extends AppCompatActivity
    implements ColorChooserDialog.ColorCallback {

  public static final int REQUEST_CODE = 1;
  public static final int RESULT_DELETE = 1003;
  public static final int RESULT_EDITED = 1004;

  protected static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";

  private EditCounterViewModel viewModel;

  public static Intent getIntent(Context context, final int id) {
    Intent intent = new Intent(context, EditCounterActivity.class);
    intent.putExtra(ARGUMENT_COUNTER_ID, id);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_edit_counter);

      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle("");

    final int id = getIntent().getIntExtra(ARGUMENT_COUNTER_ID, 0);

    viewModel = getViewModel(id);

    subscribeToModel();
  }

  private EditCounterViewModel getViewModel(int id) {
    CountersDao countersDao = DatabaseHolder.database().countersDao();
    EditCounterViewModelFactory factory = new EditCounterViewModelFactory(id, countersDao);
    return ViewModelProviders.of(this, factory).get(EditCounterViewModel.class);
  }

  private void subscribeToModel() {
    viewModel.getCounterLiveData().observe(this, c -> {
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
//    binding.colorPreview.setBackgroundColor(color);
    final String hex = ColorUtil.intColorToString(color);
    viewModel.updateColor(hex);
  }

  @Override public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
  }
}
