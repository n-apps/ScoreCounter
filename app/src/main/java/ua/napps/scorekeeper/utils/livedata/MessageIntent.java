package ua.napps.scorekeeper.utils.livedata;

import androidx.annotation.StringRes;

public class MessageIntent {

    @StringRes
    public final int messageResId;

    public MessageIntent(@StringRes int messageResId) {
        this.messageResId = messageResId;
    }
}
