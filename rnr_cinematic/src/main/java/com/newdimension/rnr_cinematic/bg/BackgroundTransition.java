package com.newdimension.rnr_cinematic.bg;

import android.widget.ImageView;

/** Abstract base for visual transitions between two images. */
public abstract class BackgroundTransition {
    protected final ImageView current;
    protected final ImageView next;
    protected final Runnable onComplete;

    protected BackgroundTransition(ImageView current, ImageView next, Runnable onComplete) {
        this.current = current;
        this.next = next;
        this.onComplete = onComplete;
    }

    /** Duration in ms. */
    public static final long DURATION_MS = 800L;

    public abstract void start();
}
