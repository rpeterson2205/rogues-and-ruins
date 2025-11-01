package com.newdimension.rnr_cinematic.bg;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Loads and manages bag-shuffled backgrounds from assets/room_images/. */
public class BackgroundLoader {
    private final Context ctx;
    private final ArrayDeque<String> bag = new ArrayDeque<>();
    private final List<String> all = new ArrayList<>();

    public BackgroundLoader(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        reloadAll();
    }

    private void reloadAll() {
        try {
            AssetManager am = ctx.getAssets();
            String[] files = am.list("room_images");
            if (files != null) {
                all.clear();
                for (String f : files) {
                    String lf = f.toLowerCase();
                    if (lf.endsWith(".png") || lf.endsWith(".jpg") ||
                            lf.endsWith(".jpeg") || lf.endsWith(".webp") || lf.endsWith(".bmp")) {
                        all.add("room_images/" + f);
                    }
                }
                reshuffle();
            }
        } catch (IOException ignored) {}
    }

    private void reshuffle() {
        Collections.shuffle(all);
        bag.clear();
        bag.addAll(all);
    }

    /** Returns the next random (non-repeating) asset path. */
    public String next() {
        if (bag.isEmpty()) reshuffle();
        return bag.removeFirst();
    }

    /** Decode and return a bitmap from the given asset path. */
    public Bitmap loadBitmap(String path) {
        try (InputStream in = ctx.getAssets().open(path)) {
            return BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
