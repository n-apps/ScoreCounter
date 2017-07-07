package ua.napps.scorekeeper.favorites;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import io.paperdb.Paper;
import java.util.ArrayList;
import java.util.List;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityFavoritesBinding;
import ua.napps.scorekeeper.utils.Constants;

public class FavoritesActivity extends AppCompatActivity {

  private ActivityFavoritesBinding binding;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_counters);

    setSupportActionBar(binding.toolbar);
    RecyclerView recyclerView = binding.recyclerView;
    recyclerView.setHasFixedSize(true);
    LinearLayoutManager llm = new LinearLayoutManager(this);
    llm.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(llm);
    List<FavoriteSet> result = new ArrayList<>();
    final List<String> keys = Paper.book(Constants.FAVORITES_COUNTER_SETS).getAllKeys();
    for (String key : keys) {
      if (Paper.book().exist(key)) {
        result.add(Paper.book().read(key));
      }
    }
    recyclerView.setAdapter(new FavoritesAdapter(this, result));
  }
}
