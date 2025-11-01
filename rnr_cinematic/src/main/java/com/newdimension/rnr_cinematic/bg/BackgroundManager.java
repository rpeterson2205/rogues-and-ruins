package com.newdimension.rnr_cinematic.bg;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import com.newdimension.rnr_cinematic.R;

import android.widget.FrameLayout;
import android.view.ViewGroup;

public class BackgroundManager {

    private final BackgroundLoader loader;
    private final ImageView currentView;
    private final ImageView nextView;
    private int modeIndex = 0; // 0,1,2 repeating

    private final FrameLayout parent;

    public BackgroundManager(View root, BackgroundLoader loader) {
        this.loader = loader;

        // root is your FrameLayout from activity_splash (we added @+id/root earlier)
        this.parent = (FrameLayout) root;

        this.currentView = parent.findViewById(R.id.staticBg);

        this.nextView = new ImageView(root.getContext());
        this.nextView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.nextView.setVisibility(View.INVISIBLE);
        this.nextView.setLayoutParams(
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        // Insert nextView directly ABOVE current bg, but BELOW the logo
        // Assuming child order is: 0=bg, 1=logo; we insert at index 1.
        parent.addView(nextView, /*index*/ 1);
    }

    /** Start next transition at tick. */
    public void startTransition() {
        String path = loader.next();
        Bitmap bmp = loader.loadBitmap(path);
        if (bmp == null) return;
        nextView.setImageDrawable(new BitmapDrawable(nextView.getResources(), bmp));

        int mode = modeIndex % 3;
        modeIndex++;

        BackgroundTransition transition;
        switch (mode) {
            case 0: transition = new TransitionChromaFade(currentView, nextView, this::cleanup); break;
            case 1: transition = new TransitionCrossDissolve(currentView, nextView, this::cleanup); break;
            default: transition = new TransitionWhiteFlash(currentView, nextView, this::cleanup); break;
        }
        transition.start();
    }

    private void cleanup() {
        nextView.setImageDrawable(null);
        nextView.setVisibility(View.INVISIBLE);
    }
}
