package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityEditCounterBinding;
import ua.napps.scorekeeper.data.CurrentSet;

public class EditCounterActivity extends AppCompatActivity {

    protected static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";
    private ActivityEditCounterBinding binding;

    public static Intent getIntent(Context context, String id) {
        Intent intent = new Intent(context, EditCounterActivity.class);
        intent.putExtra(ARGUMENT_COUNTER_ID, id);
        return intent;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_counter);

        final String id = getIntent().getStringExtra(ARGUMENT_COUNTER_ID);

        if (id == null) throw new NullPointerException("Counter id is null!");

        final Counter counter = CurrentSet.getInstance().getCounter(id);

        if (counter == null) throw new NullPointerException("Counter with id " + id + " is null!");

        binding.setCounter(counter);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                counter.setName(binding.counterName.getText().toString());
                Toast.makeText(EditCounterActivity.this, "Counter updated", Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        });
    }
}
