package ua.napps.scorekeeper.counters;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.dice.DicesFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener, DicesFragment.OnDiceFragmentInteractionListener {

    private Fragment countersFragment;
    private DicesFragment diceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationBar bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);

        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_plus_one_white, "Home"))
                .addItem(new BottomNavigationItem(R.drawable.ic_dice_white, "Books"))
                .addItem(new BottomNavigationItem(R.drawable.ic_settings, "Music"))
                .initialise();

        bottomNavigationBar.setTabSelectedListener(this);

        setDefaultFragment(CountersFragment.newInstance());
    }

    @Override
    public void onTabSelected(int position) {
        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        switch (position) {
            case 0:
                if (countersFragment == null) {
                    countersFragment = CountersFragment.newInstance();
                }
                transaction.replace(R.id.container, countersFragment);
                break;
            case 1:
                if (diceFragment == null) {
                    diceFragment = DicesFragment.newInstance();
                }
                transaction.replace(R.id.container, diceFragment);
                break;
            case 2:
                // settings
                break;
            default:
                break;
        }
        transaction.commit();
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    private void setDefaultFragment(Fragment fragment) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

