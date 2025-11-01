package com.newdimension.rnr_cinematic.choral;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * ChoralEngine (v2 – bag shuffle edition)
 *
 * Rules:
 *  - On each color transition, randomly select 4 DISTINCT chorals from a shuffle bag of all 14.
 *  - Bag refills (reshuffles) only after every choral has been played once.
 *  - No audio mixing, effects, or volume changes.
 *  - Chorals are never stopped mid-play; they run to completion, even if overlapping transitions.
 *  - onPause() releases all players; onStart() restarts fresh.
 */
public final class ChoralEngine {

    private final Context appContext;
    private final int[] choralResIds;
    private final Random rng = new Random();

    // One primary player per resource id
    private final HashMap<Integer, ExoPlayer> primaryPlayers = new HashMap<>();
    // Spillover players created when a primary is already busy
    private final List<ExoPlayer> spillover = new ArrayList<>();

    // Shuffle bag indices to avoid repeats until exhausted
    private final ArrayDeque<Integer> bag = new ArrayDeque<>();

    private boolean started = false;

    public ChoralEngine(@NonNull Context context, @NonNull int[] choralResIds) {
        this.appContext = context.getApplicationContext();
        this.choralResIds = choralResIds.clone();
    }

    // ------------------------------------------------------------
    // Lifecycle hooks
    // ------------------------------------------------------------

    /** Called when app comes to foreground (acts as a fresh launch). */
    @MainThread
    public void startFresh() {
        stopAndRelease();
        ensurePrimaries();
        refillBag();
        started = true;
        onColorTransition(); // initial red transition
    }

    /** Called when app loses focus / goes to background. */
    @MainThread
    public void stopAndRelease() {
        started = false;

        // Release primaries
        for (ExoPlayer p : primaryPlayers.values()) {
            safeStop(p);
            safeRelease(p);
        }
        primaryPlayers.clear();

        // Release spillovers
        for (ExoPlayer p : spillover) {
            safeStop(p);
            safeRelease(p);
        }
        spillover.clear();
    }

    // ------------------------------------------------------------
    // Playback logic
    // ------------------------------------------------------------

    /** Triggered from ThemeCycleEngine on every color transition. */
    @MainThread
    public void onColorTransition() {
        if (!started || choralResIds.length == 0) return;

        // Ensure we have at least 4 left in the bag; if not, reshuffle
        if (bag.size() < 4) refillBag();

        // Draw 4 distinct indices from the bag
        List<Integer> picks = new ArrayList<>(4);
        for (int i = 0; i < Math.min(4, choralResIds.length); i++) {
            if (bag.isEmpty()) refillBag();
            picks.add(bag.removeFirst());
        }

        // Start all 4 simultaneously
        final long now = System.nanoTime();
        for (int idx : picks) {
            int resId = choralResIds[idx];
            playOnce(resId, now);
        }
    }

    // ------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------

    /** Populate and shuffle the bag with all choral indices. */
    private void refillBag() {
        List<Integer> all = new ArrayList<>(choralResIds.length);
        for (int i = 0; i < choralResIds.length; i++) all.add(i);
        Collections.shuffle(all, rng);
        bag.clear();
        for (int i : all) bag.addLast(i);
    }

    private void ensurePrimaries() {
        for (int resId : choralResIds) {
            if (!primaryPlayers.containsKey(resId)) {
                primaryPlayers.put(resId, newPlayer());
            }
        }
    }

    private void playOnce(int resId, long syncGroupTs) {
        ExoPlayer primary = primaryPlayers.get(resId);
        if (primary == null) {
            primary = newPlayer();
            primaryPlayers.put(resId, primary);
        }

        if (isPlaying(primary)) {
            // Primary busy → allocate a one-shot spillover player
            ExoPlayer extra = newPlayer();
            spillover.add(extra);
            prepareAndPlay(extra, resId, syncGroupTs, true);
        } else {
            prepareAndPlay(primary, resId, syncGroupTs, false);
        }
    }

    private boolean isPlaying(ExoPlayer p) {
        return p != null && p.isPlaying();
    }

    private ExoPlayer newPlayer() {
        return new ExoPlayer.Builder(appContext).build();
    }

    private void prepareAndPlay(ExoPlayer player, int resId, long syncGroupTs, boolean autoRelease) {
        Uri uri = Uri.parse("android.resource://" + appContext.getPackageName() + "/" + resId);
        MediaItem item = MediaItem.fromUri(uri);
        player.setMediaItem(item);
        player.prepare();
        player.play();

        if (autoRelease) {
            player.addListener(new com.google.android.exoplayer2.Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == ExoPlayer.STATE_ENDED) {
                        safeStop(player);
                        safeRelease(player);
                        spillover.remove(player);
                    }
                }
            });
        }
    }

    private void safeStop(ExoPlayer p) {
        try { p.stop(); } catch (Throwable ignored) {}
    }

    private void safeRelease(ExoPlayer p) {
        try { p.release(); } catch (Throwable ignored) {}
    }
}
