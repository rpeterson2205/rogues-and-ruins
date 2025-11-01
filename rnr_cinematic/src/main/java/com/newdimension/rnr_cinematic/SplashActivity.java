package com.newdimension.rnr_cinematic;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.newdimension.rnr_cinematic.anim.BreathingSaturationAlgorithm;
import com.newdimension.rnr_cinematic.anim.BreathingValueAlgorithm;
import com.newdimension.rnr_cinematic.anim.GradientPulseAlgorithm;
import com.newdimension.rnr_cinematic.anim.HueWaveAlgorithm;
import com.newdimension.rnr_cinematic.anim.PulseHueAlgorithm;
import com.newdimension.rnr_cinematic.anim.SparkleNoiseAlgorithm;
import com.newdimension.rnr_cinematic.anim.ThemeAnimation;
import com.newdimension.rnr_cinematic.core.ThemeAnimationFactory;
import com.newdimension.rnr_cinematic.core.ThemeCycleEngine;
import com.google.android.material.button.MaterialButton;
import com.newdimension.rnr_cinematic.ui.LogoMaskView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.newdimension.rnr_cinematic.choral.ChoralEngine;
import com.newdimension.rnr_cinematic.choral.Chorals;

import com.newdimension.rnr_cinematic.bg.BackgroundLoader;
import com.newdimension.rnr_cinematic.bg.BackgroundManager;

public class SplashActivity extends AppCompatActivity {

    private BackgroundManager bgManager;
    // Six base hues (R,O,Y,G,B,V)
    private static final float[] HUES = new float[]{0f, 30f, 55f, 120f, 210f, 275f};

    private ChoralEngine choralEngine;

    private ThemeCycleEngine engine;
    private NoRepeatFactory factory;

    private LogoMaskView logoMaskView;

    private MaterialButton btnStart, btnContinue, btnEditor;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        BackgroundLoader loader = new BackgroundLoader(this);
        bgManager = new BackgroundManager(findViewById(R.id.splashRoot), loader);

        bgManager.startTransition();

        // Fullscreen immersive: no status bar, no nav bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        android.view.View decor = getWindow().getDecorView();
        androidx.core.view.WindowInsetsControllerCompat c =
                new androidx.core.view.WindowInsetsControllerCompat(getWindow(), decor);
        c.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars()
                | androidx.core.view.WindowInsetsCompat.Type.navigationBars());
        c.setSystemBarsBehavior(
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        logoMaskView = findViewById(R.id.logoMaskView);

        btnStart = findViewById(R.id.btnStartNew);
        btnContinue = findViewById(R.id.btnContinue);
        btnEditor = findViewById(R.id.btnMapEditor);

        // Init engine
        engine = new ThemeCycleEngine(
                HUES,
                ThemeCycleEngine.DEFAULT_THEME_HOLD_MS,
                ThemeCycleEngine.DEFAULT_CROSSFADE_MS,
                ThemeCycleEngine.DEFAULT_SAT,
                ThemeCycleEngine.DEFAULT_VAL
        );
        engine.attach(findViewById(R.id.splashRoot));

        factory = new NoRepeatFactory();

        engine.setListener(new ThemeCycleEngine.Listener() {
            @Override public void onThemeChanged(int idx, float hue) {

                // change to your favorite algo
                ThemeAnimation algo = new BreathingValueAlgorithm();

                algo.attach(findViewById(R.id.logoMaskView));
                algo.setBaseHue(hue);

                // 🔊 start 4 random chorals at the beginning of this color transition
                if (choralEngine != null) choralEngine.onColorTransition();

                bgManager.startTransition();  // 800ms transition sync with tick

                algo.start();
            }
            @Override public void onTick(float progress01) {
                // no-op (or hook a UI meter if you want)
            }
        });


        // Boot with an initial algorithm
        ThemeAnimation first = factory.next();
        engine.start(first);

        // Hook up buttons (stubs)
        btnStart.setOnClickListener(v -> Toast.makeText(this, "Start New (stub)", Toast.LENGTH_SHORT).show());
        btnContinue.setOnClickListener(v -> Toast.makeText(this, "Continue (stub)", Toast.LENGTH_SHORT).show());
        btnEditor.setOnClickListener(v -> Toast.makeText(this, "Map Editor (stub)", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // (your existing engine start code stays as-is)

        // Start choral system fresh (treat as app just launched → initial "red" transition)
        if (choralEngine == null) {
            choralEngine = new ChoralEngine(getApplicationContext(), Chorals.all());
        }
        choralEngine.startFresh(); // also triggers an initial 4-track start
    }


    @Override
    protected void onPause() {
        // (your existing visual engine stop code stays as-is)

        if (choralEngine != null) {
            choralEngine.stopAndRelease();
            // keep reference; onStart() will re-use/create as needed
        }
        super.onPause();
    }


    @Override protected void onResume() {
        super.onResume();
        // Resume with a fresh algorithm (keeps the no-repeat bag in play)
        ThemeAnimation again = factory.next();
        again.attach(findViewById(R.id.splashRoot));
        engine.start(again);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        engine.dispose();
    }

    /** Simple no-repeat bag of algorithms. Add your other algos here once fixed. */
    private static class NoRepeatFactory implements ThemeAnimationFactory {
        private final List<Integer> poolOrder = new ArrayList<>();
        private final List<Integer> bag = new ArrayList<>();

        NoRepeatFactory() {
            for (int i = 0; i < 1; i++) poolOrder.add(i); // currently 1 algo; expand as you add more
            refillBag();
        }

        @Override public ThemeAnimation createForTheme(int themeIndex, float baseHueDegrees) {
            return next();
        }

        ThemeAnimation next() {
            if (bag.isEmpty()) refillBag();
            int pick = bag.remove(0);
            switch (pick) {
                case 0: default: return new PulseHueAlgorithm();
            }
        }

        private void refillBag() {
            bag.clear();
            bag.addAll(poolOrder);
            Collections.shuffle(bag);
        }
    }
}
