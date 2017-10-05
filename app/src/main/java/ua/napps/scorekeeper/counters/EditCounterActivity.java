package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
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

    binding.btnDelete.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        EditCounterActivity.this.setResult(RESULT_DELETE);
        EditCounterActivity.this.finish();
      }
    });
  }

  private void subscribeToModel(EditCounterViewModel model) {
    // Observe product data
    model.getCounter().observe(this, new Observer<Counter>() {
      @Override public void onChanged(@Nullable Counter c) {
        if (c != null) {
          viewModel.counterName.set(c.getName());
          viewModel.counterValue.set(c.getValue());
          setResult(RESULT_EDITED);
        }
      }
    });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_counter_save, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        break;
      case R.id.menu_save_counter: {
        if (fieldsIsValid()) {
          setResult(RESULT_EDITED);
          finish();
        } else {
          Toast.makeText(EditCounterActivity.this, "Wrong fields", Toast.LENGTH_SHORT).show();
        }
        return true;
      }
    }
    return false;
  }

  public void onColorPickerClick(View v) {
    new ColorChooserDialog.Builder(this, R.string.dialog_select_color_title).doneButton(
        R.string.action_select)
        .cancelButton(R.string.md_cancel_label)
        .dynamicButtonColor(false)
        .allowUserColorInputAlpha(false)
        .show(this);
  }

  private boolean fieldsIsValid() {
    return binding.etCounterName.getText().length() > 0;
  }

  @Override public void onColorSelection(@NonNull ColorChooserDialog dialog, int color) {
    binding.colorPreview.setBackgroundColor(color);
    final String hex = ColorUtil.intColorToString(color);
  }

  @Override public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
  }
}
