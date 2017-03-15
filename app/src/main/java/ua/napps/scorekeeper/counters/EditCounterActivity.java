package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityEditCounterBinding;
import ua.napps.scorekeeper.data.CurrentSet;

public class EditCounterActivity extends AppCompatActivity
        implements ColorChooserDialog.ColorCallback {

    protected static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";
    private ActivityEditCounterBinding binding;
    private Counter counter;

    public static Intent getIntent(Context context, String id) {
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

        final String id = getIntent().getStringExtra(ARGUMENT_COUNTER_ID);

        if (id == null) throw new NullPointerException("Counter id is null!");

        counter = CurrentSet.getInstance().getCounter(id);

        if (counter == null) throw new NullPointerException("Counter with id " + id + " is null!");

        binding.setCounter(counter);
        binding.executePendingBindings();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_counter_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save_counter: {
                if (fieldsIsValid()) {
                    counter.setName(binding.counterName.getText().toString());
                    counter.setValue(Integer.parseInt(binding.counterValue.getText().toString()));
                    counter.setStep(Integer.parseInt(binding.counterStep.getText().toString()));
                    counter.setDefaultValue(
                            Integer.parseInt(binding.counterDefaultValue.getText().toString()));

                    CurrentSet.getInstance().replaceCounter(counter);
                    Toast.makeText(EditCounterActivity.this, "Counter updated", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else {
                    Toast.makeText(EditCounterActivity.this, "Wrong fields", Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            }
        }
        return false;
    }

    public void onColorPickerClick(View v) {
        new ColorChooserDialog.Builder(this, R.string.color_palette).titleSub(
                R.string.colors)  // title of dialog when viewing shades of a color
                .accentMode(
                        false)  // when true, will display accent palette instead of primary palette
                .doneButton(R.string.md_done_label)  // changes label of the done button
                .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                .backButton(R.string.md_back_label)  // changes label of the back button
                //.preselect(primaryPreselect)  // optionally preselects a color
                .dynamicButtonColor(
                        true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                .show();
    }

    private boolean fieldsIsValid() {
        return binding.counterName.getText().length() > 0;
    }

    @Override public void onColorSelection(@NonNull ColorChooserDialog dialog, int color) {
        binding.colorPreview.setBackgroundColor(color);
        binding.appBar.setBackground(new ColorDrawable(color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(CircleView.shiftColorDown(color));
        }
        counter.setBackgroundColor(color);
    }

    @Override public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }
}
