package com.newdimension.rnr_cinematic.bg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.widget.ImageView;

public class TransitionCrossDissolve extends BackgroundTransition {

    public TransitionCrossDissolve(ImageView current, ImageView next, Runnable onComplete) {
        super(current, next, onComplete);
    }

    @Override
    public void start() {
        next.setAlpha(0f);
        next.setVisibility(ImageView.VISIBLE);

        next.animate().alpha(1f).setDuration(DURATION_MS).withEndAction(() -> {
            // commit the new image to current, hide next
            current.setImageDrawable(next.getDrawable());
            next.setVisibility(ImageView.INVISIBLE);
            next.setAlpha(0f);
            onComplete.run();
        }).start();
    }

}
