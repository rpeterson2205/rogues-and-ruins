package com.newdimension.rnr_cinematic.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.newdimension.rnr_cinematic.R;

/** Full-bleed, edge-to-edge pulsing fill clipped to the logo silhouette.
 *  Uses the exact math of ImageView.CENTER_CROP so it lines up with your outline ImageView. */
public class LogoMaskView extends View {

    private final Paint paintMask = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap maskBitmap;      // white letters, transparent elsewhere
    private Bitmap working;         // buffer we draw into
    private int fillColor = Color.WHITE;

    // Matrix that matches ImageView ScaleType.CENTER_CROP (no padding applied)
    private final Matrix cropMatrix = new Matrix();

    public LogoMaskView(Context c) { super(c); init(); }
    public LogoMaskView(Context c, AttributeSet a) { super(c, a); init(); }
    public LogoMaskView(Context c, AttributeSet a, int s) { super(c, a, s); init(); }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        paintMask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        maskBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_mask);
    }

    /** Called by animations to update the pulsing color (ARGB). */
    public void setFillColor(int color) {
        if (fillColor != color) { fillColor = color; invalidate(); }
        postInvalidateOnAnimation();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        if (working != null && (working.getWidth() != w || working.getHeight() != h)) {
            working.recycle();
            working = null;
        }
        if (working == null) working = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        computeCenterCropMatrix(w, h);
    }

    /** Match ImageView.CENTER_CROP behavior exactly (no padding). */
    private void computeCenterCropMatrix(int vw, int vh) {
        if (maskBitmap == null) return;
        int bw = maskBitmap.getWidth();
        int bh = maskBitmap.getHeight();
        if (bw <= 0 || bh <= 0) return;

        float scale = Math.max(vw / (float) bw, vh / (float) bh); // CENTER_CROP uses max
        float dx = (vw - bw * scale) * 0.5f;  // center horizontally
        float dy = (vh - bh * scale) * 0.5f;  // center vertically

        cropMatrix.reset();
        cropMatrix.setScale(scale, scale);
        cropMatrix.postTranslate(dx, dy);
    }

    @Override protected void onDraw(Canvas canvas) {
        if (working == null) return;

        // 1) Fill the entire buffer with the current color
        Canvas buf = new Canvas(working);
        buf.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        buf.drawColor(fillColor);

        // 2) Apply the mask with CENTER_CROP transform
        if (maskBitmap != null) {
            buf.drawBitmap(maskBitmap, cropMatrix, paintMask);
        }

        // 3) Blit to screen
        canvas.drawBitmap(working, 0, 0, null);
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (working != null) { working.recycle(); working = null; }
    }
}
