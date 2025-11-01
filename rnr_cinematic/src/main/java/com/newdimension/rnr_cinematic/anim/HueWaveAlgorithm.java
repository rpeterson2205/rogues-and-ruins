package com.newdimension.rnr_cinematic.anim;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;

/** Continuous hue orbit from the base hue (slow wave). */
public class HueWaveAlgorithm implements ThemeAnimation {
    private static final long PERIOD_MS = 600L; // full rotation

    private View root;
    private float baseHue;
    private ValueAnimator animator;

    @Override public void attach(View root) { this.root = root; }
    @Override public void setBaseHue(float baseHueDegrees) { this.baseHue = norm(baseHueDegrees); }

    @Override public void start() {
        if (root == null) return;
        stop();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(PERIOD_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue(); // 0..1
            float hue = norm(baseHue + 360f * t);
            pushColor(root, Color.HSVToColor(new float[]{hue, 0.95f, 1.00f}));
        });
        animator.start();
    }

    @Override public void stop() { if (animator != null) { animator.cancel(); animator = null; } }
    @Override public void dispose() { stop(); root = null; }

    private static float norm(float h){ h%=360f; return h<0?h+360f:h; }
    private static void pushColor(View v,int c){
        if (v instanceof com.newdimension.rnr_cinematic.ui.LogoMaskView)
            ((com.newdimension.rnr_cinematic.ui.LogoMaskView)v).setFillColor(c);
        else v.setBackgroundColor(c);
    }
}
