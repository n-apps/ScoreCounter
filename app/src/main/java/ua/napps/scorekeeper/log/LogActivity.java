package ua.napps.scorekeeper.log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.ViewUtil;

public class LogActivity extends AppCompatActivity {

    private LogAdapter logAdapter;
    private Group emptyState;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, LogActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // get back button
        Toolbar toolbar = findViewById(R.id.toolbar_log_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // setup recycler view and adapter
        RecyclerView mRecyclerView = findViewById(R.id.rv_log_main);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.setHasFixedSize(true);

        logAdapter = new LogAdapter(Singleton.getInstance().getLogEntries());
        mRecyclerView.setAdapter(logAdapter);

        emptyState = findViewById(R.id.g_empty_history);
        emptyState.setVisibility(logAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        findViewById(R.id.btn_close).setOnClickListener(v -> finishAfterTransition());

        boolean nightModeActive = ViewUtil.isNightModeActive(this);
        ViewUtil.setLightMode(this, !nightModeActive);
        ViewUtil.setNavBarColor(this, !nightModeActive);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (logAdapter != null && logAdapter.getItemCount() > 0) {
            getMenuInflater().inflate(R.menu.menu_delete, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            new MaterialDialog.Builder(this)
                    .title(R.string.delete)
                    .content(R.string.dialog_confirmation_question)
                    .onPositive((dialog, which) -> {
                        logAdapter.notifyItemRangeRemoved(0, logAdapter.getItemCount());
                        Singleton.getInstance().clearLogEntries();
                        emptyState.setVisibility(View.VISIBLE);
                    })
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .showListener(dialog1 -> {
                        TextView content = ((MaterialDialog) dialog1).getContentView();
                        if (content != null) {
                            content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        }
                    })
                    .positiveText(R.string.delete)
                    .positiveColorRes(R.color.colorError)
                    .negativeText(R.string.dialog_no)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
