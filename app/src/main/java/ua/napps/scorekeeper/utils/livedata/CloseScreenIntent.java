package ua.napps.scorekeeper.utils.livedata;

public class CloseScreenIntent {

    public final int resultMessageResId;

    public CloseScreenIntent() {
        this(0);
    }

    public CloseScreenIntent(int resultMessageResId) {
        this.resultMessageResId = resultMessageResId;
    }
}
