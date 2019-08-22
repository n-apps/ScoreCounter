package ua.napps.scorekeeper.utils.livedata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SingleShotEvent<T> {

    @NonNull
    public final T value;
    private boolean consumed;

    public SingleShotEvent(@NonNull T value) {
        this.value = value;
    }

    @Nullable
    public T getValueAndConsume() {
        if (consumed) return null;
        consumed = true;
        return value;
    }
}
