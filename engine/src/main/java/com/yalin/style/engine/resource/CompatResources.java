package com.yalin.style.engine.resource;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.AnyRes;
import android.support.annotation.ArrayRes;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.FractionRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.annotation.RawRes;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jinyalin
 * @since 2017/7/17.
 */

public class CompatResources extends Resources {
    private Resources mHostResources;


    public CompatResources(Resources host, AssetManager assets,
                           DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
        mHostResources = host;
    }

    @NonNull
    @Override
    public CharSequence getText(@StringRes int id) throws NotFoundException {
        try {
            return super.getText(id);
        } catch (Exception e) {
            return mHostResources.getText(id);
        }
    }

    @NonNull
    @Override
    public CharSequence getQuantityText(@PluralsRes int id, int quantity) throws NotFoundException {
        try {
            return super.getQuantityText(id, quantity);
        } catch (Exception e) {
            return mHostResources.getQuantityText(id, quantity);
        }
    }

    @NonNull
    @Override
    public String getString(@StringRes int id) throws NotFoundException {
        try {
            return super.getString(id);
        } catch (Exception e) {
            return mHostResources.getString(id);
        }
    }

    @NonNull
    @Override
    public String getString(@StringRes int id, Object... formatArgs) throws NotFoundException {
        try {
            return super.getString(id, formatArgs);
        } catch (Exception e) {
            return mHostResources.getString(id, formatArgs);
        }
    }

    @NonNull
    @Override
    public String getQuantityString(@PluralsRes int id, int quantity, Object... formatArgs) throws NotFoundException {
        try {
            return super.getQuantityString(id, quantity, formatArgs);
        } catch (Exception e) {
            return mHostResources.getQuantityString(id, quantity, formatArgs);
        }
    }

    @NonNull
    @Override
    public String getQuantityString(@PluralsRes int id, int quantity) throws NotFoundException {
        try {
            return super.getQuantityString(id, quantity);
        } catch (Exception e) {
            return mHostResources.getQuantityString(id, quantity);
        }
    }

    @Override
    public CharSequence getText(@StringRes int id, CharSequence def) {
        try {
            return super.getText(id, def);
        } catch (Exception e) {
            return mHostResources.getText(id, def);
        }
    }

    @NonNull
    @Override
    public CharSequence[] getTextArray(@ArrayRes int id) throws NotFoundException {
        try {
            return super.getTextArray(id);
        } catch (Exception e) {
            return mHostResources.getTextArray(id);
        }
    }

    @NonNull
    @Override
    public String[] getStringArray(@ArrayRes int id) throws NotFoundException {
        try {
            return super.getStringArray(id);
        } catch (Exception e) {
            return mHostResources.getStringArray(id);
        }
    }

    @NonNull
    @Override
    public int[] getIntArray(@ArrayRes int id) throws NotFoundException {
        try {
            return super.getIntArray(id);
        } catch (Exception e) {
            return mHostResources.getIntArray(id);
        }
    }

    @NonNull
    @Override
    public TypedArray obtainTypedArray(@ArrayRes int id) throws NotFoundException {
        try {
            return super.obtainTypedArray(id);
        } catch (Exception e) {
            return mHostResources.obtainTypedArray(id);
        }
    }

    @Override
    public float getDimension(@DimenRes int id) throws NotFoundException {
        try {
            return super.getDimension(id);
        } catch (Exception e) {
            return mHostResources.getDimension(id);
        }
    }

    @Override
    public int getDimensionPixelOffset(@DimenRes int id) throws NotFoundException {
        try {
            return super.getDimensionPixelOffset(id);
        } catch (Exception e) {
            return mHostResources.getDimensionPixelOffset(id);
        }
    }

    @Override
    public int getDimensionPixelSize(@DimenRes int id) throws NotFoundException {
        try {
            return super.getDimensionPixelSize(id);
        } catch (Exception e) {
            return mHostResources.getDimensionPixelSize(id);
        }
    }

    @Override
    public float getFraction(@FractionRes int id, int base, int pbase) {
        try {
            return super.getFraction(id, base, pbase);
        } catch (Exception e) {
            return mHostResources.getFraction(id, base, pbase);
        }
    }

    @Override
    public Drawable getDrawable(@DrawableRes int id) throws NotFoundException {
        try {
            return super.getDrawable(id);
        } catch (Exception e) {
            return mHostResources.getDrawable(id);
        }
    }

    @RequiresApi(api = 21)
    @Override
    public Drawable getDrawable(@DrawableRes int id, @Nullable Theme theme)
            throws NotFoundException {
        try {
            return super.getDrawable(id, theme);
        } catch (Exception e) {
            return mHostResources.getDrawable(id, theme);
        }
    }

    @RequiresApi(api = 15)
    @Override
    public Drawable getDrawableForDensity(@DrawableRes int id, int density)
            throws NotFoundException {
        try {
            return super.getDrawableForDensity(id, density);
        } catch (Exception e) {
            return mHostResources.getDrawableForDensity(id, density);
        }
    }

    @RequiresApi(api = 21)
    @Override
    public Drawable getDrawableForDensity(@DrawableRes int id, int density, @Nullable Theme theme) {
        try {
            return super.getDrawableForDensity(id, density, theme);
        } catch (Exception e) {
            return mHostResources.getDrawableForDensity(id, density, theme);
        }
    }

    @Override
    public Movie getMovie(@RawRes int id) throws NotFoundException {
        try {
            return super.getMovie(id);
        } catch (Exception e) {
            return mHostResources.getMovie(id);
        }
    }

    @Override
    public int getColor(@ColorRes int id) throws NotFoundException {
        try {
            return super.getColor(id);
        } catch (Exception e) {
            return mHostResources.getColor(id);
        }
    }

    @RequiresApi(api = 23)
    @Override
    public int getColor(@ColorRes int id, @Nullable Theme theme) throws NotFoundException {
        try {
            return super.getColor(id, theme);
        } catch (Exception e) {
            return mHostResources.getColor(id, theme);
        }
    }

    @Nullable
    @Override
    public ColorStateList getColorStateList(@ColorRes int id) throws NotFoundException {
        try {
            return super.getColorStateList(id);
        } catch (Exception e) {
            return mHostResources.getColorStateList(id);
        }
    }

    @RequiresApi(api = 23)
    @Nullable
    @Override
    public ColorStateList getColorStateList(@ColorRes int id, @Nullable Theme theme)
            throws NotFoundException {
        try {
            return super.getColorStateList(id, theme);
        } catch (Exception e) {
            return mHostResources.getColorStateList(id, theme);
        }
    }

    @Override
    public boolean getBoolean(@BoolRes int id) throws NotFoundException {
        try {
            return super.getBoolean(id);
        } catch (Exception e) {
            return mHostResources.getBoolean(id);
        }
    }

    @Override
    public int getInteger(@IntegerRes int id) throws NotFoundException {
        try {
            return super.getInteger(id);
        } catch (Exception e) {
            return mHostResources.getInteger(id);
        }
    }

    @Override
    public XmlResourceParser getLayout(@LayoutRes int id) throws NotFoundException {
        try {
            return super.getLayout(id);
        } catch (Exception e) {
            return mHostResources.getLayout(id);
        }
    }

    @Override
    public XmlResourceParser getAnimation(@AnimRes int id) throws NotFoundException {
        try {
            return super.getAnimation(id);
        } catch (Exception e) {
            return mHostResources.getAnimation(id);
        }
    }

    @Override
    public XmlResourceParser getXml(@XmlRes int id) throws NotFoundException {
        try {
            return super.getXml(id);
        } catch (Exception e) {
            return mHostResources.getXml(id);
        }
    }

    @Override
    public InputStream openRawResource(@RawRes int id) throws NotFoundException {
        try {
            return super.openRawResource(id);
        } catch (Exception e) {
            return mHostResources.openRawResource(id);
        }
    }

    @Override
    public InputStream openRawResource(@RawRes int id, TypedValue value) throws NotFoundException {
        try {
            return super.openRawResource(id, value);
        } catch (Exception e) {
            return mHostResources.openRawResource(id, value);
        }
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(@RawRes int id) throws NotFoundException {
        try {
            return super.openRawResourceFd(id);
        } catch (Exception e) {
            return mHostResources.openRawResourceFd(id);
        }
    }

    @Override
    public void getValue(@AnyRes int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        try {
            super.getValue(id, outValue, resolveRefs);
        } catch (Exception e) {
            mHostResources.getValue(id, outValue, resolveRefs);
        }
    }

    @RequiresApi(api = 15)
    @Override
    public void getValueForDensity(@AnyRes int id, int density, TypedValue outValue,
                                   boolean resolveRefs) throws NotFoundException {
        try {
            super.getValueForDensity(id, density, outValue, resolveRefs);
        } catch (Exception e) {
            mHostResources.getValueForDensity(id, density, outValue, resolveRefs);
        }
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        try {
            super.getValue(name, outValue, resolveRefs);
        } catch (Exception e) {
            mHostResources.getValue(name, outValue, resolveRefs);
        }
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        try {
            return super.obtainAttributes(set, attrs);
        } catch (Exception e) {
            return mHostResources.obtainAttributes(set, attrs);
        }
    }

    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
        try {
            super.updateConfiguration(config, metrics);
        } catch (Exception e) {
//            mHostResources.updateConfiguration(config, metrics);
        }
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        try {
            return super.getDisplayMetrics();
        } catch (Exception e) {
            return mHostResources.getDisplayMetrics();
        }
    }

    @Override
    public Configuration getConfiguration() {
        try {
            return super.getConfiguration();
        } catch (Exception e) {
            return mHostResources.getConfiguration();
        }
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        try {
            return super.getIdentifier(name, defType, defPackage);
        } catch (Exception e) {
            return mHostResources.getIdentifier(name, defType, defPackage);
        }
    }

    @Override
    public String getResourceName(@AnyRes int resid) throws NotFoundException {
        try {
            return super.getResourceName(resid);
        } catch (Exception e) {
            return mHostResources.getResourceName(resid);
        }
    }

    @Override
    public String getResourcePackageName(@AnyRes int resid) throws NotFoundException {
        try {
            return super.getResourcePackageName(resid);
        } catch (Exception e) {
            return mHostResources.getResourcePackageName(resid);
        }
    }

    @Override
    public String getResourceTypeName(@AnyRes int resid) throws NotFoundException {
        try {
            return super.getResourceTypeName(resid);
        } catch (Exception e) {
            return mHostResources.getResourceTypeName(resid);
        }
    }

    @Override
    public String getResourceEntryName(@AnyRes int resid) throws NotFoundException {
        try {
            return super.getResourceEntryName(resid);
        } catch (Exception e) {
            return mHostResources.getResourceEntryName(resid);
        }
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
            throws XmlPullParserException, IOException {
        try {
            super.parseBundleExtras(parser, outBundle);
        } catch (Exception e) {
//            mHostResources.parseBundleExtras(parser, outBundle);
        }
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle)
            throws XmlPullParserException {
        try {
            super.parseBundleExtra(tagName, attrs, outBundle);
        } catch (Exception e) {
//            mHostResources.parseBundleExtra(tagName, attrs, outBundle);
        }
    }

    public static TypedArray obtainAttributes(
            Resources res, Theme theme, AttributeSet set, int[] attrs) {
        try {
            if (theme == null) {
                return res.obtainAttributes(set, attrs);
            }
            return theme.obtainStyledAttributes(set, attrs, 0, 0);
        } catch (Exception e) {
            return (TypedArray) ReflectUtil.invokeNoException(
                    Resources.class, null, "obtainAttributes",
                    new Class[]{Resources.class, Theme.class, AttributeSet.class, int[].class},
                    res, theme, set, attrs);
        }
    }
}
