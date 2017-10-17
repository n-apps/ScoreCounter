package ua.napps.scorekeeper.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.Constants;
import ua.napps.scorekeeper.utils.TinyDB;

public class SettingsActivity extends AppCompatActivity {

  public static final int REQUEST_CODE = 2;
  public static final int RESULT_EDITED = 2001;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_settings);

      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    final TinyDB settingsDB = new TinyDB(getApplicationContext());
    boolean isStayAwake = settingsDB.getBoolean(Constants.SETTINGS_STAY_AWAKE);

//    stayAwake.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
//      @Override public void onPropertyChanged(Observable observable, int i) {
//        final ObservableBoolean checked = (ObservableBoolean) observable;
//        settingsDB.putBoolean(Constants.SETTINGS_STAY_AWAKE, checked.get());
//        setResult(RESULT_EDITED);
//      }
//    });
  }
}
