package com.yalin.style.util;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.LruCache;
import android.view.Gravity;

/**
 * @author jinyalin
 * @since 2017/4/21.
 * Utility methods for creating prettier gradient scrims.
 */
public class ScrimUtil {

    private static final LruCache<Integer, Drawable> cubicGradientScrimCache = new LruCache<>(10);

    private ScrimUtil() {
    }

    /**
     * Creates an approximated cubic gradient using a multi-stop linear gradient. See
     * <a href="https://plus.google.com/+RomanNurik/posts/2QvHVFWrHZf">this post</a> for more
     * details.
     */
    public static Drawable makeCubicGradientScrimDrawable(int baseColor,
                                                          int numStops, int gravity) {

        // Generate a cache key by hashing together the inputs,
        // based on the method described in the Effective Java book
        int cacheKeyHash = baseColor;
        cacheKeyHash = 31 * cacheKeyHash + numStops;
        cacheKeyHash = 31 * cacheKeyHash + gravity;

        Drawable cachedGradient = cubicGradientScrimCache.get(cacheKeyHash);
        if (cachedGradient != null) {
            return cachedGradient;
        }

        numStops = Math.max(numStops, 2);

        PaintDrawable paintDrawable = new PaintDrawable();
        paintDrawable.setShape(new RectShape());

        final int[] stopColors = new int[numStops];

        int red = Color.red(baseColor);
        int green = Color.green(baseColor);
        int blue = Color.blue(baseColor);
        int alpha = Color.alpha(baseColor);

        for (int i = 0; i < numStops; i++) {
            float x = i * 1f / (numStops - 1);
            float opacity = MathUtil.constrain(0, 1, (float) Math.pow(x, 3));
            stopColors[i] = Color.argb((int) (alpha * opacity), red, green, blue);
        }

        final float x0, x1, y0, y1;
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
                x0 = 1;
                x1 = 0;
                break;
            case Gravity.RIGHT:
                x0 = 0;
                x1 = 1;
                break;
            default:
                x0 = 0;
                x1 = 0;
                break;
        }
        switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
                y0 = 1;
                y1 = 0;
                break;
            case Gravity.BOTTOM:
                y0 = 0;
                y1 = 1;
                break;
            default:
                y0 = 0;
                y1 = 0;
                break;
        }

        paintDrawable.setShaderFactory(new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(
                        width * x0,
                        height * y0,
                        width * x1,
                        height * y1,
                        stopColors, null,
                        Shader.TileMode.CLAMP);
            }
        });

        cubicGradientScrimCache.put(cacheKeyHash, paintDrawable);
        return paintDrawable;
    }
}
