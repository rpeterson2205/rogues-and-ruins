package com.newdimension.rnr_cinematic.bg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
public class TransitionWhiteFlash extends BackgroundTransition {

    public TransitionWhiteFlash(ImageView current, ImageView next, Runnable onComplete) {
        super(current, next, onComplete);
    }

    @Override
    public void start() {
        ViewGroup parent = (ViewGroup) current.getParent();

        View flash = new View(parent.getContext());
        flash.setBackgroundColor(Color.WHITE);
        flash.setAlpha(0f);
        flash.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Insert just below the logo (above backgrounds)
        int insertIndex = Math.min(parent.getChildCount(), 2); // usually 0=bg,1=next,2=logo
        parent.addView(flash, insertIndex);

        // quick bloom
        flash.animate().alpha(1f).setDuration(DURATION_MS / 3).withEndAction(() -> {
            current.setImageDrawable(next.getDrawable()); // swap while flashed
            // fade flash away
            flash.animate().alpha(0f).setDuration(DURATION_MS * 2 / 3).withEndAction(() -> {
                parent.removeView(flash);
                onComplete.run();
            }).start();
        }).start();
    }
}
