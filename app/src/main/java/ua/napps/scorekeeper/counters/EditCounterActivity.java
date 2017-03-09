package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityEditCounterBinding;

public class EditCounterActivity extends AppCompatActivity {

    protected static final String ARGUMENT_COUNTER = "ARGUMENT_COUNTER";
    private ActivityEditCounterBinding binding;

    public static Intent getIntent(Context context, Counter counter) {
        Intent intent = new Intent(context, EditCounterActivity.class);
        intent.putExtra(ARGUMENT_COUNTER, counter);
        return intent;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_counter);

        final Counter counter = getIntent().getParcelableExtra(ARGUMENT_COUNTER);
        binding.setCounter(counter);
    }
}
