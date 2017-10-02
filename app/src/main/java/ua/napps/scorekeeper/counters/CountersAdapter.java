package ua.napps.scorekeeper.counters;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.List;
import java.util.Objects;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ItemCounterBinding;

public class CountersAdapter extends RecyclerView.Adapter<CountersAdapter.ViewHolder> {

  List<? extends Counter> mProductList;
  private int height;

  @Nullable private final CounterActionCallback mProductClickCallback;

  public CountersAdapter(@Nullable CounterActionCallback callback) {
    mProductClickCallback = callback;
  }

  public void setProductList(final List<? extends Counter> productList) {
    if (mProductList == null) {
      mProductList = productList;
      notifyItemRangeInserted(0, productList.size());
    } else {
      DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
        @Override public int getOldListSize() {
          return mProductList.size();
        }

        @Override public int getNewListSize() {
          return productList.size();
        }

        @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
          return Objects.equals(mProductList.get(oldItemPosition).getId(),
              productList.get(newItemPosition).getId());
        }

        @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
          Counter newProduct = productList.get(newItemPosition);
          Counter oldProduct = mProductList.get(oldItemPosition);
          return Objects.equals(newProduct.getId(), oldProduct.getId()) && Objects.equals(
              newProduct.getColor(), oldProduct.getColor()) && Objects.equals(newProduct.getName(),
              oldProduct.getName()) && newProduct.getValue() == oldProduct.getValue();
        }
      });
      mProductList = productList;
      result.dispatchUpdatesTo(this);
    }
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    ItemCounterBinding binding =
        DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_counter,
            parent, false);
    return new ViewHolder(binding);
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {
    holder.binding.setCallback(mProductClickCallback);
    holder.binding.setData(mProductList.get(position));
    holder.binding.executePendingBindings();

    //ViewGroup.LayoutParams lp = holder.binding.getRoot().getLayoutParams();
    //if (lp instanceof FlexboxLayoutManager.LayoutParams) {
    //  FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
    //  flexboxLp.setFlexGrow(1.0f);
    //  final int itemCount = getItemCount();
    //
    //  if (itemCount > 4) {
    //    flexboxLp.setHeight(height == 0 ? 384 : height);
    //  } else {
    //    if (itemCount == 4) {
    //      height = holder.binding.getRoot().getHeight();
    //    }
    //    flexboxLp.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    //  }
    //}
  }

  @Override public int getItemCount() {
    return mProductList == null ? 0 : mProductList.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    final ItemCounterBinding binding;

    ViewHolder(ItemCounterBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
