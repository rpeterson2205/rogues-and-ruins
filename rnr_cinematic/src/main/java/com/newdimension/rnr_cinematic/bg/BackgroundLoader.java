package com.newdimension.rnr_cinematic.bg;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Slim BackgroundLoader (compatible)
 * - assets/bg_scene/*
 * - nextPath()/next() non-repeating
 * - open(), decode(), loadBitmap(context, path)
 */
public final class BackgroundLoader {
    private static final String TAG = "BackgroundLoader";
    private static final String BG_DIR = "bg_scene";

    private static volatile BackgroundLoader INSTANCE;
    public static BackgroundLoader get() {
        if (INSTANCE == null) {
            synchronized (BackgroundLoader.class) {
                if (INSTANCE == null) INSTANCE = new BackgroundLoader();
            }
        }
        return INSTANCE;
    }

    private final Object lock = new Object();
    private boolean inited = false;
    private final List<String> all = new ArrayList<>();   // "bg_scene/file.jpg"
    private final Deque<String> cycle = new ArrayDeque<>();

    private BackgroundLoader() {}

    /** Safe to call multiple times. */
    public void init(@NonNull Context context) {
        if (inited) return;
        synchronized (lock) {
            if (inited) return;
            try {
                AssetManager am = context.getApplicationContext().getAssets();
                String[] names = am.list(BG_DIR);
                if (names != null) {
                    for (String n : names) {
                        if (n == null || n.isEmpty()) continue;
                        String ln = n.toLowerCase();
                        if (ln.endsWith(".png") || ln.endsWith(".jpg") || ln.endsWith(".jpeg")
                                || ln.endsWith(".webp") || ln.endsWith(".bmp")) {
                            all.add(BG_DIR + "/" + n);
                        }
                    }
                }
                reshuffle();
                inited = true;
                Log.d(TAG, "Backgrounds indexed: " + all.size());
            } catch (Exception e) {
                Log.e(TAG, "init failed", e);
            }
        }
    }

    /** Back-compat shim for old code. */
    @NonNull public String next() { return nextPath(); }

    /** Returns next asset path like "bg_scene/foo.jpg". */
    @NonNull
    public String nextPath() {
        synchronized (lock) {
            if (!inited) throw new IllegalStateException("BackgroundLoader.init(context) not called");
            if (cycle.isEmpty()) reshuffle();
            String p = cycle.pollFirst();
            if (p == null) throw new IllegalStateException("No backgrounds in assets/" + BG_DIR);
            return p;
        }
    }

    /** Open an InputStream for an asset path returned by nextPath(). Caller closes. */
    @Nullable
    public InputStream open(@NonNull Context context, @NonNull String assetPath) {
        try {
            return context.getApplicationContext().getAssets().open(assetPath);
        } catch (IOException e) {
            Log.e(TAG, "open failed: " + assetPath, e);
            return null;
        }
    }

    /** Decode bitmap with simple downsampling to approx reqW x reqH. */
    @Nullable
    public Bitmap decode(@NonNull Context context, @NonNull String assetPath, int reqW, int reqH) {
        AssetManager am = context.getApplicationContext().getAssets();
        InputStream b = null, d = null;
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            b = am.open(assetPath);
            BitmapFactory.decodeStream(b, null, o);
            close(b);

            o.inSampleSize = computeSample(o, reqW, reqH);
            o.inJustDecodeBounds = false;

            d = am.open(assetPath);
            return BitmapFactory.decodeStream(d, null, o);
        } catch (IOException e) {
            Log.e(TAG, "decode failed: " + assetPath, e);
            return null;
        } finally {
            close(b);
            close(d);
        }
    }

    /** Convenience for code that expects loadBitmap(path). No scaling. */
    @Nullable
    public Bitmap loadBitmap(@NonNull Context context, @NonNull String assetPath) {
        return decode(context, assetPath, 0, 0);
    }

    // --- internals ---
    private void reshuffle() {
        cycle.clear();
        if (all.isEmpty()) return;
        List<String> tmp = new ArrayList<>(all);
        Collections.shuffle(tmp);
        cycle.addAll(tmp);
    }

    private static int computeSample(BitmapFactory.Options o, int reqW, int reqH) {
        int w = Math.max(o.outWidth, 1), h = Math.max(o.outHeight, 1);
        if (reqW <= 0 || reqH <= 0) return 1;
        int s = 1;
        while ((w / (s * 2)) >= reqW && (h / (s * 2)) >= reqH) s *= 2;
        return Math.max(s, 1);
    }

    private static void close(@Nullable InputStream is) {
        if (is == null) return;
        try { is.close(); } catch (IOException ignored) {}
    }
    // How many background files are indexed (after init)
    public int getIndexedCount() {
        synchronized (lock) {
            return all.size();
        }
    }

    public boolean isInitialized() {
        synchronized (lock) { return inited; }
    }

}
