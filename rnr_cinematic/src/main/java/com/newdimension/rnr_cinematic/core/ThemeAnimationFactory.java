package com.newdimension.rnr_cinematic.core;

import com.newdimension.rnr_cinematic.anim.ThemeAnimation;

/** Produces a ThemeAnimation for a given theme index (and/or hue). */
public interface ThemeAnimationFactory {
    ThemeAnimation createForTheme(int themeIndex, float baseHueDegrees);
}
