package com.newdimension.rnr_cinematic.anim;

import android.view.View;

/**
 * Strategy interface for a background animation used during a single theme hold.
 * The ThemeCycleEngine will:
 *  - attach(view) once,
 *  - call setBaseHue() whenever the theme changes,
 *  - start()/stop() on lifecycle transitions,
 *  - dispose() when permanently finished with the instance.
 */
public interface ThemeAnimation {
    /** Called once when the algorithm will render on this root view. */
    void attach(View root);

    /** Supply/refresh the base hue in degrees [0..360). */
    void setBaseHue(float baseHueDegrees);

    /** Start or resume rendering. Should be idempotent. */
    void start();

    /** Pause/stop rendering (must not leak handlers/animators). */
    void stop();

    /** Release all resources; instance should not be reused after this. */
    void dispose();
}
