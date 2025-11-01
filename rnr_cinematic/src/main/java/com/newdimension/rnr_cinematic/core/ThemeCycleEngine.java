package com.newdimension.rnr_cinematic.core;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.newdimension.rnr_cinematic.anim.ThemeAnimation;

/** Owns theme order/timing and crossfades; delegates per-theme rendering to ThemeAnimation. */
public class ThemeCycleEngine {

    public interface Listener {
        void onThemeChanged(int themeIndex, float baseHueDegrees);
        void onTick(float progress01);
    }

    public static final long DEFAULT_THEME_HOLD_MS = 10_000L;
    public static final long DEFAULT_CROSSFADE_MS = 400L;
    public static final float DEFAULT_SAT = 0.90f;
    public static final float DEFAULT_VAL = 1.00f;

    private final float[] baseHuesDeg; // 6 themes by default
    private final long themeDurationMs;
    private final long crossfadeMs;
    private final float sat, val;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable advance = new Runnable() {
        @Override public void run() {
            if (!running) return;
            nextTheme();
        }
    };

    private ThemeAnimation current;
    private View target;
    private int idx = -1;
    private boolean running = false;
    private Listener listener;

    public ThemeCycleEngine(float[] baseHuesDeg, long themeDurationMs, long crossfadeMs, float sat, float val) {
        this.baseHuesDeg = baseHuesDeg.clone();
        this.themeDurationMs = themeDurationMs;
        this.crossfadeMs = crossfadeMs;
        this.sat = sat;
        this.val = val;
    }

    public void attach(View target) { this.target = target; }

    public void setListener(Listener l) { this.listener = l; }

    public void start(ThemeAnimation first) {
        if (running) return;
        running = true;
        idx = -1;
        this.current = first;
        if (current != null) {
            current.attach(target);
        }
        nextTheme();
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        if (current != null) current.stop();
    }

    public void dispose() {
        stop();
        if (current != null) current.dispose();
        current = null;
        target = null;
        listener = null;
    }

    private void nextTheme() {
        idx = (idx + 1) % baseHuesDeg.length;
        float baseHue = baseHuesDeg[idx];

        if (listener != null) listener.onThemeChanged(idx, baseHue);
        if (current != null) {
            current.setBaseHue(baseHue);
            current.start();
        }

        // schedule next advance
        handler.postDelayed(advance, themeDurationMs);
    }

    /** Convenience: set a solid HSV background immediately (used during crossfade or init). */
    public static void setHSVBackground(View view, float hueDeg, float sat, float val) {
        int color = Color.HSVToColor(new float[]{hueDeg, sat, val});
        view.setBackgroundColor(color);
    }

    public static TimeInterpolator easeInOut() {
        return input -> {
            // simple cubic ease-in-out
            if (input < 0.5f) {
                return 4f * input * input * input;
            } else {
                float f = (2f * input) - 2f;
                return 0.5f * f * f * f + 1f;
            }
        };
    }

    /** Optional helper to animate crossfade HSV between two hues. */
    public void crossfadeHue(final View view, float fromHueDeg, float toHueDeg) {
        final ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(crossfadeMs);
        va.setInterpolator(easeInOut());
        va.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            float hue = fromHueDeg + (toHueDeg - fromHueDeg) * t;
            setHSVBackground(view, hue, sat, val);
            if (listener != null) listener.onTick(t);
        });
        va.start();
    }
}
