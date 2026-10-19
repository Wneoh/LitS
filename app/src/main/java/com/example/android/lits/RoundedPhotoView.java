package com.example.android.lits;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import androidx.appcompat.widget.AppCompatImageView;

/** Rounds the actual photo bounds, including photos letterboxed by FIT_CENTER. */
public final class RoundedPhotoView extends AppCompatImageView {
    private final Path clip = new Path();
    private final RectF bounds = new RectF();
    private final float radius;

    public RoundedPhotoView(Context context) {
        super(context);
        radius = 16 * getResources().getDisplayMetrics().density;
    }

    @Override protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) { super.onDraw(canvas); return; }
        bounds.set(getDrawable().getBounds());
        getImageMatrix().mapRect(bounds);
        bounds.offset(getPaddingLeft(), getPaddingTop());
        if (!bounds.intersect(0, 0, getWidth(), getHeight())) return;
        clip.reset();
        clip.addRoundRect(bounds, radius, radius, Path.Direction.CW);
        int saved = canvas.save();
        canvas.clipPath(clip);
        super.onDraw(canvas);
        canvas.restoreToCount(saved);
    }
}
