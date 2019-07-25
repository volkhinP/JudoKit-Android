package com.judopay.view;

import android.view.View;

public abstract class SingleClickOnClickListener implements View.OnClickListener {

    private static boolean enabled = true;

    private static final Runnable ENABLE_AGAIN = new Runnable() {
        @Override
        public void run() {
            enabled = true;
        }
    };

    @Override
    public final void onClick(final View v) {
        if (enabled) {
            enabled = false;
            v.post(ENABLE_AGAIN);
            doClick();
        }
    }

    public abstract void doClick();
}