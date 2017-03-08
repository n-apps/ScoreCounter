package ua.napps.scorekeeper.utils;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.databinding.adapters.ListenerUtil;
import android.os.Build;
import android.support.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import ua.com.napps.scorekeeper.BR;
import ua.com.napps.scorekeeper.R;

public class DataBindingAdapters {

    public static final String TAG = "DataBindingAdapters";

    /**
     * Prevent instantiation
     */
    private DataBindingAdapters() {
    }

    /**
     * Assign a list of items to a ViewGroup. This is used with the {@code entries} and
     * {@code layout} attributes in the application namespace. Example Usage:
     * <pre><code>&lt;LinearLayout
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:orientation="vertical"
     *     app:entries="@{items}"
     *     app:layout="@{@layout/item}"/&gt;
     * </code></pre>
     * <p>
     * In the above, {@code items} is a List or ObservableList. {@code layout} does not
     * need to be hard-coded, but most commonly will be. This BindingAdapter will work
     * with any ViewGroup that only needs addView() and removeView() to manage its Views.
     * <p>
     * The layout, &commat;layout/item for example, must have a single variable named
     * {@code data}.
     */
    @BindingAdapter({ "entries", "layout" }) public static <T> void setEntries(
            ViewGroup viewGroup, List<T> oldEntries, int oldLayoutId, List<T> newEntries,
            int newLayoutId) {
        if (oldEntries == newEntries && oldLayoutId == newLayoutId) {
            return; // nothing has changed
        }

        EntryChangeListener listener = ListenerUtil.getListener(viewGroup, R.id.entryListener);
        if (oldEntries != newEntries && listener != null && oldEntries instanceof ObservableList) {
            ((ObservableList) oldEntries).removeOnListChangedCallback(listener);
        }

        if (newEntries == null) {
            viewGroup.removeAllViews();
        } else {
            if (newEntries instanceof ObservableList) {
                if (listener == null) {
                    listener = new EntryChangeListener(viewGroup, newLayoutId);
                    ListenerUtil.trackListener(viewGroup, listener, R.id.entryListener);
                } else {
                    listener.setLayoutId(newLayoutId);
                }
                if (newEntries != oldEntries) {
                    ((ObservableList) newEntries).addOnListChangedCallback(listener);
                }
            }
            resetViews(viewGroup, newLayoutId, newEntries);
        }
    }

    /**
     * Inflates and binds a layout to an entry to the {@code data} variable
     * of the bound layout.
     *
     * @param inflater The LayoutInflater
     * @param parent The ViewGroup containing the list of Views
     * @param layoutId The layout ID to use for the list item
     * @param entry The data to bind to the inflated View
     * @return A ViewDataBinding, bound to a newly-inflated View with {@code entry}
     * set as the {@code data} variable.
     */
    private static ViewDataBinding bindLayout(LayoutInflater inflater, ViewGroup parent,
            int layoutId, Object entry) {
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        if (!binding.setVariable(BR.data, entry)) {
            String layoutName = parent.getResources().getResourceEntryName(layoutId);
            Log.w(TAG, "There is no variable 'data' in layout " + layoutName);
        }
        return binding;
    }

    /**
     * Clears all Views in {@code parent} and fills it with a View for
     * each item in {@code entries}, bound to the item. If layoutId
     * is 0, no Views will be added.
     *  @param parent The ViewGroup to contain the list of items.
     * @param layoutId The layout ID to inflate for the child Views.
     * @param entries The list of items to bind to the inflated Views. Each
     */
    private static void resetViews(ViewGroup parent, int layoutId, List entries) {
        parent.removeAllViews();
        if (layoutId == 0) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < entries.size(); i++) {
            Object entry = entries.get(i);
            ViewDataBinding binding = bindLayout(inflater, parent, layoutId, entry);
            parent.addView(binding.getRoot());
        }
    }

    /**
     * Starts a transition only if on KITKAT or higher.
     *
     * @param root The scene root
     */
    private static void startTransition(ViewGroup root) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(root);
        }
    }

    /**
     * A listener to watch for changes in an Observable list and
     * animate the change.
     */
    private static class EntryChangeListener extends ObservableList.OnListChangedCallback {
        private final ViewGroup mTarget;
        private int mLayoutId;

        public EntryChangeListener(ViewGroup target, int layoutId) {
            mTarget = target;
            mLayoutId = layoutId;
        }

        public void setLayoutId(int layoutId) {
            mLayoutId = layoutId;
        }

        @Override public void onChanged(ObservableList observableList) {
            resetViews(mTarget, mLayoutId, observableList);
        }

        @Override
        public void onItemRangeChanged(ObservableList observableList, int start, int count) {
            if (mLayoutId == 0) {
                return;
            }
            LayoutInflater inflater = (LayoutInflater) mTarget.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            startTransition(mTarget);
            final int end = start + count;
            for (int i = start; i < end; i++) {
                Object data = observableList.get(i);
                ViewDataBinding binding = bindLayout(inflater, mTarget, mLayoutId, data);
                binding.setVariable(BR.data, data);
                mTarget.removeViewAt(i);
                mTarget.addView(binding.getRoot(), i);
            }
        }

        @Override
        public void onItemRangeInserted(ObservableList observableList, int start, int count) {
            if (mLayoutId == 0) {
                return;
            }
            startTransition(mTarget);
            final int end = start + count;
            LayoutInflater inflater = (LayoutInflater) mTarget.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (int i = end - 1; i >= start; i--) {
                Object entry = observableList.get(i);
                ViewDataBinding binding = bindLayout(inflater, mTarget, mLayoutId, entry);
                mTarget.addView(binding.getRoot(), start);
            }
        }

        @Override
        public void onItemRangeMoved(ObservableList observableList, int from, int to, int count) {
            if (mLayoutId == 0) {
                return;
            }
            startTransition(mTarget);
            for (int i = 0; i < count; i++) {
                View view = mTarget.getChildAt(from);
                mTarget.removeViewAt(from);
                int destination = (from > to) ? to + i : to;
                mTarget.addView(view, destination);
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList observableList, int start, int count) {
            if (mLayoutId == 0) {
                return;
            }
            startTransition(mTarget);
            for (int i = 0; i < count; i++) {
                mTarget.removeViewAt(start);
            }
        }
    }
}
