package ua.napps.scorekeeper.utils.livedata;

public class CloseScreenIntent {

    public final int resultMessageResId;
    public final boolean dueToError;

    public CloseScreenIntent() {
        this(0, false);
    }

    public CloseScreenIntent(int resultMessageResId, boolean dueToError) {
        this.resultMessageResId = resultMessageResId;
        this.dueToError = dueToError;
    }
}
