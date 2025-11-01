package com.newdimension.rnr_cinematic.bg;

import android.widget.ImageView;

public class TransitionChromaFade extends BackgroundTransition {

    public TransitionChromaFade(ImageView current, ImageView next, Runnable onComplete) {
        super(current, next, onComplete);
    }

    @Override
    public void start() {
        // phase 1: fade current to black (0 alpha)
        current.animate().alpha(0f).setDuration(DURATION_MS / 2).withEndAction(() -> {
            // swap image while fully black
            current.setImageDrawable(next.getDrawable());
            // phase 2: fade new image up
            current.animate().alpha(1f).setDuration(DURATION_MS / 2).withEndAction(onComplete).start();
        }).start();
    }}
