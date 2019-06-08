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

import android.graphics.PorterDuff;
import android.os.Build;
import androidx.annotation.RestrictTo;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 */
public class ViewUtils {

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
     * ancestors of the given view, up to but not including the given ancestor. */
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

    @RestrictTo(LIBRARY_GROUP)
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

}
