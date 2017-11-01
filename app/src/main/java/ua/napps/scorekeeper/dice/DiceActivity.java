package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import ua.com.napps.scorekeeper.R;

public class DiceActivity extends AppCompatActivity {

    ImageView dice;

    TextView debugInfo;

    private DiceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        dice = findViewById(R.id.dice);
        debugInfo = findViewById(R.id.debug_info);

        dice.setOnClickListener(v -> viewModel.rollDice());

        viewModel = ViewModelProviders.of(this).get(DiceViewModel.class);

        subscribeToModel();
    }

    private void rollDice(int diceResult, int previousResult) {

        ((Animatable) dice.getDrawable()).stop();

        debugInfo.setText(String.format("From %d to %d", previousResult, diceResult));

        @DrawableRes int diceResId = 0;

        switch (diceResult) {
            case 1: {
                switch (previousResult) {
                    case 1: {
                        Toast.makeText(this, "The same!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_one;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_one;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_one;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_one;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_one;
                        break;
                    }
                }
                break;
            }
            case 2: {
                switch (previousResult) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_two;
                        break;
                    }
                    case 2: {
                        Toast.makeText(this, "The same!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_two;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_two;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_two;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_two;
                        break;
                    }
                }
                break;
            }
            case 3: {
                switch (previousResult) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_three;
                        break;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_three;
                        break;
                    }
                    case 3: {
                        Toast.makeText(this, "The same!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_three;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_three;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_three;
                        break;
                    }
                }
                break;
            }
            case 4: {
                switch (previousResult) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_four;
                        break;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_four;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_four;
                        break;
                    }
                    case 4: {
                        Toast.makeText(this, "The same!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_four;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_four;
                        break;
                    }
                }
                break;
            }
            case 5: {
                switch (previousResult) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_five;
                        break;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_five;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_five;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_five;
                        break;
                    }
                    case 5: {
                        Toast.makeText(this, "The same!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_five;
                        break;
                    }
                    default:
                        break;
                }
                break;
            }
            case 6: {
                switch (previousResult) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_six;
                        break;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_six;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_six;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_six;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_six;
                        break;
                    }
                    case 6: {
                        Toast.makeText(this, "The same!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

            }

        }
        if (diceResId == 0) {
            return;
        }

        AnimatedVectorDrawableCompat animatedVectorDrawableCompat = AnimatedVectorDrawableCompat
                .create(this, diceResId);
        dice.setImageDrawable(animatedVectorDrawableCompat);
        ((Animatable) dice.getDrawable()).start();

    }

    private void subscribeToModel() {

        final DiceLiveData diceLiveData = viewModel.getDiceLiveData();
        diceLiveData.observe(this, roll -> {
            if (roll != null && roll > 0) {
                rollDice(roll, diceLiveData.getPreviousValue());
            }
        });
    }
}
