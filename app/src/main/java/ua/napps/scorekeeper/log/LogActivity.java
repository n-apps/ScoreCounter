package ua.napps.scorekeeper.log;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.ViewUtil;

public class LogActivity extends AppCompatActivity {

    private LogAdapter mAdapter;
    private Group emptyState;

    public static void start(Activity activity) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        Intent intent = new Intent(activity, LogActivity.class);
        activity.startActivity(intent, options.toBundle());
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

        mAdapter = new LogAdapter(Singleton.getInstance().getLogEntries());
        mRecyclerView.setAdapter(mAdapter);

        emptyState = findViewById(R.id.g_empty_history);
        emptyState.setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        findViewById(R.id.btn_close).setOnClickListener(v -> finishAfterTransition());

        boolean isLightTheme = LocalSettings.isLightTheme();
        if (isLightTheme) {
            ViewUtil.setLightStatusBar(this);
        } else {
            ViewUtil.clearLightStatusBar(this);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            getMenuInflater().inflate(R.menu.menu_delete, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            Typeface medium = null;
            Typeface regular = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                medium = getResources().getFont(R.font.ptm700);
                regular = getResources().getFont(R.font.ptm400);
            }
            new MaterialDialog.Builder(this)
                    .content(R.string.dialog_confirmation_question)
                    .onPositive((dialog, which) -> {
                        Singleton.getInstance().clearLogEntries();
                        mAdapter.notifyDataSetChanged();
                        emptyState.setVisibility(View.VISIBLE);
                    })
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .showListener(dialog1 -> {
                        TextView content = ((MaterialDialog) dialog1).getContentView();
                        if (content != null) {
                            content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        }
                    })
                    .typeface(medium, regular)
                    .positiveText(R.string.dialog_yes)
                    .negativeText(R.string.dialog_no)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
