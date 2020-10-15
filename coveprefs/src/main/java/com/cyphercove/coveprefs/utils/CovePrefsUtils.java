/*
 * Copyright (C) 2015 The Android Open Source Project
 * Copyright (C) 2017 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

/*
 * This class's parseTintMode method contains derivative code from the v7.preference library's
 * android.support.v7.widget.DrawableUtils class.*/
package com.cyphercove.coveprefs.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RestrictTo;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import java.util.List;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Utility methods not part of the public API.
 */
@RestrictTo(LIBRARY_GROUP)
public final class CovePrefsUtils {
    private CovePrefsUtils() {}

    private static final int[] OUTPUT = new int[2];

    /**
     * Returns the x position of the view in relation to the second view
     * @param view The view whose position is to be measured.
     * @param otherView The view the first view's position is measured from.
     * @return The relative X position
     */
    public static float getRelativeX (View view, View otherView) {
        view.getLocationOnScreen(OUTPUT);
        int viewScreenX = OUTPUT[0];
        otherView.getLocationOnScreen(OUTPUT);
        int otherViewScreenX = OUTPUT[0];
        return viewScreenX - otherViewScreenX;
    }

    /**
     * Returns the y position of the view in relation to the second view
     * @param view The view whose position is to be measured.
     * @param otherView The view the first view's position is measured from.
     * @return The relative y position
     */
    public static float getRelativeY (View view, View otherView) {
        view.getLocationOnScreen(OUTPUT);
        int viewScreenY = OUTPUT[1];
        otherView.getLocationOnScreen(OUTPUT);
        int otherViewScreenX = OUTPUT[1];
        return viewScreenY - otherViewScreenX;
    }

    /** Sets {@link ViewGroup#setClipChildren(boolean)} and {@link ViewGroup#setClipToPadding(boolean)} to false on all
     * ancestors of the given view, up to but not including the given ancestor.
     * @param view The view at the bottom of the chain to change.
     * @param ancestor The final view in the chain to change. */
    public static void clearAncestorOutlineClipping (View view, ViewParent ancestor){
        if (Build.VERSION.SDK_INT < 21)
            return;
        if (view != ancestor){
            if (view instanceof ViewGroup){
                ((ViewGroup) view).setClipChildren(false);
                ((ViewGroup) view).setClipToPadding(false);
            }
            ViewParent viewParent = view.getParent();
            if (viewParent instanceof View)
                clearAncestorOutlineClipping((View)viewParent, ancestor);
        }
    }

    // Restricted because the coveprefs_tintMode attribute may change.
    public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3: return PorterDuff.Mode.SRC_OVER;
            case 5: return PorterDuff.Mode.SRC_IN;
            case 9: return PorterDuff.Mode.SRC_ATOP;
            case 14: return PorterDuff.Mode.MULTIPLY;
            case 15: return PorterDuff.Mode.SCREEN;
            case 16: return PorterDuff.Mode.ADD;
            default: return defaultMode;
        }
    }

    public static float calculateGrayScaleValue (int color){
        return (0.3f * ((color & 0x00ff0000) >>> 16) + 0.59f * ((color & 0x0000ff00) >>> 8)+ 0.11f * ((color & 0x000000ff))) / 255f;
    }

    /**
     * Resolves an Intent that can open an Activity from the given URIs.
     * @param context Application context.
     * @param formatArg If non-null, the URIs are treated as format Strings with it as the sole argument.
     * @param uris The URIs to use in order of precedence. The first match is used.
     * @return An Intent with a view action that has a match for an Activity from the PackageManager,
     * or null if no Activity is found that can open any of the URIs.
     */
    public static Intent resolveIntent(Context context, String formatArg, String... uris) {
        for (String uri : uris) {
            if (uri == null) {
                continue;
            }
            String formattedUri = formatArg == null ? uri : String.format(uri, formatArg);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(formattedUri));
            List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (!activities.isEmpty()) {
                return intent;
            }
        }
        return null;
    }

    public static void setContrastingShadow(TextView textView, float radiusPx){
        ColorStateList colorStateList = textView.getTextColors();
        int enabledColor = colorStateList.getColorForState(new int[]{android.R.attr.state_enabled}, 0xFF808080);
        float[] hsv = new float[3];
        Color.colorToHSV(enabledColor, hsv);
        int contrastColor = hsv[2] > 0.5f ? Color.BLACK : Color.WHITE;
        setShadow(textView, contrastColor, radiusPx);
    }

    public static void setShadow(TextView textView, int shadowColor, float radiusPx){
        textView.setShadowLayer(radiusPx, 0, 0, shadowColor);
    }

    /**
     * Various animation curves. Each method returns a modified fraction out.
     * <p>
     * Equations borrowed from
     * <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Interpolation.java">
     *     LibGDX's Interpolation class.</a>
     */
    @SuppressWarnings("WeakerAccess")
    public static class Curve {

        public static float smoothStep (float fraction) {
            return fraction * fraction * (3 - 2 * fraction);
        }

        public static float slowInSlowOut (float fraction) {
            return slowInSlowOut(fraction, 3);
        }

        public static float slowInSlowOut (float fraction, float power){
            if (fraction <= 0.5f) return (float)Math.pow(fraction * 2, 2) / 2;
            return (float)Math.pow((fraction - 1) * 2, power) / (power % 2 == 0 ? -2 : 2) + 1;
        }

        public static float slowInFastOut (float fraction){
            return slowInFastOut(fraction, 3);
        }

        public static float slowInFastOut (float fraction, float power){
            return (float)Math.pow(fraction, power);
        }

        public static float fastInSlowOut (float fraction){
            return fastInSlowOut(fraction, 3);
        }

        public static float fastInSlowOut (float fraction, float power){
            return (float)Math.pow(fraction - 1, power) * (power % 2 == 0 ? -1 : 1) + 1;
        }
    }
}
