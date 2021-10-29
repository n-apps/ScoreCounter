package ua.napps.scorekeeper.utils.colorpicker;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatDialog;

public class CustomDialog extends AppCompatDialog {
    private View view;

    public CustomDialog(Context context, View layout) {
        super(context);
        view = layout;
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);
    }
}