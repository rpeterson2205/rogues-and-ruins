package com.newdimension.rnr_cinematic.anim;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;

import java.util.Random;

/** Soft “sparkle” using stochastic brightness flicker with smooth easing. */
public class SparkleNoiseAlgorithm implements ThemeAnimation {
    private static final long PERIOD_MS = 180L;
    private static final float SAT = 0.95f;
    private static final float VAL_BASE = 0.92f;
    private static final float VAL_RANGE = 1.00f; // up to 1.00

    private final Random rng = new Random();
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
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue(); // 0..1..0
            // new random target every cycle; eased by t
            float target = VAL_BASE + VAL_RANGE * rng.nextFloat();
            float val = VAL_BASE + (target - VAL_BASE) * t;
            pushColor(root, Color.HSVToColor(new float[]{baseHue, SAT, val}));
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
