package ua.napps.scorekeeper.settings;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import io.paperdb.Paper;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivitySettingsBinding;
import ua.napps.scorekeeper.utils.Constants;

public class SettingsActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivitySettingsBinding binding =
        DataBindingUtil.setContentView(this, R.layout.activity_settings);

    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    boolean isStayAwake = true;
    final ObservableBoolean stayAwake = new ObservableBoolean(isStayAwake);
    binding.setStayAwake(stayAwake);
    stayAwake.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable observable, int i) {
        final ObservableBoolean checked = (ObservableBoolean) observable;
        Paper.book(Constants.SETTINGS).write(Constants.SETTINGS_STAY_AWAKE, checked.get());
      }
    });
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        supportFinishAfterTransition();
        break;
    }
    return false;
  }
}
