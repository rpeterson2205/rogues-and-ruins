package com.newdimension.rnr_cinematic.anim;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;

/** Gentle pulse using phased hue/saturation/value shifts for a richer “gradient-like” feel. */
public class GradientPulseAlgorithm implements ThemeAnimation {
    private static final long PERIOD_MS = 800L;

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
        animator.setInterpolator(easeInOut());
        animator.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();   // 0..1..0
            float hue = norm(baseHue + (t - 0.5f) * 14f);     // ±7°
            float sat = 0.85f + 0.10f * t;                    // 0.85..0.95
            float val = 0.92f + 0.08f * (1f - t);             // 1.00..0.92..1.00 (counterphase)
            pushColor(root, Color.HSVToColor(new float[]{hue, sat, val}));
        });
        animator.start();
    }

    @Override public void stop() { if (animator != null) { animator.cancel(); animator = null; } }
    @Override public void dispose() { stop(); root = null; }

    private static float norm(float h){ h%=360f; return h<0?h+360f:h; }
    private static TimeInterpolator easeInOut(){ return x->{ if(x<0.5f)return 4*x*x*x; float f=2*x-2;return 0.5f*f*f*f+1;}; }
    private static void pushColor(View v,int c){
        if (v instanceof com.newdimension.rnr_cinematic.ui.LogoMaskView)
            ((com.newdimension.rnr_cinematic.ui.LogoMaskView)v).setFillColor(c);
        else v.setBackgroundColor(c);
    }
}
